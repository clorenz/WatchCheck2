package de.uhrenbastler.watchcheck;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.Display;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.gc.materialdesign.views.ButtonRectangle;

import java.util.Calendar;
import java.util.GregorianCalendar;

import de.uhrenbastler.watchcheck.provider.GpsTimeProvider;
import de.uhrenbastler.watchcheck.provider.ITimeProvider;
import de.uhrenbastler.watchcheck.provider.NtpTimeProvider;
import de.uhrenbastler.watchcheck.tools.Logger;
import watchcheck.db.Log;
import watchcheck.db.Watch;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by clorenz on 09.01.15.
 */
public class CheckWatchActivity extends WatchCheckActionBarActivity {

    public static final String EXTRA_WATCH = "watch";
    public static final String EXTRA_LAST_LOG = "last_log";
    Watch currentWatch;
    Log lastLog;
    TimePicker timePicker;
    AsyncTask<Context, Integer, Integer> referenceTimeUpdater;
    ITimeProvider gpsTimeProvider;
    ITimeProvider ntpTimeProvider;
    LocationManager lm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.check_watch);

        setTitle(R.string.measure_watch);

        currentWatch = (Watch) getIntent().getSerializableExtra(EXTRA_WATCH);

        setWatchName(currentWatch);

        lastLog = (Log) getIntent().getSerializableExtra(EXTRA_LAST_LOG);

        timePicker = (TimePicker) findViewById(R.id.timePicker);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        if ( size.y < 1024) {
            // TimePicker must be smaller
            timePicker.setScaleX(0.75f);
            timePicker.setScaleY(0.75f);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.gravity = Gravity.CENTER;
            layoutParams.setMargins(0, -20, 0, 0);
            timePicker.setLayoutParams(layoutParams);
        }

        ButtonRectangle btnMeasure = (ButtonRectangle) findViewById(R.id.buttonMeasure);
        btnMeasure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long referenceTime = getReferenceTimeMillis();
                long objectTime = getMillisFromTimePicker(timePicker);
                referenceTimeUpdater.cancel(true);
                ((ReferenceTimeUpdater)referenceTimeUpdater).terminate();
                Intent addLogIntent = new Intent(CheckWatchActivity.this,AddLogActivity.class);
                addLogIntent.putExtra(AddLogActivity.EXTRA_WATCH, currentWatch);
                addLogIntent.putExtra(AddLogActivity.EXTRA_REFERENCE_TIME, referenceTime);
                addLogIntent.putExtra(AddLogActivity.EXTRA_LOG_TIME, objectTime);
                addLogIntent.putExtra(AddLogActivity.EXTRA_LAST_LOG, lastLog);
                Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(100);
                startActivity(addLogIntent);
                CheckWatchActivity.this.finish();
            }
        });

        // Set timepicker to next minute PLUS last known deviation
        timePicker.setIs24HourView(true);
        timePicker.setKeepScreenOn(true);

        TextView tvLastDeviation= (TextView) findViewById(R.id.lastdeviation);
        GregorianCalendar now = new GregorianCalendar();
        now.add(Calendar.MINUTE, 1);
        if ( lastLog != null) {
            Logger.debug("LastLog="+lastLog.getWatchTime()+" vs "+lastLog.getReferenceTime());
            double deviation = (double) (lastLog.getWatchTime().getTime() - lastLog.getReferenceTime().getTime()) / 1000d;
            now.add(Calendar.SECOND, (int)deviation);

            tvLastDeviation.setText(String.format(tvLastDeviation.getText().toString(), deviation));
        } else {
            tvLastDeviation.setText(getString(R.string.no_last_deviation));
        }
        timePicker.setCurrentHour(now.get(Calendar.HOUR_OF_DAY));
        timePicker.setCurrentMinute(now.get(Calendar.MINUTE));

    }

    private long getReferenceTimeMillis() {
        ITimeProvider[] timeProviders = new ITimeProvider[]{gpsTimeProvider,ntpTimeProvider};
        long referenceMillis=-1;

        for ( ITimeProvider timeProvider : timeProviders) {
            // We prefer NTP. Only if no NTP, use other time providers, if they provide valid data
            if ( timeProvider.isValid() && ( timeProvider.isNtp() || (referenceMillis==-1))) {
                referenceMillis = timeProvider.getMillis();
            }
        }
        return referenceMillis;
    }


    @Override
    public void onResume() {
        super.onResume();

        Logger.debug("On Resume");

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        ntpTimeProvider = new NtpTimeProvider(cm,50,6000);            // TODO: The last one shall be 6000 or so  (10 per second => 10 minutes)
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        gpsTimeProvider = new GpsTimeProvider(lm);


        referenceTimeUpdater = new ReferenceTimeUpdater(new ITimeProvider[]{gpsTimeProvider,ntpTimeProvider},
                new Integer[]{R.id.gpstime,R.id.ntptime},
                new int[]{R.string.gps_time,R.string.ntp_time},timePicker);
        referenceTimeUpdater.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    protected void onPause() {
        referenceTimeUpdater.cancel(true);
        ((ReferenceTimeUpdater)referenceTimeUpdater).terminate();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        referenceTimeUpdater.cancel(true);
        ((ReferenceTimeUpdater)referenceTimeUpdater).terminate();
        super.onDestroy();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        referenceTimeUpdater.cancel(true);
        ((ReferenceTimeUpdater)referenceTimeUpdater).terminate();
        return super.onOptionsItemSelected(item);
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
        ButtonRectangle btnMeasure;

        ReferenceTimeUpdater(ITimeProvider[] timeProviders, Integer[] viewIds, int[] defaultResources, TimePicker timepicker) {
            int number=0;
            timestamp = new String[timeProviders.length];
            tvTime = new TextView[timeProviders.length];
            format = new String[timeProviders.length];
            this.timeProviders = timeProviders;

            for ( Integer viewId: viewIds) {
                tvTime[number] = (TextView) findViewById(viewId);
                format[number] = getString(defaultResources[number]);
                number++;
            }

            this.timepicker = timepicker;

            colorRed=getResources().getColor(R.color.measure_red);
            colorGreen=getResources().getColor(R.color.measure_green);

            tvDeviation = (TextView) findViewById(R.id.currentdeviation);
            formatTvDeviation = getString(R.string.current_deviation);

            btnMeasure = (ButtonRectangle) findViewById(R.id.buttonMeasure);

            runnable=true;

            Logger.info("Starting reference time update. Runnable="+runnable);
        }

        @Override
        protected void onPostExecute(Integer result) {
            runnable=false;
            terminateAllProvider();
            Logger.info("Killed reference time display of "+ArrayUtils.toString(timeProviders));
        }

        private void terminateAllProvider() {
            for ( ITimeProvider timeProvider :  timeProviders ) {
                timeProvider.terminate();
            }
        }

        @Override
        protected void onCancelled() {
            runnable=false;
            terminateAllProvider();
            Logger.info("Killed reference time display of "+ArrayUtils.toString(timeProviders));
        }


        public void terminate() {
            runnable=false;
            terminateAllProvider();
            Logger.info("Killed reference time display of "+ArrayUtils.toString(timeProviders));
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            int number=0;
            for ( String fmt: format) {
                tvTime[number].setText(String.format(format[number], timestamp[number]));
                number++;
            }
            if ( deviation > -86400 && deviation < 86400) {
                tvDeviation.setText(String.format(formatTvDeviation, deviation));
            } else {
                tvDeviation.setText(getString(R.string.no_current_deviation));
            }
            btnMeasure.setBackgroundColor(valid ? colorGreen : colorRed);
            //timepicker.setBackgroundColor(valid ? colorGreen : colorRed);
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

            Logger.info("Terminated background task");

            return null;
        }


    }
}
