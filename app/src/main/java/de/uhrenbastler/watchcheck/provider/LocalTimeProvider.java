package de.uhrenbastler.watchcheck.provider;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by clorenz on 09.01.15.
 */
public class LocalTimeProvider implements ITimeProvider{

    @Override
    public String getTime() {
        return sdf.format(new Date());
    }

    @Override
    public boolean isValid() {
        return false;
    }

    @Override
    public boolean isGps() {
        return false;
    }

    @Override
    public long getMillis() {
        return System.currentTimeMillis();
    }
}
