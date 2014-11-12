package de.uhrenbastler.watchcheck.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import de.uhrenbastler.watchcheck.R;
import de.uhrenbastler.watchcheck.managers.WatchManager;
import de.uhrenbastler.watchcheck.tools.Logger;
import de.uhrenbastler.watchcheck.views.SelectWatchArrayAdapter;
import roboguice.inject.InjectResource;
import roboguice.inject.InjectView;

/**
 * Created by clorenz on 30.10.14.
 */
public class DisplayResultActivity extends BaseActivity {

    private static final String PREFERENCE_CURRENT_WATCH = "currentWatch";
    private int selectedWatchId;
    private ActionBarDrawerToggle drawerToggle;
    //private String title;

    private ListView drawerList;
    private DrawerLayout drawer;

    SelectWatchArrayAdapter adapter;
    List<de.uhrenbastler.watchcheck.models.Watch> watches;

    @InjectResource(R.string.addWatch)
    String ADD_WATCH;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        de.uhrenbastler.watchcheck.models.Watch selectedWatch = WatchManager.retrieveCurrentWatch(this);
        selectedWatchId = (int)(selectedWatch!=null?selectedWatch.getId():-1);
        if ( selectedWatchId==-1) {
            selectedWatchId=1;
            Logger.warn("No watch selected. Using first watch as default");
            persistCurrentWatch(selectedWatchId);
        }
        Logger.debug("Current watch has got ID "+selectedWatchId);

        setActionBarIcon(R.drawable.ic_drawer);

        watches = sortWithCurrentFirst(WatchManager.retrieveAllWatches(),selectedWatch);
        watches.add(new de.uhrenbastler.watchcheck.models.Watch(ADD_WATCH,null,null,null));

        Logger.debug("Watches="+watches);
        setActionBarWatchName(selectedWatch.getName());
        //title = getActionBarTitle();

        adapter = new SelectWatchArrayAdapter(this, getApplicationContext(),
                R.layout.drawer_list_item, R.id.watchName, R.id.watchSerial, watches, selectedWatchId);
        drawerList = (ListView) findViewById(R.id.drawer_list);
        drawerList.setAdapter(adapter);

        drawerToggle = new ActionBarDrawerToggle(this, drawer, R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                //setActionBarTitle(title);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                //title = getActionBarTitle();
                //setActionBarTitle("Select watch");
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        // Set the drawer toggle as the DrawerListener
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.setDrawerListener(drawerToggle);
    }

    @Override protected int getLayoutResource() {
        return R.layout.activity_display_result;
    }


    private void persistCurrentWatch(int currentWatchId) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(PREFERENCE_CURRENT_WATCH, currentWatchId);
        editor.commit();
    }

    private List<de.uhrenbastler.watchcheck.models.Watch> sortWithCurrentFirst(List<de.uhrenbastler.watchcheck.models.Watch> origList, de.uhrenbastler.watchcheck.models.Watch firstItem) {
        List<de.uhrenbastler.watchcheck.models.Watch> ret = new ArrayList<de.uhrenbastler.watchcheck.models.Watch>();
        ret.add(firstItem);

        for ( de.uhrenbastler.watchcheck.models.Watch watch : origList ) {
            if ( watch.getId() != firstItem.getId()) {
                ret.add(watch);
            }
        }

        return ret;
    }
}
