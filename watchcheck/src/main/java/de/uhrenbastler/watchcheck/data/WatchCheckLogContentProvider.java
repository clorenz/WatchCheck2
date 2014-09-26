package de.uhrenbastler.watchcheck.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import de.uhrenbastler.watchcheck.tools.Logger;

import static de.uhrenbastler.watchcheck.data.Log.Logs;
import static de.uhrenbastler.watchcheck.data.Watch.Watches;

/**
 * Created by clorenz on 14.02.14.
 */
public class WatchCheckLogContentProvider extends ContentProvider{

    public static final String AUTHORITY = "de.uhrenbastler.watchcheck.data.WatchCheckLogContentProvider";
    private static final UriMatcher sUriMatcher;
    private static final int WATCHES = 1;
    private static final int LOGS = 2;
    public static final int DB_VERSION = 4;
    private DatabaseHelper dbHelper;
    private static Map<String, String> watchesProjectionMap;
    private static Map<String, String> logsProjectionMap;
    public static final String WATCHCHECK_DB_NAME = "watchcheck.db";

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(AUTHORITY, Watches.TABLE_NAME, 1);
        sUriMatcher.addURI(AUTHORITY, Watches.TABLE_NAME+"/#", 1);
        sUriMatcher.addURI(AUTHORITY, Logs.TABLE_NAME, 2);
        sUriMatcher.addURI(AUTHORITY, "close", 3);

        watchesProjectionMap = new HashMap<String,String>();
        watchesProjectionMap.put(Watches._ID, Watches._ID);
        watchesProjectionMap.put(Watches.WATCH_ID, Watches.WATCH_ID);
        watchesProjectionMap.put(Watches.NAME, Watches.NAME);
        watchesProjectionMap.put(Watches.SERIAL, Watches.SERIAL);
        watchesProjectionMap.put(Watches.DATE_CREATE, Watches.DATE_CREATE);
        watchesProjectionMap.put(Watches.COMMENT, Watches.COMMENT);

        logsProjectionMap = new HashMap<String,String>();
        logsProjectionMap.put(Logs.LOG_ID, Logs.LOG_ID);
        logsProjectionMap.put(Logs.WATCH_ID, Logs.WATCH_ID);
        logsProjectionMap.put(Logs.MODUS, Logs.MODUS);
        logsProjectionMap.put(Logs.LOCAL_TIMESTAMP, Logs.LOCAL_TIMESTAMP);
        logsProjectionMap.put(Logs.NTP_DIFF, Logs.NTP_DIFF);
        logsProjectionMap.put(Logs.FLAG_RESET, Logs.FLAG_RESET);
        logsProjectionMap.put(Logs.POSITION, Logs.POSITION);
        logsProjectionMap.put(Logs.TEMPERATURE, Logs.TEMPERATURE);
        logsProjectionMap.put(Logs.COMMENT, Logs.COMMENT);
        logsProjectionMap.put(Logs.DEVIATION, Logs.DEVIATION);

    }

    // --------------------------------------------------------

    private static class DatabaseHelper extends SQLiteOpenHelper {



        DatabaseHelper(Context context) {
            super(context, WATCHCHECK_DB_NAME, null, DB_VERSION);
        }


        @Override
        public void onCreate(SQLiteDatabase db) {
            android.util.Log.d("WatchCheck","Creating databases");

            db.execSQL(Watches.CREATE_TABLE_STATEMENT);

            db.execSQL(Logs.CREATE_TABLE_STATEMENT);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Logger.info("Upgrading DB from " + oldVersion + " to " + newVersion);
            if ( oldVersion == 1 && newVersion >= 2) {
                db.execSQL("ALTER TABLE " + Logs.WATCH_ID +" RENAME TO "+Watches.TABLE_NAME);
                onUpgrade(db, (oldVersion)+1, newVersion);
            }
            if ( oldVersion == 2 && newVersion >= 3 ) {
                db.execSQL("ALTER TABLE " + Logs.TABLE_NAME +" ADD COLUMN "+
                        Logs.FLAG_RESET+" BOOLEAN");
                onUpgrade(db, (oldVersion+1), newVersion);
            }
            if ( oldVersion == 3 && newVersion == 4) {
                db.execSQL("ALTER TABLE " + Logs.TABLE_NAME +" ADD COLUMN "+
                        Logs.DEVIATION+" DECIMAL(6,2)");
            }
        }
    }


    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db=null;
        int count=0;

        try {
            db = dbHelper.getWritableDatabase();

            switch (sUriMatcher.match(uri)) {
                case WATCHES:
                    // TODO: Delete referencing logs, too??
                    Logger.debug("Deleting watch with "+selection+"="+ Arrays.toString(selectionArgs));
                    count = db.delete(Watches.TABLE_NAME, selection, selectionArgs);
                    break;

                case LOGS:
                    Logger.debug("Deleting logs with " + selection + "=" + Arrays.toString(selectionArgs));
                    count = db.delete(Logs.TABLE_NAME, selection, selectionArgs);
                    break;

                case 3:
                    Logger.debug("Closing database");
                    close();
                    break;

                default:
                    throw new IllegalArgumentException("Unknown URI " + uri);
            }

            getContext().getContentResolver().notifyChange(uri, null);
        } finally {
            if ( db!=null )
                db.close();
        }

        return count;
    }


    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case WATCHES:
                return Watches.CONTENT_TYPE;

            case LOGS:
                return Logs.CONTENT_TYPE;

            default:
                throw new IllegalArgumentException("Unknown URI "+uri);
        }
    }


    @Override
    public Uri insert(Uri uri, ContentValues values) {
        switch (sUriMatcher.match(uri)) {
            case WATCHES:
                return updateWatches(uri, values);

            case LOGS:
                return updateLogs(uri, values);

        }

        throw new IllegalArgumentException("Unknown URI " + uri);
    }



    private Uri updateWatches(Uri uri, ContentValues initialValues) {
        ContentValues values;

        if ( initialValues!=null)
            values = new ContentValues(initialValues);
        else
            values = new ContentValues();

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        long rowId = db.insert(Watches.TABLE_NAME, Watches.NAME, values);

        if ( rowId > 0) {
            Uri watchUri = ContentUris.withAppendedId(Watches.CONTENT_URI, rowId);
            getContext().getContentResolver().notifyChange(watchUri, null);
            return watchUri;
        } else {
            throw new SQLException("Could not insert into "+uri);
        }
    }


    private Uri updateLogs(Uri uri, ContentValues initialValues) {
        ContentValues values;

        if ( initialValues!=null)
            values = new ContentValues(initialValues);
        else
            values = new ContentValues();

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        long rowId = db.insert(Logs.TABLE_NAME, Logs.MODUS, values);

        if ( rowId > 0) {
            Uri LogUri = ContentUris.withAppendedId(Logs.CONTENT_URI, rowId);
            getContext().getContentResolver().notifyChange(LogUri, null);
            return LogUri;
        } else {
            throw new SQLException("Could not insert into "+uri);
        }
    }


    @Override
    public boolean onCreate() {
        dbHelper = new DatabaseHelper(getContext());
        return (dbHelper != null);
    }


    public void close() {
        dbHelper.close();
    }


    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        switch (sUriMatcher.match(uri)) {
            case WATCHES:
                qb.setTables(Watches.TABLE_NAME);
                qb.setProjectionMap(watchesProjectionMap);
                break;

            case LOGS:
                qb.setTables(Logs.TABLE_NAME);
                qb.setProjectionMap(logsProjectionMap);
                break;

            case 3:
                close();
                Logger.info("Closing database");
                return null;

            default:
                throw new IllegalArgumentException("Unknown URI: "+uri);
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);

        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }


    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
            case WATCHES:
                count = db.update(Watches.TABLE_NAME, values, selection, selectionArgs);
                break;
            case LOGS:
                count = db.update(Logs.TABLE_NAME, values, selection, selectionArgs);
                break;
            case 3:
                db.close();
                count = -1;
                Logger.info("Closing database");
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
}
