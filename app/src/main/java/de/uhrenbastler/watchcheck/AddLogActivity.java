package de.uhrenbastler.watchcheck;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.apache.commons.lang3.ArrayUtils;

import java.util.Date;

import de.uhrenbastler.watchcheck.tools.Logger;
import watchcheck.db.Log;
import watchcheck.db.LogDao;
import watchcheck.db.Watch;
import watchcheck.db.WatchDao;

/**
 * Created by clorenz on 13.01.15.
 */
public class AddLogActivity extends Activity {
    private static final String[] POSITIONARR = { "","DU","DD","12U","3U","6U","9U" };
    private static final int[] TEMPARR = { -273, 4, 20, 36 };
    private Spinner positionSpinner;
    private Spinner temperatureSpinner;
    private CheckBox startFlag;
    private Button saveButton;
    private EditText comment;

    private Log lastLog;


    public static final String EXTRA_WATCH = "watch";
    public static final String EXTRA_REFERENCE_TIME = "reference_time";
    public static final String EXTRA_LOG_TIME = "log_time";
    public static final String EXTRA_LAST_LOG = "last_log";     // To pre-fill the spinners of temperature and position

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.add_log);

        lastLog = (Log) getIntent().getSerializableExtra(EXTRA_LAST_LOG);
        final Watch currentWatch = (Watch) getIntent().getSerializableExtra(EXTRA_WATCH);
        final long referenceTime = getIntent().getLongExtra(EXTRA_REFERENCE_TIME, -1);
        final long watchTime = getIntent().getLongExtra(EXTRA_LOG_TIME, -1);

        double deviation = (double)(watchTime - referenceTime)/1000d;

        String deviationFormat = ((TextView) findViewById(R.id.logDeviation)).getText().toString();
        ((TextView) findViewById(R.id.logDeviation)).setText(String.format(deviationFormat, deviation));

        saveButton = (Button) findViewById(R.id.buttonSave);
        comment = (EditText) findViewById(R.id.logComment);

        prefillFormByLastLog(lastLog);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int period=0;
                if ( lastLog!=null) {
                    period = startFlag.isChecked()?lastLog.getPeriod()+1:lastLog.getPeriod();
                }
                saveLog(currentWatch.getId(), period,
                        referenceTime, watchTime,
                        POSITIONARR[(int)positionSpinner.getSelectedItemId()],
                        TEMPARR[(int)temperatureSpinner.getSelectedItemId()],
                        comment.getEditableText().toString());
                finish();
            }
        });
    }


    private void saveLog(long watchId, int period, long referenceTime, long watchTime,
                         String position, int temperature, String comment) {
        LogDao logDao = ((WatchCheckApplication)getApplicationContext()).getDaoSession().getLogDao();
        Log log = new Log(null, watchId, period, new Date(referenceTime), new Date(watchTime),
                position, temperature, comment);
        logDao.insert(log);
        Logger.info("Added log "+log);
    }


    private void prefillFormByLastLog(Log lastLog) {
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
    }
}
