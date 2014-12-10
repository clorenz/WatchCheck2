package de.uhrenbastler.watchcheck.managers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.orm.Database;

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
        db.execSQL("CREATE TABLE IF NOT EXISTS LOG ( ID INTEGER PRIMARY KEY AUTOINCREMENT, WATCH_ID INTEGER, PERIOD INTEGER, REFERENCE_TIME TIMESTAMP, WATCH_TIME TIMESTAMP, POSITION VARCHAR(2), TEMPERATURE INTEGER, COMMENT TEXT);");

        /*
        BEGIN;
        CREATE TABLE IF NOT EXISTS LOG ( ID INTEGER PRIMARY KEY AUTOINCREMENT, WATCH_ID INTEGER, PERIOD INTEGER, REFERENCE_TIME TIMESTAMP, WATCH_TIME TIMESTAMP, POSITION VARCHAR(2), TEMPERATURE INTEGER, COMMENT TEXT);
        COMMIT;
        */
    }

}
