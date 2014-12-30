package de.uhrenbastler.watchcheck.managers;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by clorenz on 30.12.14.
 */
public abstract class AbstractMigratorHelper {

    public abstract void onUpgrade(SQLiteDatabase db) throws MigrationException;
}
