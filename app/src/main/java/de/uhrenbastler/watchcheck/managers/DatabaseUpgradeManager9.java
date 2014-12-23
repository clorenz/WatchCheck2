package de.uhrenbastler.watchcheck.managers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.orm.Database;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import de.uhrenbastler.watchcheck.tools.Logger;

/**
 * Created by clorenz on 10.12.14.
 *
 * This class is a real hack to bypass the limitations of SugarORM, which is not capable
 * in running more than standard SQL scripts during the upgrade of a database.
 */
public class DatabaseUpgradeManager9 extends SQLiteOpenHelper {

    public DatabaseUpgradeManager9(Context context) {
        super(context, "watchcheck.db", null, 9);

        try {
            new Database(context).getDB();
        } catch ( SQLiteException e) {
            if (e.getMessage().indexOf("exit") > -1) {
                Logger.info("Upgrading database required");
                getWritableDatabase().execSQL("PRAGMA user_version=9");
            }
        }
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        // Ignore this, since SugarORM already created the database
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Logger.info("Upgrading db from "+oldVersion+" to "+newVersion);
        //
        // TODO: Here: Prepare the new results table with the individual result frames
        try {
            db.execSQL("CREATE TABLE IF NOT EXISTS LOG ( ID INTEGER PRIMARY KEY AUTOINCREMENT, WATCH_ID INTEGER, PERIOD INTEGER, REFERENCE_TIME TIMESTAMP, WATCH_TIME TIMESTAMP, POSITION VARCHAR(2), TEMPERATURE INTEGER, COMMENT TEXT);");

            migrateLogs(db);
        } catch ( Exception e) {
            Logger.error("Cannot migrate old database: ",e);
        }

        /*
        BEGIN;
        CREATE TABLE IF NOT EXISTS LOG ( ID INTEGER PRIMARY KEY AUTOINCREMENT, WATCH_ID INTEGER, PERIOD INTEGER, REFERENCE_TIME TIMESTAMP, WATCH_TIME TIMESTAMP, POSITION VARCHAR(2), TEMPERATURE INTEGER, COMMENT TEXT);
        COMMIT;
        */
    }


    private void migrateLogs(SQLiteDatabase db) throws Exception {
        Cursor cursor = db.rawQuery("SELECT * FROM logs order by watch_id,local_timestamp asc", null);

        int period=-1;
        int oldWatchId=-1;

        if ( cursor.moveToFirst()) {
            do {
                int watchId = cursor.getInt(1);
                if ( watchId!=oldWatchId) {
                    oldWatchId=watchId;
                    period=-1;
                }

                Timestamp origTime = new Timestamp(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(cursor.getString(3)).getTime() -
                        (int)(1000d * cursor.getDouble(4)));
                Timestamp watchTime = new Timestamp(origTime.getTime() + (int)(1000d * cursor.getDouble(5)));
                if ( cursor.getInt(6) == 1) {
                    // Reset
                    period++;
                }
                String position = cursor.getString(7);
                int temperature = cursor.getInt(8);
                String comment = cursor.getString(9);

                Logger.debug("watchId=" + watchId+", pPeriod=" + period + ", origTime=" + origTime + ", watchTime=" + watchTime +
                        ", position=" + position + ", temperature=" + temperature + ", comment=" + comment);

                ContentValues insertValues = new ContentValues();
                insertValues.put("WATCH_ID",watchId);
                insertValues.put("PERIOD",period);
                insertValues.put("REFERENCE_TIME",origTime.toString());
                insertValues.put("WATCH_TIME",watchTime.toString());
                insertValues.put("POSITION",position);
                insertValues.put("TEMPERATURE",temperature);
                insertValues.put("COMMENT",comment);
                db.insert("LOG", null, insertValues);
            } while (cursor.moveToNext());
        }

    }

}
