package de.uhrenbastler.watchcheck.ui;

import android.app.Activity;
import android.content.ContentValues;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import de.uhrenbastler.watchcheck.R;
import de.uhrenbastler.watchcheck.data.Watch;
import de.uhrenbastler.watchcheck.db.WatchCheckDBHelper;
import de.uhrenbastler.watchcheck.tools.Logger;

/**
 * Created by clorenz on 23.02.14.
 */
public class EditWatchActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.editwatch);

        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            Logger.warn("EditWatchActivity called without data!!");
            return;
        }

        final int watchId = extras.getInt(Watch.Watches._ID);

        // Retrieve watch and fill fields
        Watch watch = WatchCheckDBHelper.getWatchFromDatabase(watchId, this.getContentResolver());
        Logger.debug("Editing watch with id=" + watchId + "=" + watch);

        final EditText model = (EditText) findViewById(R.id.editTextModel);
        model.setText(watch.getName());

        final EditText serial = (EditText) findViewById(R.id.editTextSerial);
        serial.setText(watch.getSerial());

        final EditText remarks = (EditText) findViewById(R.id.editTextRemarks);
        remarks.setText(watch.getComment());

        Button btnOk = (Button) findViewById(R.id.buttonOk);
        Button btnCancel = (Button) findViewById(R.id.buttonCancel);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditWatchActivity.this.finish();
                return;
            }
        });

        btnOk.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Daten in die DB legen
                ContentValues values = new ContentValues();
                values.put(Watch.Watches.NAME, model.getEditableText().toString());
                values.put(Watch.Watches.SERIAL, serial.getEditableText().toString());
                values.put(Watch.Watches.COMMENT, remarks.getEditableText().toString());

                int updatedRecords = getContentResolver().update(Watch.Watches.CONTENT_URI, values, Watch.Watches._ID+"="+watchId, null);

                Logger.debug("Updated "+updatedRecords+" watches with id="+watchId+" and values="+values);

                EditWatchActivity.this.finish();
                return;
            }
        });
    }
}
