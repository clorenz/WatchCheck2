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

import java.sql.Timestamp;
import java.util.List;

import de.uhrenbastler.watchcheck.managers.WatchManager;
import de.uhrenbastler.watchcheck.tools.Logger;
import watchcheck.db.Watch;
import watchcheck.db.WatchDao;

// Sollte das nicht besser ein Fragement sein??

/**
 * Created by clorenz on 23.02.14.
 */
public class EditWatchActivity extends ActionBarActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    boolean isNewWatch=false;
    private WatchDao watchDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        watchDao = ((WatchCheckApplication)getApplicationContext()).getDaoSession().getWatchDao();
        setContentView(R.layout.activity_edit_watch);

        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            // Indicator, that we will add a new watch
            TextView headline = (TextView) findViewById(R.id.textViewAddWatch);
            headline.setText(getString(R.string.addWatchHeadline));
        }

        final Watch watch = (extras!=null && extras.get("watch")!=null?(Watch) extras.get("watch"):new Watch());
        if ( watch.getName() != null || watch.getSerial()!=null || watch.getComment()!=null || watch.getCreatedAt()!=null ) {
            watch.setId((Long) extras.get("id"));

            Logger.debug("Editing watch " + watch);

            ((TextView)findViewById(R.id.editTextModel)).setText(watch.getName());
            ((TextView)findViewById(R.id.editTextSerial)).setText(watch.getSerial());
            ((TextView)findViewById(R.id.editTextRemarks)).setText(watch.getComment());
            ((Button)findViewById(R.id.buttonDelete)).setVisibility(View.VISIBLE);
        } else {
            Logger.debug("Creating new watch");
            isNewWatch=true;
            watch.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        }

        Button btnCancel = (Button) findViewById(R.id.buttonCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditWatchActivity.this.finish();
                return;
            }
        });

        Button btnOk = (Button) findViewById(R.id.buttonOk);
        btnOk.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                watch.setName(((TextView)findViewById(R.id.editTextModel)).getEditableText().toString());
                watch.setSerial(((TextView)findViewById(R.id.editTextSerial)).getEditableText().toString());
                watch.setComment(((TextView)findViewById(R.id.editTextRemarks)).getEditableText().toString());
                watchDao.insertOrReplace(watch);

                Logger.debug("Updated watch "+watch);

                finish();

                updateNavigationDrawerAndHeadline(isNewWatch ? null : watch);

                Toast.makeText(getApplicationContext(), String.format(getString(R.string.createdWatch),
                        watch.getName()), Toast.LENGTH_SHORT).show();

                return;
            }
        });

        Button btnDelete = (Button) findViewById(R.id.buttonDelete);
        btnDelete.setOnClickListener(new View.OnClickListener() {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            watchDao.delete(watch);
                            updateNavigationDrawerAndHeadline(isNewWatch ? null : watch);
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
                AlertDialog.Builder deleteWatchAlertDialog = new AlertDialog.Builder(v.getContext());
                deleteWatchAlertDialog.setMessage(String.format(getString(R.string.deleteWatchQuestion),
                        watch.getName()+(watch.getSerial()!=null?"/"+watch.getSerial():"")))
                        .setPositiveButton(getString(R.string.yes), dialogClickListener)
                        .setNegativeButton(getString(R.string.no), dialogClickListener)
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

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        Logger.debug("ITEM="+position);
    }


    public void updateNavigationDrawerAndHeadline(Watch currentWatch) {
        SpannableString subtitle = new SpannableString(currentWatch.getName());
        subtitle.setSpan(new RelativeSizeSpan(0.8f), 0, currentWatch.getName().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        getSupportActionBar().setSubtitle(subtitle);

        
        // Re-Fetch all watches and re-populate the navigation drawer
        List<Watch> watches = WatchManager.retrieveAllWatchesWithCurrentFirstAndAddWatch(
                (WatchCheckApplication)getApplicationContext(),currentWatch);

        // TODO ListView of NavigationDrawer re-populaten

        /*
        NavigationDrawerFragment mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mNavigationDrawerFragment.setWatches(watches);
        if ( currentWatch!=null ) {
            mNavigationDrawerFragment.setSelectedWatch(currentWatch);
            //updateTitle(currentWatch.getName());
        }
        mNavigationDrawerFragment.updateDrawer();
        */
    }


    /*
    private void updateTitle(String watchName) {
        // Find calling activity and update title there
        SpannableString subtitle = new SpannableString(watchName);
        subtitle.setSpan(new RelativeSizeSpan(0.8f), 0, watchName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        getSupportActionBar().setSubtitle(subtitle);
    }
    */
}
