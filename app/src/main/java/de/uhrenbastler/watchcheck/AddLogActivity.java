package de.uhrenbastler.watchcheck;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import org.apache.commons.lang3.ArrayUtils;

import watchcheck.db.Log;

/**
 * Created by clorenz on 13.01.15.
 */
public class AddLogActivity extends Activity {
    private static final String[] POSITIONARR = { "","DU","DD","12U","3U","6U","9U" };
    private static final int[] TEMPARR = { -273, 4, 20, 36 };
    private Spinner positionSpinner;
    private Spinner temperatureSpinner;

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

        positionSpinner = (Spinner) findViewById(R.id.logSpinnerPosition);
        ArrayAdapter<?> positionAdapter = ArrayAdapter.createFromResource( getApplicationContext(),
                        R.array.positions,android.R.layout.simple_spinner_item);
        positionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        positionSpinner.setAdapter(positionAdapter);

        if ( lastLog!=null && lastLog.getPosition()!=null)
                positionSpinner.setSelection(ArrayUtils.indexOf(POSITIONARR, lastLog.getPosition()));
        else
                positionSpinner.setSelection(0);
    }
}
