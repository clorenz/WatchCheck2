package de.uhrenbastler.watchcheck.preferences;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.preference.CheckBoxPreference;
import android.util.AttributeSet;

import de.uhrenbastler.watchcheck.reminder.OnDataChangeAlarmScheduler;
import de.uhrenbastler.watchcheck.reminder.ReminderReceiver;
import de.uhrenbastler.watchcheck.tools.Logger;

/**
 * Created by clorenz on 23.03.15.
 */
public class ReminderCheckboxPreference extends CheckBoxPreference implements OnDataChangeAlarmScheduler {


    public ReminderCheckboxPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(21)
    public ReminderCheckboxPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public ReminderCheckboxPreference(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.checkBoxPreferenceStyle);
    }

    public ReminderCheckboxPreference(Context context) {
        this(context, null);
    }

    @Override
    protected void onClick() {
        super.onClick();
        onSettingDataChanged();
    }


    @Override
    public void onSettingDataChanged() {
        // TODO: Move this into a helper class, which also reads TimepickerPreference. And this class should also
        // change the alarm manager, if the time changes. Maybe use interfaces here?
        Logger.info("Is checked="+isChecked());
        if ( isChecked() ) {
            Intent intent = new Intent(getContext(), ReminderReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), 0, intent, 0);
            AlarmManager am = (AlarmManager)getContext().getSystemService(Activity.ALARM_SERVICE);
            am.setRepeating(am.RTC_WAKEUP, System.currentTimeMillis() + (60 * 1000), 60 * 1000, pendingIntent);         // am.INTERVAL_DAY
            Logger.info("Setting alarmmanager");
        } else {
            Intent intent = new Intent(getContext(), ReminderReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), 0, intent, 0);
            AlarmManager am = (AlarmManager)getContext().getSystemService(Activity.ALARM_SERVICE);
            am.cancel(pendingIntent);
            Logger.info("Cancelled intent");
        }
    }
}
