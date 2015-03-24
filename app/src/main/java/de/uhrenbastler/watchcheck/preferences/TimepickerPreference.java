package de.uhrenbastler.watchcheck.preferences;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.preference.CheckBoxPreference;
import android.preference.DialogPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.prefs.Preferences;

import de.uhrenbastler.watchcheck.R;
import de.uhrenbastler.watchcheck.reminder.OnDataChangeAlarmScheduler;
import de.uhrenbastler.watchcheck.reminder.ReminderManager;
import de.uhrenbastler.watchcheck.tools.Logger;

/**
 * Created by clorenz on 23.03.15.
 */
public class TimepickerPreference extends DialogPreference implements OnDataChangeAlarmScheduler {

    private TimePicker picker=null;
    private Calendar calendar;

    @TargetApi(21)
    public TimepickerPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        calendar = new GregorianCalendar();
    }


    public TimepickerPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        calendar = new GregorianCalendar();
    }

    public TimepickerPreference(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.dialogPreferenceStyle);
    }


    public TimepickerPreference(Context context) {
        this(context, null);
    }


    @Override
    public void onDependencyChanged(Preference dependency, boolean disableDependent) {
        super.onDependencyChanged(dependency, disableDependent);
        setShouldDisableView(disableDependent);
        setEnabled(!disableDependent);

    }

    @Override
    protected View onCreateDialogView() {
        picker = new TimePicker(getContext());
        Locale current = getContext().getResources().getConfiguration().locale;
        if ( Locale.GERMAN.getLanguage().equals(current.getLanguage())) {
            picker.setIs24HourView(true);
        }
        return picker;
    }



    @Override
    protected void onBindDialogView(View v) {
        super.onBindDialogView(v);
        picker.setCurrentHour(calendar.get(Calendar.HOUR_OF_DAY));
        picker.setCurrentMinute(calendar.get(Calendar.MINUTE));
    }


    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return (a.getString(index));
    }


    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {

        Logger.info("setInitial. Restore="+restoreValue+", default="+defaultValue);

        if (restoreValue) {
            if (defaultValue == null) {
                calendar.setTimeInMillis(getPersistedLong(System.currentTimeMillis()));
            } else {
                calendar.setTimeInMillis(Long.parseLong(getPersistedString((String) defaultValue)));
            }
        } else {
            if (defaultValue == null) {
                calendar.setTimeInMillis(System.currentTimeMillis());
                persistLong(calendar.getTime().getTime());
                Logger.info("Persisting "+getPersistedLong(0));
            } else {
                calendar.setTimeInMillis(Long.parseLong((String) defaultValue));
            }
        }
        setSummary(getSummary());
    }


    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            calendar.set(Calendar.HOUR_OF_DAY, picker.getCurrentHour());
            calendar.set(Calendar.MINUTE, picker.getCurrentMinute());

            setSummary(getSummary());
            if (callChangeListener(calendar.getTimeInMillis())) {
                persistLong(calendar.getTimeInMillis());
                onSettingDataChanged();
            }
        }
    }


    @Override
    public CharSequence getSummary() {
        if (calendar == null) {
            return null;
        }

        long time=getPersistedLong(0);

        if ( time > 0 ) {
            return DateFormat.getTimeFormat(getContext()).format(new Date(calendar.getTimeInMillis()));
        } else {
            return getContext().getString(R.string.reminder_time_unset);
        }
    }


    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        // Do we already have a time set? No?
        if ( getPersistedLong(0) == 0) {
            // Check for value of "reminder enabled"
            String reminderEnabledKey = getDependency();
            Preference reminderEnabled = findPreferenceInHierarchy(reminderEnabledKey);
            if (reminderEnabled != null) {
                if (((CheckBoxPreference) reminderEnabled).isChecked()) {
                    calendar.setTime(new Date());
                    persistLong(calendar.getTime().getTime());
                }
            }
        }
    }



    @Override
    public void onSettingDataChanged() {
        String reminderEnabledKey = getDependency();
        Preference reminderEnabled = findPreferenceInHierarchy(reminderEnabledKey);
        if (reminderEnabled != null) {
            if (((CheckBoxPreference) reminderEnabled).isChecked()) {
                ReminderManager.cancelAlarmSilently(getContext());
                ReminderManager.setAlarm(getContext(), getPersistedLong(0));
            } else {
                ReminderManager.cancelAlarm(getContext());
            }
        }
    }

    public long getTime() {
        return getPersistedLong(0);
    }
}
