package de.uhrenbastler.watchcheck.reminder;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import de.uhrenbastler.watchcheck.R;
import de.uhrenbastler.watchcheck.tools.Logger;

/**
 * Created by clorenz on 24.03.15.
 */
public class ReminderManager {

    public static final int REQUEST_CODE = 3733713;
    private static SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

    public static void cancelAlarm(Context context) {
        cancelAlarm(context, true);
    }

    public static void cancelAlarm(Context context, boolean showToast) {
        Intent intent = new Intent(context, ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager)context.getSystemService(Activity.ALARM_SERVICE);
        am.cancel(pendingIntent);
        if ( showToast ) {
            Toast.makeText(context, context.getString(R.string.cancel_reminder), Toast.LENGTH_SHORT).show();
        }
    }


    public static void setAlarm(Context context, long alarmTimeInMillis) {
        long timeInMillis = getFirstAlarmMillis(alarmTimeInMillis);

        Logger.info("Setting alarm to "+new Date(timeInMillis));
        Intent intent = new Intent(context, ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager)context.getSystemService(Activity.ALARM_SERVICE);
        am.setRepeating(am.RTC_WAKEUP, timeInMillis, am.INTERVAL_DAY, pendingIntent);
        String timeString = sdf.format(timeInMillis);
        Toast.makeText(context,String.format(context.getString(R.string.set_reminder), timeString),Toast.LENGTH_SHORT).show();
    }


    public static boolean isAlarmActive(Context context) {
        Intent intent = new Intent(context, ReminderReceiver.class);
        return (PendingIntent.getBroadcast(context, REQUEST_CODE,
                intent,
                PendingIntent.FLAG_NO_CREATE) != null);
    }

    public static long getFirstAlarmMillis(long timeInMillis) {
        Calendar alarmTime = new GregorianCalendar();
        alarmTime.setTimeInMillis(timeInMillis);

        Calendar firstAlarm = new GregorianCalendar();
        // Set day = today and hour/minute = hour/minute of alarm
        firstAlarm.set(Calendar.HOUR_OF_DAY,alarmTime.get(Calendar.HOUR_OF_DAY));
        firstAlarm.set(Calendar.MINUTE,alarmTime.get(Calendar.MINUTE));
        firstAlarm.set(Calendar.SECOND,0);
        firstAlarm.set(Calendar.MILLISECOND,0);

        // If firstAlarm is in the past (same day), add one day for the next schedule
        if ( firstAlarm.before(new GregorianCalendar())) {
            firstAlarm.add(Calendar.HOUR_OF_DAY, 24);
        }
        return firstAlarm.getTimeInMillis();
    }

    public static void cancelAlarmSilently(Context context) {
        cancelAlarm(context, false);
    }
}
