package de.uhrenbastler.watchcheck.reminder;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import java.text.SimpleDateFormat;

import de.uhrenbastler.watchcheck.MainActivity;
import de.uhrenbastler.watchcheck.R;
import de.uhrenbastler.watchcheck.tools.Logger;

/**
 * Created by clorenz on 23.03.15.
 */
public class ReminderReceiver extends BroadcastReceiver {

    private static SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

    @Override
    public void onReceive(Context context, Intent intent) {
        if ( Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            rescheduleAlarm(context);
        } else {
            showNotification(context);
        }
    }

    private void rescheduleAlarm(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        boolean reminderPreferenceExists = sharedPref.contains("pref_reminder");
        boolean alarmEnabled = sharedPref.getBoolean("pref_reminder", false);
        long alarmSchedule = sharedPref.getLong("pref_reminder_time",0);

        if ( reminderPreferenceExists && alarmEnabled ) {
            ReminderManager.setAlarm(context, alarmSchedule, false, false);
            String timeString = sdf.format(alarmSchedule);
            Logger.info("Received boot completed intent; scheduling reminder to "+timeString);
        }
    }

    private void showNotification(Context context) {
        AlarmManager am = (AlarmManager)context.getSystemService(Activity.ALARM_SERVICE);
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(context, ReminderManager.REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(context.getString(R.string.notification_text));
        mBuilder.setContentIntent(pi);
        mBuilder.setDefaults(Notification.DEFAULT_ALL);
        mBuilder.setAutoCancel(true);
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(ReminderManager.REQUEST_CODE, mBuilder.build());
    }
}
