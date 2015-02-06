package de.uhrenbastler.watchcheck;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.gc.materialdesign.views.ButtonFlat;
import com.gc.materialdesign.widgets.Dialog;

import org.apache.commons.lang3.StringUtils;

import java.sql.Timestamp;
import java.util.List;

import de.uhrenbastler.watchcheck.managers.WatchManager;
import de.uhrenbastler.watchcheck.tools.Logger;
import watchcheck.db.Log;
import watchcheck.db.Watch;
import watchcheck.db.WatchDao;


/**
 * Created by clorenz on 23.02.14.
 */
public class EditWatchActivity extends WatchCheckActionBarActivity {

    boolean isNewWatch=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_edit_watch);

        Bundle extras = getIntent().getExtras();
        final Watch watch = (extras!=null && extras.get("watch")!=null?(Watch) extras.get("watch"):new Watch());

        if (extras == null) {
            // Indicator, that we will add a new watch
            setTitle(R.string.new_watch);
        } else {
            setTitle(R.string.edit_watch);
            setWatchName(watch);
        }


        if ( watch.getName() != null || watch.getSerial()!=null || watch.getComment()!=null || watch.getCreatedAt()!=null ) {
            watch.setId((Long) extras.get("id"));

            Logger.debug("Editing watch " + watch);

            ((TextView)findViewById(R.id.editTextModel)).setText(watch.getName());
            ((TextView)findViewById(R.id.editTextSerial)).setText(watch.getSerial());
            ((TextView)findViewById(R.id.editTextRemarks)).setText(watch.getComment());
            ((ButtonFlat)findViewById(R.id.buttonDelete)).setVisibility(View.VISIBLE);
        } else {
            Logger.debug("Creating new watch");
            isNewWatch=true;
            watch.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        }

        ButtonFlat btnCancel = (ButtonFlat) findViewById(R.id.buttonCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditWatchActivity.this.finish();
                return;
            }
        });

        ButtonFlat btnOk = (ButtonFlat) findViewById(R.id.buttonOk);
        btnOk.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                watch.setName(((TextView)findViewById(R.id.editTextModel)).getEditableText().toString());
                watch.setSerial(((TextView)findViewById(R.id.editTextSerial)).getEditableText().toString());
                watch.setComment(((TextView)findViewById(R.id.editTextRemarks)).getEditableText().toString());
                long newWatchId = watchDao.insertOrReplace(watch);

                Logger.debug("Updated watch "+watch);

                if ( isNewWatch ) {
                    // Set this very watch as default
                    persistCurrentWatch(newWatchId);
                }

                finish();
                Toast.makeText(getApplicationContext(), String.format(getString(R.string.createdWatch),
                        watch.getName()), Toast.LENGTH_SHORT).show();

                return;
            }
        });

        ButtonFlat btnDelete = (ButtonFlat) findViewById(R.id.buttonDelete);
        btnDelete.setOnClickListener(new View.OnClickListener() {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            watchDao.delete(watch);
                            Toast.makeText(getApplicationContext(), String.format(getString(R.string.deletedWatch),
                                    watch.getName()), Toast.LENGTH_SHORT).show();
                            finish();
                            break;
                        case DialogInterface.BUTTON_NEGATIVE:
                            break;
                    }
                }
            };

            @Override
            public void onClick(View v) {
                final Dialog deleteWatchAlertDialog = new Dialog(EditWatchActivity.this,
                        getString(R.string.delete_this_watch),String.format(getString(R.string.deleteWatchQuestion),
                            watch.getName()+(!StringUtils.isBlank(watch.getSerial())?"/"+watch.getSerial():"")));
                deleteWatchAlertDialog.setCancelable(true);
                deleteWatchAlertDialog.addCancelButton(getString(R.string.no));
                deleteWatchAlertDialog.setOnAcceptButtonClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        List<Log> logs = logDao._queryWatch_Logs(watch.getId());
                        for ( Log log : logs ) {
                            logDao.delete(log);
                        }
                        watchDao.delete(watch);
                        Toast.makeText(getApplicationContext(), String.format(getString(R.string.deletedWatch),
                                watch.getName()), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
                deleteWatchAlertDialog.show();
                deleteWatchAlertDialog.getButtonAccept().setText(getString(R.string.yes));
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
