package de.uhrenbastler.watchcheck.preferences;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.util.AttributeSet;

import de.uhrenbastler.watchcheck.R;
import de.uhrenbastler.watchcheck.reminder.OnDataChangeAlarmScheduler;
import de.uhrenbastler.watchcheck.reminder.ReminderManager;

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
        if (isChecked()) {
            Preference reminderTimepicker = findPreferenceInHierarchy("pref_reminder_time");
            ReminderManager.setAlarm(getContext(), ((TimepickerPreference) reminderTimepicker).getTime(), false, true);
        } else {
            ReminderManager.cancelAlarm(getContext());
        }
    }
}
