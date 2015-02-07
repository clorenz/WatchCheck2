package de.uhrenbastler.watchcheck;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.view.MenuItem;

import de.uhrenbastler.watchcheck.tools.Logger;
import watchcheck.db.LogDao;
import watchcheck.db.Watch;
import watchcheck.db.WatchDao;

/**
 * Created by clorenz on 27.01.15.
 */
public class WatchCheckActionBarActivity extends ActionBarActivity {

    protected WatchDao watchDao;
    protected LogDao logDao;
    private static final String PREFERENCE_CURRENT_WATCH = "currentWatch";

    protected void onCreate(Bundle savedInstanceState, int viewResource) {
        super.onCreate(savedInstanceState);

        setContentView(viewResource);

        setToolbar();

        watchDao = ((WatchCheckApplication)getApplicationContext()).getDaoSession().getWatchDao();
        logDao = ((WatchCheckApplication)getApplicationContext()).getDaoSession().getLogDao();


        DrawerLayout dl = (DrawerLayout) findViewById(R.id.drawer_layout);
        dl.setStatusBarBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
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
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public void persistCurrentWatch(long currentWatchId) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        if ( currentWatchId>=0) {
            editor.putInt(PREFERENCE_CURRENT_WATCH, (int) currentWatchId);
            Logger.debug("Setting preference for current watch to " + currentWatchId);
        } else {
            Logger.debug("Removing preference for current watch");
            editor.remove(PREFERENCE_CURRENT_WATCH);
        }
        editor.commit();
    }
}
