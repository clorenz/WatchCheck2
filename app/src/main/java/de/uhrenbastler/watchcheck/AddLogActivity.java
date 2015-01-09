package de.uhrenbastler.watchcheck;

import android.app.Activity;
import android.content.Context;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;
import android.widget.TimePicker;

import de.uhrenbastler.watchcheck.provider.GpsTimeProvider;
import de.uhrenbastler.watchcheck.provider.ITimeProvider;
import de.uhrenbastler.watchcheck.provider.LocalTimeProvider;
import de.uhrenbastler.watchcheck.provider.NtpTimeProvider;
import de.uhrenbastler.watchcheck.tools.Logger;

/**
 * Created by clorenz on 09.01.15.
 */
public class AddLogActivity extends Activity {

    TimePicker timePicker;
    AsyncTask<Context, Integer, Integer> referenceTimeUpdater;
    ITimeProvider gpsTimeProvider;
    ITimeProvider ntpTimeProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.add_log);

        timePicker = (TimePicker) findViewById(R.id.timePicker);
        timePicker.setIs24HourView(true);
        timePicker.setKeepScreenOn(true);

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        ntpTimeProvider = new NtpTimeProvider(cm,50,6000);            // TODO: The last one shall be 6000 or so  (10 per second => 10 minutes)

        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        gpsTimeProvider = new GpsTimeProvider(lm);
    }


    @Override
    public void onResume() {
        super.onResume();

        referenceTimeUpdater = new ReferenceTimeUpdater(new ITimeProvider[]{gpsTimeProvider,ntpTimeProvider},
                new Integer[]{R.id.gpstime,R.id.ntptime}, timePicker);

        referenceTimeUpdater.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        referenceTimeUpdater.cancel(true);
    }
    

    // ------------------------------------------------------------------
    private class ReferenceTimeUpdater extends AsyncTask<Context, Integer, Integer> {

        boolean runnable=true;
        TextView[] tvTime;
        String[] timestamp;
        String[] format;
        ITimeProvider[] timeProviders;
        TimePicker timepicker;
        int colorRed;
        int colorGreen;
        boolean valid=false;

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
            timepicker.setBackgroundColor(valid?colorGreen:colorRed);
        }

        @Override
        protected Integer doInBackground(Context... params) {
            Logger.info(this.hashCode()+": Runnable="+runnable);

            int progress=0;

            while ( runnable ) {
                int number=0;
                valid=false;
                for ( ITimeProvider timeProvider : timeProviders) {
                    timestamp[number++] = timeProvider.getTime();
                    valid |= timeProvider.isValid();
                }
                publishProgress((++progress % 2));
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignore) {};
            }

            return null;
        }
    }
}
