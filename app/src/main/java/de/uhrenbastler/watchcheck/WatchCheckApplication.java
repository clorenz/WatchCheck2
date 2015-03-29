package de.uhrenbastler.watchcheck;

import android.app.Application;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;

import de.uhrenbastler.watchcheck.managers.WatchCheckOpenHelper;
import de.uhrenbastler.watchcheck.reminder.ReminderManager;
import de.uhrenbastler.watchcheck.tools.Logger;
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
        setReminder();
    }


    private void setReminder() {
        if ( !ReminderManager.isAlarmActive(getApplicationContext())) {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            boolean reminderPreferenceExists = sharedPref.contains("pref_reminder");

            boolean alarmEnabled = sharedPref.getBoolean("pref_reminder", false);
            long alarmSchedule = sharedPref.getLong("pref_reminder_time",0);

            // No alarmSchedule was set -> first time behaviour!
            if ( !reminderPreferenceExists) {
                // Set schedule to now and enable pref_reminder - this is the default behaviour!
                alarmSchedule = System.currentTimeMillis();
                sharedPref.edit().putLong("pref_reminder_time",alarmSchedule).putBoolean("pref_reminder",true).apply();
                alarmEnabled=true;
                Logger.info("Alarm initial setup");
            }

            if ( alarmEnabled ) {
                if ( alarmSchedule == 0 ) {
                    // Should not happen, but is possible. Set time to "now"
                    alarmSchedule = System.currentTimeMillis();
                    sharedPref.edit().putLong("pref_reminder_time",alarmSchedule).apply();
                    Logger.info("Alarm was enabled, but reminder time was unset. Setting to now");
                }
                ReminderManager.setAlarm(getApplicationContext(), alarmSchedule, !reminderPreferenceExists, false);
            } else {
                Logger.info("No alarm to schedule. User actively disabled feature");
            }
        } else {
            Logger.info("Alarm is already active");
        }
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
