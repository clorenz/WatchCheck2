package de.uhrenbastler.watchcheck.preferences;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.AttributeSet;

import de.uhrenbastler.watchcheck.R;
import de.uhrenbastler.watchcheck.tools.Logger;

/**
 * Created by clorenz on 23.03.15.
 */
public class RingtonePreference extends android.preference.RingtonePreference {

    @TargetApi(21)
    public RingtonePreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public RingtonePreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public RingtonePreference(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.ringtonePreferenceStyle);
    }

    public RingtonePreference(Context context) {
        this(context, null);
    }

    @Override
    public CharSequence getSummary() {
        Uri ringtoneUri = Uri.parse(getPersistedString("..."));
        Ringtone ringtone = RingtoneManager.getRingtone(getContext(), ringtoneUri);
        Logger.info("Ringtone "+ringtoneUri+"="+ringtone.getTitle(getContext()));
        return ringtone.getTitle(getContext());
    }
}
