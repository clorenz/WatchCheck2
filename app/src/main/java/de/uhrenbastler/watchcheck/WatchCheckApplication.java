package de.uhrenbastler.watchcheck;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;

import de.uhrenbastler.watchcheck.managers.WatchCheckOpenHelper;
import watchcheck.db.DaoMaster;
import watchcheck.db.DaoSession;

/**
 * Created by clorenz on 30.12.14.
 */
public class WatchCheckApplication extends Application {

    public DaoSession daoSession;

    @Override
    public void onCreate() {
        super.onCreate();
        setupDatabase();
    }

    private void setupDatabase() {
        WatchCheckOpenHelper helper = new WatchCheckOpenHelper(this, "watchcheck.db", null);
        SQLiteDatabase db = helper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(db);
        daoSession = daoMaster.newSession();
    }

    public DaoSession getDaoSession() {
        return daoSession;
    }
}
