package de.uhrenbastler.watchcheck.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import de.uhrenbastler.watchcheck.R;
import de.uhrenbastler.watchcheck.managers.WatchManager;
import de.uhrenbastler.watchcheck.models.Watch;
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

    @InjectView(R.id.drawer_list)
    private ListView drawerList;

    // For whatever reason, this cannot be injected
    private DrawerLayout drawer;

    SelectWatchArrayAdapter adapter;
    List<de.uhrenbastler.watchcheck.models.Watch> watches;

    @InjectResource(R.string.addWatch)
    String ADD_WATCH;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMenuAndHeadline();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setMenuAndHeadline();
    }

    private void setMenuAndHeadline() {
        Watch selectedWatch = WatchManager.retrieveCurrentWatch(this);
        selectedWatchId = (int)(selectedWatch!=null?selectedWatch.getId():-1);
        if ( selectedWatchId==-1) {
            selectedWatchId=1;
            Logger.warn("No watch selected. Using first watch as default");
            persistCurrentWatch(selectedWatchId);
        }
        Logger.debug("Current watch has got ID "+selectedWatchId);

        watches = sortWithCurrentFirst(WatchManager.retrieveAllWatches(),selectedWatch);
        watches.add(new Watch(ADD_WATCH,null,null,null));

        Logger.debug("Watches="+watches);
        setActionBarWatchName(selectedWatch.getName());
        getToolbar().setNavigationIcon(R.drawable.ic_drawer);

        adapter = new SelectWatchArrayAdapter(this, getApplicationContext(),
                R.layout.drawer_list_item, R.id.watchName, R.id.watchSerial, watches, selectedWatchId);
        drawerList.setAdapter(adapter);
        drawerList.setOnItemClickListener(new NavigationDrawerItemClickListener());
        drawerList.setLongClickable(true);
        drawerList.setOnItemLongClickListener(new NavigationDrawerItemLongClickListener());

        drawerToggle = new ActionBarDrawerToggle(this, drawer, R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        drawerToggle.setDrawerIndicatorEnabled(true);
        // Set the drawer toggle as the DrawerListener
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.setDrawerListener(drawerToggle);
    }



    @Override protected int getLayoutResource() {
        return R.layout.activity_display_result;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Logger.debug("Selected item "+item);
        if ( drawerToggle.onOptionsItemSelected(item)) {
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        Logger.debug("Navigate up");
        return super.onSupportNavigateUp();
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

    /**
     * Listener, which receives the selected navigation drawer item
     */
    private class NavigationDrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            drawerList.setItemChecked(position, true);
            de.uhrenbastler.watchcheck.models.Watch selectedWatch = watches.get((int) id);
            if ( selectedWatch.getId() != null) {
                setActionBarWatchName(selectedWatch.getName());
                Logger.debug("Selected item " + position + " with id " + id + "=" + selectedWatch);
                drawer.closeDrawer((LinearLayout) findViewById(R.id.drawer_linear_layout));
            } else {
                Logger.debug("New watch selected. Starting new activity");
                drawer.closeDrawer((LinearLayout) findViewById(R.id.drawer_linear_layout));
                Intent editWatchIntent = new Intent(parent.getContext(), EditWatchActivity.class);
                startActivity(editWatchIntent);
            }
        }
    }

    /**
     * Listener, which recieves long clicks on the selected navigation drawer item which leads
     * to editing the selected items
     */
    private class NavigationDrawerItemLongClickListener implements ListView.OnItemLongClickListener {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            drawerList.setItemChecked(position, true);
            de.uhrenbastler.watchcheck.models.Watch selectedWatch = watches.get((int) id);
            if ( selectedWatch.getId() != null) {
                Logger.debug("Selected watch for edit: "+selectedWatch+". Starting new activity");
                drawer.closeDrawer((LinearLayout) findViewById(R.id.drawer_linear_layout));
                Intent editWatchIntent = new Intent(parent.getContext(), EditWatchActivity.class);
                editWatchIntent.putExtra("watch", selectedWatch);
                editWatchIntent.putExtra("id",selectedWatch.getId());
                startActivity(editWatchIntent);
                return true;
            }
            return false;
        }
    }
}
