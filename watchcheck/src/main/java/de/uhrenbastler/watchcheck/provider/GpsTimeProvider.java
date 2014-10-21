package de.uhrenbastler.watchcheck.provider;

import android.app.Fragment;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by clorenz on 21.10.14.
 */
public class GpsTimeProvider {

    // Emulator: telnet localhost 5554
    // geo fix 1 1
    // TODO: If location was retrieved, sync internal clock to GPS clock and from now on always return that synced clock!

    boolean valid=false;
    Calendar calendar = new GregorianCalendar();

    public GpsTimeProvider() {
    }

    public void setLocationManager(LocationManager lm) {
        LocationListener ll = new GpsLocationListener();
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,ll);
    }

    public boolean isValid() {
        return valid;
    }

    public Calendar getTime() {
        return calendar;
    }


    private class GpsLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            if ( location != null) {
                calendar.setTimeInMillis(location.getTime());
                valid=true;
            } else {
                calendar.setTimeInMillis(0);
                valid=false;
            }
        }

        @Override
        public void onProviderDisabled(String provider) {
            calendar.setTimeInMillis(0);
            valid=false;
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            if ( status != LocationProvider.AVAILABLE) {
                calendar.setTimeInMillis(0);
                valid = false;
            }
        }
    }
}
