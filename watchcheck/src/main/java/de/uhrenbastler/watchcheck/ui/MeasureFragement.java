package de.uhrenbastler.watchcheck.ui;

import android.app.Fragment;
import android.content.Context;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.TimePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import de.uhrenbastler.watchcheck.R;
import de.uhrenbastler.watchcheck.provider.GpsTimeProvider;

/**
 * Created by clorenz on 07.10.14.
 */
public class MeasureFragement extends Fragment {

    TimePicker watchtimePicker;
    AsyncTask<Context, Integer, Integer> updateReferenceTime;
    LocationManager lm;

    public MeasureFragement() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.measure, container, false);

        watchtimePicker = (TimePicker) view.findViewById(R.id.timePicker);
        watchtimePicker.setIs24HourView(true);
        watchtimePicker.setEnabled(false);                  // as long, as we don't have NTP or GPS data

        updateReferenceTime = new UpdateReferenceTime(
                (TextView) view.findViewById(R.id.referencetime),
                (TextView) view.findViewById(R.id.referencetimesource));
        updateReferenceTime.execute(view.getContext());

        return view;
    }


    public void setLocationManager(LocationManager lm) {
        if ( updateReferenceTime != null) {
            ((UpdateReferenceTime)updateReferenceTime).setLocationManager(lm);
        }
    }


    @Override
    public void onResume() {
        super.onResume();

        //updateReferenceTime = new UpdateReferenceTime();
        //updateReferenceTime.execute(this);
    }



    private class UpdateReferenceTime extends AsyncTask<Context, Integer, Integer> {
        final TextView referenceTimeTextView;
        final TextView referenceTimeSourceTextView;
        boolean runnable=true;
        String referenceTimeString;
        String referenceTimeSourceString;
        int progress=0;
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        GpsTimeProvider gpsTimeProvider = new GpsTimeProvider();

        UpdateReferenceTime(TextView referenceTimeTextView, TextView referenceTimeSourceTextView) {
            super();
            this.referenceTimeTextView = referenceTimeTextView;
            this.referenceTimeSourceTextView = referenceTimeSourceTextView;
        }


        public void setLocationManager(LocationManager lm) {
            gpsTimeProvider.setLocationManager(lm);
        }

        @Override
        protected void onCancelled(Integer result) {
            runnable=false;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            referenceTimeTextView.setText(referenceTimeString);
            referenceTimeSourceTextView.setText(referenceTimeSourceString);
        }

        @Override
        protected Integer doInBackground(Context... params) {
            while ( runnable ) {
                Calendar referenceTime = null;
                referenceTimeSourceString = getResources().getString(R.string.timeSourceNone);

                if ( gpsTimeProvider.isValid()) {
                    referenceTime = gpsTimeProvider.getTime();
                    referenceTimeSourceString = getResources().getString(R.string.timeSourceGps);
                } else {
                    // referenceTime = new GregorianCalendar();
                }

                if ( referenceTime != null ) {
                    referenceTimeString = String.format(getResources().getString(R.string.referenceTime),
                            String.format("%02d", referenceTime.get(Calendar.HOUR_OF_DAY)),
                            String.format("%02d", referenceTime.get(Calendar.MINUTE)),
                            String.format("%02d", referenceTime.get(Calendar.SECOND)));
                } else {
                    referenceTimeString=String.format(getResources().getString(R.string.referenceTime),
                            "--","--","--");
                }
                publishProgress((++progress % 2));

                try {
                    Thread.sleep(333);
                } catch (InterruptedException ignore) {
                }
            }

            return null;
        }


    }
}
