package de.uhrenbastler.watchcheck;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.view.MenuItem;

import watchcheck.db.LogDao;
import watchcheck.db.Watch;
import watchcheck.db.WatchDao;

/**
 * Created by clorenz on 27.01.15.
 */
public class WatchCheckActionBarActivity extends ActionBarActivity {

    protected WatchDao watchDao;
    protected LogDao logDao;

    protected void onCreate(Bundle savedInstanceState, int viewResource) {
        super.onCreate(savedInstanceState);

        setContentView(viewResource);

        setToolbar();

        watchDao = ((WatchCheckApplication)getApplicationContext()).getDaoSession().getWatchDao();
        logDao = ((WatchCheckApplication)getApplicationContext()).getDaoSession().getLogDao();
    }

    private void setToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }


    public void setTitle(int resource) {
        getSupportActionBar().setTitle(resource);
    }


    public void setWatchName(Watch currentWatch) {
        if ( currentWatch!=null ) {
            String watchName = currentWatch.getName();
            SpannableString subtitle = new SpannableString(watchName);
            subtitle.setSpan(new RelativeSizeSpan(0.8f), 0, watchName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            getSupportActionBar().setSubtitle(subtitle);
        }
    }


    public void unsetWatchName() {
        getSupportActionBar().setSubtitle(null);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }
}
