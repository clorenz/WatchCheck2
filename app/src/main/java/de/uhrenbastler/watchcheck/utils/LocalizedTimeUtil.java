package de.uhrenbastler.watchcheck.utils;

import android.content.Context;
import android.text.format.DateUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.Date;

/**
 * Created by clorenz on 23.01.16.
 */
public class LocalizedTimeUtil {

    public static String getTime(Context context, Date date) {
        String showTime = DateUtils.formatDateTime(context, date.getTime(),
                DateUtils.FORMAT_SHOW_TIME)+" ";
        showTime = showTime.replaceFirst("\\s",
                (String) android.text.format.DateFormat.format(":ss ", date));

        return StringUtils.trim(showTime);
    }

    public static String getDate(Context context, Date date) {
        return DateUtils.formatDateTime(context, date.getTime(),
            DateUtils.FORMAT_SHOW_DATE|DateUtils.FORMAT_ABBREV_MONTH|DateUtils.FORMAT_SHOW_YEAR);
    }

    public static String getDateAndTime(Context context, Date date) {
        return getDate(context, date) + " " + getTime(context, date);
    }
}
