package de.uhrenbastler.watchcheck.provider;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import de.uhrenbastler.watchcheck.tools.Logger;

/**
 * Created by clorenz on 09.01.15.
 */
// Emulator: telnet localhost 5554
// geo fix 1 1
public class GpsTimeProvider implements ITimeProvider {
    boolean valid=false;
    Date timestamp=null;
    Long offset;
    LocationListener ll;
    LocationManager lm;

    public GpsTimeProvider(LocationManager lm) {
        this.lm = lm;
        ll = new GpsLocationListener();
        this.lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,ll);
    }

    @Override
    public void terminate() {
        lm.removeUpdates(ll);
    }

    @Override
    public String getTime() {
        if ( offset!=null) {
            timestamp = new Date(System.currentTimeMillis()-offset);  // = localtime - localtime + referenceTime
        }
        return valid?sdf.format(timestamp):"--:--:--";
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    @Override
    public boolean isGps() {
        return true;
    }

    @Override
    public boolean isNtp() {
        return false;
    }

    @Override
    public long getMillis() {
        return valid ? System.currentTimeMillis()-offset : -1;
    }


    private class GpsLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {
                offset = System.currentTimeMillis() - location.getTime();
                Logger.debug("GPS offset=" + offset + ": Location=" + new Date(location.getTime()) + " vs. " + new Date());
                valid = true;
            } else {
                offset=null;
                valid = false;
            }
        }

        @Override
        public void onProviderDisabled(String provider) {
            offset=null;
            valid = false;
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            if (status != LocationProvider.AVAILABLE) {
                offset=null;
                valid = false;
            }
        }
    }

    @Override
    public String toString() {
        return "GpsTimeProvider{" +
                "valid=" + valid +
                ", timestamp=" + timestamp +
                ", offset=" + offset +
                '}';
    }
}
