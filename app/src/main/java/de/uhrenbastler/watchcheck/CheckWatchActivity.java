package de.uhrenbastler.watchcheck;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import de.uhrenbastler.watchcheck.provider.GpsTimeProvider;
import de.uhrenbastler.watchcheck.provider.ITimeProvider;
import de.uhrenbastler.watchcheck.provider.NtpTimeProvider;
import de.uhrenbastler.watchcheck.tools.Logger;
import watchcheck.db.Log;
import watchcheck.db.Watch;

/**
 * Created by clorenz on 09.01.15.
 */
public class CheckWatchActivity extends Activity {

    public static final String EXTRA_WATCH = "watch";
    public static final String EXTRA_LAST_LOG = "last_log";
    Watch currentWatch;
    Log lastLog;
    TimePicker timePicker;
    AsyncTask<Context, Integer, Integer> referenceTimeUpdater;
    ITimeProvider gpsTimeProvider;
    ITimeProvider ntpTimeProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.check_watch);

        currentWatch = (Watch) getIntent().getSerializableExtra(EXTRA_WATCH);
        lastLog = (Log) getIntent().getSerializableExtra(EXTRA_LAST_LOG);

        timePicker = (TimePicker) findViewById(R.id.timePicker);

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        ntpTimeProvider = new NtpTimeProvider(cm,50,6000);            // TODO: The last one shall be 6000 or so  (10 per second => 10 minutes)

        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        gpsTimeProvider = new GpsTimeProvider(lm);

        Button btnMeasure = (Button) findViewById(R.id.buttonMeasure);
        btnMeasure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long referenceTime = getReferenceTimeMillis();
                long objectTime = getMillisFromTimePicker(timePicker);
                referenceTimeUpdater.cancel(true);
                Intent addLogIntent = new Intent(CheckWatchActivity.this,AddLogActivity.class);
                addLogIntent.putExtra(AddLogActivity.EXTRA_WATCH, currentWatch);
                addLogIntent.putExtra(AddLogActivity.EXTRA_REFERENCE_TIME, referenceTime);
                addLogIntent.putExtra(AddLogActivity.EXTRA_LOG_TIME, objectTime);
                addLogIntent.putExtra(AddLogActivity.EXTRA_LAST_LOG, lastLog);
                startActivity(addLogIntent);
                CheckWatchActivity.this.finish();
            }
        });
    }

    private long getReferenceTimeMillis() {
        ITimeProvider[] timeProviders = new ITimeProvider[]{gpsTimeProvider,ntpTimeProvider};
        long referenceMillis=-1;

        for ( ITimeProvider timeProvider : timeProviders) {
            // We prefer GPS. Only if no GPS, use other time providers, if they provide valid data
            if ( timeProvider.isValid() && ( timeProvider.isGps() || (referenceMillis==-1))) {
                referenceMillis = timeProvider.getMillis();
            }
        }
        return referenceMillis;
    }


    @Override
    public void onResume() {
        super.onResume();

        referenceTimeUpdater = new ReferenceTimeUpdater(new ITimeProvider[]{gpsTimeProvider,ntpTimeProvider},
                new Integer[]{R.id.gpstime,R.id.ntptime}, timePicker);

        referenceTimeUpdater.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        timePicker.setIs24HourView(true);
        timePicker.setKeepScreenOn(true);

        TextView tvLastDeviation= (TextView) findViewById(R.id.lastdeviation);

        // Set timepicker to next minute PLUS last known deviation
        GregorianCalendar now = new GregorianCalendar();
        now.add(Calendar.MINUTE, 1);
        if ( lastLog != null) {
            Logger.debug("LastLog="+lastLog.getWatchTime()+" vs "+lastLog.getReferenceTime());
            int deviation = (int) (lastLog.getWatchTime().getTime() - lastLog.getReferenceTime().getTime()) / 1000;
            now.add(Calendar.SECOND, deviation);

            tvLastDeviation.setText(String.format(tvLastDeviation.getText().toString(), ((deviation > 0 ? "+" : "") + deviation + " sec.")));
        } else {
            tvLastDeviation.setText(String.format(tvLastDeviation.getText().toString(),"-"));
        }
        timePicker.setCurrentHour(now.get(Calendar.HOUR_OF_DAY));
        timePicker.setCurrentMinute(now.get(Calendar.MINUTE));


    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        referenceTimeUpdater.cancel(true);
    }


    private long getMillisFromTimePicker(TimePicker timePicker) {
        GregorianCalendar pickerTime = new GregorianCalendar();
        pickerTime.set(Calendar.HOUR_OF_DAY, timePicker.getCurrentHour());
        pickerTime.set(Calendar.MINUTE, timePicker.getCurrentMinute());
        pickerTime.set(Calendar.SECOND,0);
        pickerTime.set(Calendar.MILLISECOND,0);

        return pickerTime.getTime().getTime();
    }
    

    // ------------------------------------------------------------------
    private class ReferenceTimeUpdater extends AsyncTask<Context, Integer, Integer> {

        boolean runnable=true;
        TextView[] tvTime;
        String[] timestamp;
        String[] format;
        String formatTvDeviation;
        TextView tvDeviation;
        ITimeProvider[] timeProviders;
        TimePicker timepicker;
        int colorRed;
        int colorGreen;
        double deviation = Double.MIN_VALUE;
        boolean valid=false;
        Button btnMeasure;

        ReferenceTimeUpdater(ITimeProvider[] timeProviders, Integer[] viewIds, TimePicker timepicker) {
            int number=0;
            timestamp = new String[timeProviders.length];
            tvTime = new TextView[timeProviders.length];
            format = new String[timeProviders.length];
            this.timeProviders = timeProviders;

            for ( Integer viewId: viewIds) {
                tvTime[number] = (TextView) findViewById(viewId);
                format[number] = tvTime[number].getText().toString();
                number++;
            }

            this.timepicker = timepicker;

            colorRed=getResources().getColor(R.color.measure_red);
            colorGreen=getResources().getColor(R.color.measure_green);

            tvDeviation = (TextView) findViewById(R.id.currentdeviation);
            formatTvDeviation = tvDeviation.getText().toString();

            btnMeasure = (Button) findViewById(R.id.buttonMeasure);

            Logger.info("Starting reference time update. Runnable="+runnable);
        }

        @Override
        protected void onPostExecute(Integer result) {
            runnable=false;
            Logger.info("Killed reference time display of "+timeProviders);
        }

        @Override
        protected void onCancelled() {
            runnable=false;
            Logger.info("Killed reference time display of "+timeProviders);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            int number=0;
            for ( String fmt: format) {
                tvTime[number].setText(String.format(format[number], timestamp[number]));
                number++;
            }
            if ( deviation > -1440 && deviation < 1440) {
                tvDeviation.setText(String.format(formatTvDeviation, deviation));
            } else {
                tvDeviation.setText(getString(R.string.no_current_deviation));
            }
            timepicker.setBackgroundColor(valid ? colorGreen : colorRed);
            btnMeasure.setEnabled(valid);
        }

        @Override
        protected Integer doInBackground(Context... params) {
            Logger.info(this.hashCode()+": Runnable="+runnable);

            int progress=0;

            while ( runnable ) {
                int number=0;
                
                boolean localValid=false;
                long referenceMillis=-1;

                for ( ITimeProvider timeProvider : timeProviders) {
                    timestamp[number++] = timeProvider.getTime();
                    localValid |= timeProvider.isValid();

                    // We prefer GPS. Only if no GPS, use other time providers, if they provide valid data
                    if ( timeProvider.isValid() && ( timeProvider.isGps() || (referenceMillis==-1))) {
                        referenceMillis = timeProvider.getMillis();
                    }
                }

                valid = localValid;
                deviation = (((double)getMillisFromTimePicker(timePicker) - (double)referenceMillis )/1000);
                publishProgress((++progress % 2));
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignore) {};
            }

            return null;
        }


    }
}
