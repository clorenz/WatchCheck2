package de.uhrenbastler.watchcheck;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.widget.DrawerLayout;

import java.util.List;

import de.uhrenbastler.watchcheck.managers.DatabaseUpgradeManager9;
import de.uhrenbastler.watchcheck.managers.WatchManager;
import de.uhrenbastler.watchcheck.models.Watch;
import de.uhrenbastler.watchcheck.tools.Logger;
import de.uhrenbastler.watchcheck.views.*;


public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    private static final String PREFERENCE_CURRENT_WATCH = "currentWatch";
    private List<Watch> watches;

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    private DisplayResultPagerAdapter adapterViewPager;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new DatabaseUpgradeManager9(this.getApplicationContext());

        // For whatever reason, WatchManager has got problems and crashes in line 60
        // Maybe it helps to rename everything to watchcheck again

        Watch selectedWatch = WatchManager.retrieveCurrentWatch(this);
        int selectedWatchId = (int)(selectedWatch!=null?selectedWatch.getId():-1);
        if ( selectedWatchId==-1) {
            selectedWatchId=1;
            Logger.warn("No watch selected. Using first watch as default");
            persistCurrentWatch(selectedWatchId);
        } else {
            updateTitle(selectedWatch.getName());
        }
        Logger.debug("Current watch has got ID " + selectedWatchId);
        watches =  WatchManager.retrieveAllWatchesWithCurrentFirstAndAddWatch(selectedWatch);

        Logger.debug("Watches="+watches);

        setContentView(R.layout.activity_main);
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);

        mNavigationDrawerFragment.setWatches(watches);
        mNavigationDrawerFragment.setSelectedWatch(selectedWatch);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        ViewPager vpPager = (ViewPager) findViewById(R.id.pager);
        adapterViewPager = new DisplayResultPagerAdapter(getSupportFragmentManager(), selectedWatchId);
        vpPager.setAdapter(adapterViewPager);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        Logger.debug("Watch in position "+position+"="+watches.get(position));

        // update the main content by replacing fragments
        ViewPager vpPager = (ViewPager) findViewById(R.id.pager);
        adapterViewPager = new DisplayResultPagerAdapter(getSupportFragmentManager(), watches.get(position).getId());
        vpPager.setAdapter(adapterViewPager);

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, DisplayResultFragment.newInstance(watches.get(position).getId(), 0))
                .commit();


        // Only persist, if a real watch (and not "add watch") is selected
        if ( watches!=null && watches.get(position)!=null && watches.get(position).getId()!=null ) {
            String watchName = watches.get(position).getName();
            updateTitle(watchName);
            persistCurrentWatch(watches.get(position).getId().intValue());
        }

        // If "add watch" was selected, start the new activity
        if ( watches!=null && watches.get(position)!=null && watches.get(position).getId()==null) {
            Logger.debug("New watch selected. Starting new activity");
            Fragment editWatchFragment = new EditWatchFragment();
            Bundle bundle = new Bundle();
            bundle.putSerializable("watch", watches.get(position));
            bundle.putSerializable("currentWatch", watches.get(0));
            editWatchFragment.setArguments(bundle);

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.container, editWatchFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }

    private void updateTitle(String watchName) {
        SpannableString subtitle = new SpannableString(watchName);
        subtitle.setSpan(new RelativeSizeSpan(0.8f), 0, watchName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        getSupportActionBar().setSubtitle(subtitle);
    }

    public void restoreActionBar() {
        Logger.debug("Restoring actionBar with title="+mTitle);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void persistCurrentWatch(int currentWatchId) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(PREFERENCE_CURRENT_WATCH, currentWatchId);
        editor.commit();
    }


}
