package de.uhrenbastler.watchcheck.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Timestamp;

import de.uhrenbastler.watchcheck.R;
import de.uhrenbastler.watchcheck.models.Watch;
import de.uhrenbastler.watchcheck.tools.Logger;
import roboguice.inject.InjectResource;
import roboguice.inject.InjectView;

/**
 * Created by clorenz on 23.02.14.
 */
public class EditWatchActivity extends BaseActivity {

    @InjectResource(R.string.addWatchHeadline)
    String ADD_WATCH;
    @InjectResource(R.string.createdWatch)
    String CREATED_WATCH;
    @InjectResource(R.string.deletedWatch)
    String DELETED_WATCH;
    @InjectResource(R.string.deleteWatchQuestion)
    String DELETE_WATCH;
    @InjectResource(R.string.yes)
    String YES;
    @InjectResource(R.string.no)
    String NO;

    @InjectView(R.id.editTextModel)
    EditText model;
    @InjectView(R.id.editTextSerial)
    EditText serial;
    @InjectView(R.id.editTextRemarks)
    EditText remarks;
    @InjectView(R.id.buttonOk)
    Button btnOk;
    @InjectView(R.id.buttonCancel)
    Button btnCancel;
    @InjectView(R.id.buttonDelete)
    Button btnDelete;



    @Override protected int getLayoutResource() {
        return R.layout.activity_edit_watch;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            // Indicator, that we will add a new watch
            TextView headline = (TextView) findViewById(R.id.textViewAddWatch);
            headline.setText(ADD_WATCH);
        }

        final Watch watch = (extras!=null && extras.get("watch")!=null?(Watch) extras.get("watch"):new Watch());
        if ( watch.getName() != null || watch.getSerial()!=null || watch.getComment()!=null || watch.getCreatedAt()!=null ) {
            watch.setId((Long) extras.get("id"));

            Logger.debug("Editing watch " + watch);

            model.setText(watch.getName());
            serial.setText(watch.getSerial());
            remarks.setText(watch.getComment());
            btnDelete.setVisibility(View.VISIBLE);
        } else {
            Logger.debug("Creating new watch");
            watch.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        }

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

                finish();

                Toast.makeText(getApplicationContext(), String.format(CREATED_WATCH,watch.getName()), Toast.LENGTH_SHORT).show();

                return;
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            watch.delete();
                            Toast.makeText(getApplicationContext(), String.format(DELETED_WATCH,watch.getName()), Toast.LENGTH_SHORT).show();
                            finish();
                            break;
                        case DialogInterface.BUTTON_NEGATIVE:
                            break;
                    }
                }
            };

            @Override
            public void onClick(View v) {
                AlertDialog.Builder deleteWatchAlertDialog = new AlertDialog.Builder(v.getContext());
                deleteWatchAlertDialog.setMessage(String.format(DELETE_WATCH,watch.getName()+(watch.getSerial()!=null?"/"+watch.getSerial():"")))
                        .setPositiveButton(YES, dialogClickListener)
                        .setNegativeButton(NO, dialogClickListener)
                        .show();
            }
        });
    }

    /**
     * Handler for the "back" icon in the action bar / toolbar
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; finish activity
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
