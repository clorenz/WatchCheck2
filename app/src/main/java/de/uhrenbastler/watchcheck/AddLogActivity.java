package de.uhrenbastler.watchcheck;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.gc.materialdesign.views.ButtonRectangle;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;

import de.uhrenbastler.watchcheck.tools.Logger;
import watchcheck.db.Log;
import watchcheck.db.LogDao;
import watchcheck.db.Watch;
import watchcheck.db.WatchDao;

/**
 * Created by clorenz on 13.01.15.
 */
public class AddLogActivity extends ActionBarActivity {
    private static final String[] POSITIONARR = { "","DU","DD","12U","3U","6U","9U" };
    private static final int[] TEMPARR = { -273, 4, 20, 36 };
    private Spinner positionSpinner;
    private Spinner temperatureSpinner;
    private CheckBox startFlag;
    private ButtonRectangle saveButton;
    private EditText comment;

    private Log lastLog;
    private Log editLog;


    public static final String EXTRA_WATCH = "watch";
    public static final String EXTRA_REFERENCE_TIME = "reference_time";
    public static final String EXTRA_LOG_TIME = "log_time";
    public static final String EXTRA_LAST_LOG = "last_log";     // To pre-fill the spinners of temperature and position
    public static final String EXTRA_EDIT_LOG = "edit_log";     // current log to be edited

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WatchDao watchDao = ((WatchCheckApplication)getApplicationContext()).getDaoSession().getWatchDao();

        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.add_log);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
        getSupportActionBar().setTitle(R.string.add_log);

        lastLog = (Log) getIntent().getSerializableExtra(EXTRA_LAST_LOG);

        editLog = (Log) getIntent().getSerializableExtra(EXTRA_EDIT_LOG);

        final long currentWatchId = (editLog!=null?editLog.getWatchId():((Watch) getIntent().getSerializableExtra(EXTRA_WATCH)).getId());
        final long referenceTime = editLog!=null ? editLog.getReferenceTime().getTime() : getIntent().getLongExtra(EXTRA_REFERENCE_TIME, -1);
        final long watchTime = editLog!=null ? editLog.getWatchTime().getTime() : getIntent().getLongExtra(EXTRA_LOG_TIME, -1);

        double deviation = (double)(watchTime - referenceTime)/1000d;

        String deviationFormat = ((TextView) findViewById(R.id.logDeviation)).getText().toString();
        ((TextView) findViewById(R.id.logDeviation)).setText(String.format(deviationFormat, deviation));

        Watch currentWatch = watchDao.load(currentWatchId);
        ((TextView) findViewById(R.id.logWatchName)).setText(currentWatch.getName());
        if ( !StringUtils.isBlank(currentWatch.getSerial())) {
            ((TextView) findViewById(R.id.logWatchSerial)).setText(currentWatch.getSerial());
        } else {
            ((TextView) findViewById(R.id.logWatchSerial)).setVisibility(View.INVISIBLE);
        }

        saveButton = (ButtonRectangle) findViewById(R.id.buttonSave);
        if ( editLog!=null ) {
            saveButton.setText(getString(R.string.button_update));
        }
        comment = (EditText) findViewById(R.id.logComment);

        prefillForm(editLog != null ? editLog : lastLog, editLog != null);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( editLog!=null) {
                    updateLogEntry(editLog);
                } else {
                    createNewLogEntry(currentWatchId, referenceTime, watchTime);
                }
                finish();
            }
        });
    }

    private void createNewLogEntry(long currentWatchId, long referenceTime, long watchTime) {
        int period = 0;
        if (lastLog != null) {
            period = startFlag.isChecked() ? lastLog.getPeriod() + 1 : lastLog.getPeriod();
        }
        createLog(currentWatchId, period,
                referenceTime, watchTime,
                POSITIONARR[(int) positionSpinner.getSelectedItemId()],
                TEMPARR[(int) temperatureSpinner.getSelectedItemId()],
                comment.getEditableText().toString());
    }

    private void updateLogEntry(Log editLog) {
        updateLog(editLog.getWatchId(), editLog.getId(), editLog.getPeriod(), editLog.getReferenceTime().getTime(),
                editLog.getWatchTime().getTime(),
                POSITIONARR[(int) positionSpinner.getSelectedItemId()],
                TEMPARR[(int) temperatureSpinner.getSelectedItemId()],
                comment.getEditableText().toString());
    }


    private void createLog(long watchId, int period, long referenceTime, long watchTime,
                           String position, int temperature, String comment) {
        LogDao logDao = ((WatchCheckApplication)getApplicationContext()).getDaoSession().getLogDao();
        Log log = new Log(null, watchId, period, new Date(referenceTime), new Date(watchTime),
                position, temperature, comment);
        logDao.insert(log);
        Logger.info("Added log "+log);
    }

    private void updateLog(long watchId, long logId, int period, long referenceTime, long watchTime,
                                String position, int temperature, String comment) {
        LogDao logDao = ((WatchCheckApplication)getApplicationContext()).getDaoSession().getLogDao();
        Log log = new Log(logId, watchId, period, new Date(referenceTime), new Date(watchTime),
                position, temperature, comment);
        logDao.update(log);
        Logger.info("Updated log "+log);
    }


    private void prefillForm(Log lastLog, boolean hideStartflag) {
        positionSpinner = (Spinner) findViewById(R.id.logSpinnerPosition);
        ArrayAdapter<?> positionAdapter = ArrayAdapter.createFromResource( getApplicationContext(),
                        R.array.positions,android.R.layout.simple_spinner_item);
        positionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        positionSpinner.setAdapter(positionAdapter);

        if ( lastLog!=null && lastLog.getPosition()!=null)
                positionSpinner.setSelection(ArrayUtils.indexOf(POSITIONARR, lastLog.getPosition()));
        else
                positionSpinner.setSelection(0);

        temperatureSpinner = (Spinner) findViewById(R.id.logSpinnerTemperature);
        ArrayAdapter<?> temperatureAdapter = ArrayAdapter.createFromResource( getApplicationContext(),
                R.array.temperatures,android.R.layout.simple_spinner_item);
        temperatureAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        temperatureSpinner.setAdapter(temperatureAdapter);

        if ( lastLog!=null  )
            temperatureSpinner.setSelection(ArrayUtils.indexOf(TEMPARR, lastLog.getTemperature()));
        else
            temperatureSpinner.setSelection(0);

        startFlag = (CheckBox) findViewById(R.id.logCheckBoxNewPeriod);
        startFlag.setChecked(lastLog==null);
        startFlag.setEnabled(lastLog!=null);
        startFlag.setVisibility(hideStartflag?View.INVISIBLE:View.VISIBLE);

        if ( editLog!=null ) {
            TextView startFlagLabel = (TextView) findViewById(R.id.textViewNewPeriod);
            startFlagLabel.setVisibility(View.INVISIBLE);
            startFlag.setVisibility(View.INVISIBLE);
        }

        Logger.debug("startflag: "+startFlag.getVisibility());

    }
}
