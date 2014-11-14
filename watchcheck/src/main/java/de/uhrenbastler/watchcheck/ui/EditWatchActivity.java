package de.uhrenbastler.watchcheck.ui;

import android.app.Activity;
import android.content.ContentValues;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import de.uhrenbastler.watchcheck.R;
import de.uhrenbastler.watchcheck.models.Watch;
import de.uhrenbastler.watchcheck.tools.Logger;

/**
 * Created by clorenz on 23.02.14.
 */
// TODO: "Back" link in menu is confusing
// Find a way to delete a watch
public class EditWatchActivity extends BaseActivity {

    @Override protected int getLayoutResource() {
        return R.layout.activity_edit_watch;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            Logger.warn("EditWatchActivity called without data!!");
            return;
        }

        final Watch watch = (Watch) extras.get("watch");
        watch.setId((Long)extras.get("id"));

        Logger.debug("Editing watch "+ watch);

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
                watch.setName(model.getEditableText().toString());
                watch.setSerial(serial.getEditableText().toString());
                watch.setComment(remarks.getEditableText().toString());
                watch.save();

                Logger.debug("Updated watch "+watch);

                EditWatchActivity.this.finish();
                return;
            }
        });
    }
}
