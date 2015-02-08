package de.uhrenbastler.watchcheck.provider;

import java.text.SimpleDateFormat;

/**
 * Created by clorenz on 09.01.15.
 */
public interface ITimeProvider {
    final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

    String getTime();
    boolean isValid();
    boolean isGps();
    boolean isNtp();
    long getMillis();
    void terminate();
}
