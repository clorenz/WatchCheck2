package de.uhrenbastler.watchcheck.managers;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import de.uhrenbastler.watchcheck.tools.Logger;
import watchcheck.db.DaoMaster;

/**
 * Created by clorenz on 30.12.14.
 */
public class DBMigrationHelper4to5 extends AbstractMigratorHelper {

    @Override
    public void onUpgrade(SQLiteDatabase db) throws MigrationException {
        Logger.info("Upgrading database version 4 -> 5");
        // Beginning with schema version 5, we have the new tables, maintained by GreenORM
        DaoMaster.createAllTables(db, true);
        Logger.info("Successfully created new tables");

        try {
            migrateWatches(db);
            Logger.info("Successfully migrated watches");
        } catch ( Exception e) {
            throw new MigrationException("Cannot migrate watches ",e);
        }

        try {
            migrateLogs(db);
            Logger.info("Successfully migrated logs");
        } catch ( Exception e) {
            throw new MigrationException("Cannot migrate logs ",e);
        }

        Logger.info("Database upgrade to version 5 finished");
    }

    private void migrateWatches(SQLiteDatabase db) throws ParseException {
        Cursor cursor = db.rawQuery("SELECT _id,name,serial,date_create,comment FROM watches order by _id asc", null);

        if ( cursor.moveToFirst()) {
            do {
                int watchId = cursor.getInt(0);
                String name = cursor.getString(1);
                String serial = cursor.getString(2);
                String createdAt = cursor.getString(3);
                Timestamp dateCreate=null;
                if ( createdAt!=null && createdAt.length()>0 && !"null".equals(createdAt)) {
                    dateCreate = new Timestamp(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(createdAt).getTime());
                }
                String comment = cursor.getString(4);

                ContentValues insertValues = new ContentValues();
                insertValues.put("_ID", watchId);
                insertValues.put("NAME", name);
                insertValues.put("SERIAL", serial);
                if ( dateCreate!=null ) { insertValues.put("created_at", dateCreate.getTime()); }
                insertValues.put("COMMENT", comment);
                db.insert("WATCH", null, insertValues);
            } while (cursor.moveToNext());
        }
    }

    private void migrateLogs(SQLiteDatabase db) throws ParseException {
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

                Timestamp machineTime = new Timestamp(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(cursor.getString(3)).getTime());
                Timestamp referenceTime = new Timestamp(machineTime.getTime() -
                        (int)(1000d * cursor.getDouble(4)));

                long differenceInMillis = (long) (1000d * cursor.getDouble(5));     // Precise difference in milliseconds

                long watchTimeInMillis = referenceTime.getTime() + differenceInMillis;

                // We assume, that the seconds of the timed watch are always exact zero!
                long watchTimeInMillisPrecisionSeconds = (long)Math.ceil((double)watchTimeInMillis / 1000) * 1000;

                long millisForWatchToZero =  watchTimeInMillisPrecisionSeconds - watchTimeInMillis;

                // This number of millisForWatchToZero has now to be added to the referenceTime
                referenceTime = new Timestamp(referenceTime.getTime() + millisForWatchToZero);

                Timestamp watchTime = new Timestamp(watchTimeInMillisPrecisionSeconds);

                if ( cursor.getInt(6) == 1) {
                    // Reset
                    period++;
                }
                String position = cursor.getString(7);
                int temperature = cursor.getInt(8);
                String comment = cursor.getString(9);
                ContentValues insertValues = new ContentValues();
                insertValues.put("watch_id",watchId);
                insertValues.put("PERIOD",period);
                insertValues.put("reference_time",referenceTime.getTime());
                insertValues.put("watch_time",watchTime.getTime());
                insertValues.put("POSITION",position);
                insertValues.put("TEMPERATURE",temperature);
                insertValues.put("COMMENT",comment);
                db.insert("LOG", null, insertValues);
            } while (cursor.moveToNext());
        }
    }
}
