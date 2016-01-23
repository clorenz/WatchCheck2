package de.uhrenbastler.watchcheck;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.gc.materialdesign.views.ButtonFlat;
import com.gc.materialdesign.widgets.Dialog;

import org.apache.commons.lang3.ArrayUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import de.uhrenbastler.watchcheck.tools.Logger;
import de.uhrenbastler.watchcheck.utils.LocalizedTimeUtil;
import watchcheck.db.Log;
import watchcheck.db.Watch;

/**
 * Created by clorenz on 13.01.15.
 */
public class AddLogActivity extends WatchCheckActionBarActivity {
    private static final String[] POSITIONARR = {
            Deviations.WORN.nameForLog,
            Deviations.DU.nameForLog,
            Deviations.DD.nameForLog,
            Deviations.O12.nameForLog,
            Deviations.O3.nameForLog,
            Deviations.O6.nameForLog,
            Deviations.O9.nameForLog,
            Deviations.WINDER.nameForLog,
            Deviations.OTHER.nameForLog
    };
    private static final int[] TEMPARR = { -273, 4, 20, 36 };
    private Spinner positionSpinner;
    private Spinner temperatureSpinner;
    private CheckBox startFlag;
    private ButtonFlat saveButton;
    private ButtonFlat deleteButton;
    private ButtonFlat cancelButton;
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
        super.onCreate(savedInstanceState, R.layout.add_log);
        setTitle(R.string.add_log);

        lastLog = (Log) getIntent().getSerializableExtra(EXTRA_LAST_LOG);
        editLog = (Log) getIntent().getSerializableExtra(EXTRA_EDIT_LOG);

        final long currentWatchId = (editLog!=null?editLog.getWatchId():((Watch) getIntent().getSerializableExtra(EXTRA_WATCH)).getId());
        final long referenceTime = editLog!=null ? editLog.getReferenceTime().getTime() : getIntent().getLongExtra(EXTRA_REFERENCE_TIME, -1);
        final long watchTime = editLog!=null ? editLog.getWatchTime().getTime() : getIntent().getLongExtra(EXTRA_LOG_TIME, -1);

        double deviation = (double)(watchTime - referenceTime)/1000d;

        String deviationFormat = ((TextView) findViewById(R.id.logDeviation)).getText().toString();
        ((TextView) findViewById(R.id.logDeviation)).setText(String.format(deviationFormat, deviation));

        Watch currentWatch = watchDao.load(currentWatchId);
        setWatchName(currentWatch);

        deleteButton = (ButtonFlat) findViewById(R.id.buttonDelete);
        cancelButton = (ButtonFlat) findViewById(R.id.buttonCancel);
        saveButton = (ButtonFlat) findViewById(R.id.buttonSave);

        if ( editLog!=null ) {
            saveButton.setText(getString(R.string.button_update));
            setTitle(R.string.edit_log);
            deleteButton.setOnClickListener(deleteButtonOnClickListener());
        } else {
            // New log -> minimize and hide "delete" button;
            deleteButton.setLayoutParams(new LinearLayout.LayoutParams(0,0,0));
            deleteButton.setVisibility(View.INVISIBLE);
        }
        comment = (EditText) findViewById(R.id.logComment);

        prefillForm(editLog != null ? editLog : lastLog, editLog != null);

        saveButton.setOnClickListener(saveButtonOnClickListener(currentWatchId, referenceTime, watchTime));

        cancelButton.setOnClickListener(cancelButtonOnClickListener());
    }


    private View.OnClickListener cancelButtonOnClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        };
    }

    private View.OnClickListener saveButtonOnClickListener(final long currentWatchId, final long referenceTime, final long watchTime) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( editLog!=null) {
                    updateLogEntry(editLog);
                } else {
                    createNewLogEntry(currentWatchId, referenceTime, watchTime);
                }
                finish();
            }
        };
    }

    private View.OnClickListener deleteButtonOnClickListener() {
        return new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Dialog deleteLogAlertDialog = new Dialog(AddLogActivity.this,
                            getString(R.string.delete_this_log),String.format(getString(R.string.deleteLogQuestion),
                            LocalizedTimeUtil.getDateAndTime(getApplicationContext(),editLog.getReferenceTime())));
                            deleteLogAlertDialog.setCancelable(true);
                            deleteLogAlertDialog.addCancelButton(getString(R.string.no));
                            deleteLogAlertDialog.setOnAcceptButtonClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                                logDao.delete(editLog);
                                finish();
                                Toast.makeText(getApplicationContext(), String.format(getString(R.string.deletedLog),
                                        LocalizedTimeUtil.getDateAndTime(getApplicationContext(),editLog.getReferenceTime())),
                                        Toast.LENGTH_SHORT).show();

                        }
                    });
                    deleteLogAlertDialog.show();
                    deleteLogAlertDialog.getButtonAccept().setText(getString(R.string.yes));
                }
        };
    }


    private void createNewLogEntry(long currentWatchId, long referenceTime, long watchTime) {
        int period=0;
        List<Log> allLogs = logDao._queryWatch_Logs(currentWatchId);
        if ( allLogs!=null && !allLogs.isEmpty() ) {
            period = allLogs.get(allLogs.size() - 1).getPeriod();
            if ( startFlag.isChecked()) {
                period++;
            }
        }

        Logger.info("Creating new log entry for watch="+currentWatchId+" and period="+period);

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
        Log log = new Log(null, watchId, period, new Date(referenceTime), new Date(watchTime),
                position, temperature, comment);
        logDao.insert(log);
        Logger.info("Added log "+log);
    }

    private void updateLog(long watchId, long logId, int period, long referenceTime, long watchTime,
                                String position, int temperature, String comment) {
        Log log = new Log(logId, watchId, period, new Date(referenceTime), new Date(watchTime),
                position, temperature, comment);
        logDao.update(log);
        Logger.info("Updated log "+log);
    }


    private void prefillForm(Log lastLog, boolean hideStartflag) {
        positionSpinner = (Spinner) findViewById(R.id.logSpinnerPosition);
        ArrayAdapter<?> positionAdapter = ArrayAdapter.createFromResource( this,
                        R.array.positions,android.R.layout.simple_dropdown_item_1line );
        positionAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        positionSpinner.setAdapter(positionAdapter);

        if ( lastLog!=null && lastLog.getPosition()!=null)
                positionSpinner.setSelection(ArrayUtils.indexOf(POSITIONARR, lastLog.getPosition()));
        else
                positionSpinner.setSelection(0);

        temperatureSpinner = (Spinner) findViewById(R.id.logSpinnerTemperature);
        ArrayAdapter<?> temperatureAdapter = ArrayAdapter.createFromResource( this,
                R.array.temperatures,android.R.layout.simple_dropdown_item_1line);
        temperatureAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
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
    }
}
