package de.uhrenbastler.watchcheck.managers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import de.uhrenbastler.watchcheck.tools.Logger;
import watchcheck.db.DaoMaster;

/**
 * Created by clorenz on 30.12.14.
 *
 * Inspired by http://stackoverflow.com/a/25991289
 */
public class WatchCheckOpenHelper extends DaoMaster.OpenHelper {

    public WatchCheckOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
        super(context, name, factory);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Logger.info("Creating tables for schema version " + DaoMaster.SCHEMA_VERSION);
        DaoMaster.createAllTables(db, true);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        for (int i = oldVersion; i < newVersion; i++) {
            try {
                AbstractMigratorHelper migratorHelper = (AbstractMigratorHelper) Class.forName("de.uhrenbastler.watchcheck.managers.DBMigrationHelper" + i + "to" + (i + 1)).newInstance();

                if (migratorHelper != null) {
                    migratorHelper.onUpgrade(db);
                }

            } catch (MigrationException | ClassNotFoundException | ClassCastException | IllegalAccessException | InstantiationException e) {

                Logger.error("Could not migrate from from schema: " + i + " to " + (i+1)+": "+e.getMessage(),e);
                    /* If something fail prevent the DB to be updated to future version if the previous version has not been upgraded successfully */
                break;
            }
        }
    }
}
