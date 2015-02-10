package de.uhrenbastler.watchcheck;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.text.Html;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.webkit.WebSettings;
import android.widget.TextView;

import com.gc.materialdesign.views.ButtonFloat;
import com.gc.materialdesign.widgets.Dialog;

import java.util.List;

import de.uhrenbastler.watchcheck.managers.WatchManager;
import de.uhrenbastler.watchcheck.tools.Logger;
import de.uhrenbastler.watchcheck.views.*;
import watchcheck.db.Watch;


public class MainActivity extends WatchCheckActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    private List<Watch> watches;
    private static final String PRIVATE_PREF = "myapp";
    private static final String VERSION_KEY = "version_number";


    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    private DisplayResultPagerAdapter adapterViewPager;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    private Watch selectedWatch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_main);
        init();
    }


    @Override
    protected void onResume() {
        super.onResume();

        watches = watchDao.loadAll();
        selectedWatch = WatchManager.retrieveCurrentWatch(this);
        Logger.debug("Selected watch onResume="+selectedWatch);
        if ( selectedWatch == null ) {
            if ( watches!=null && !watches.isEmpty()) {
                long selectedWatchId = watches.get(0).getId();
                Logger.warn("No watch selected. Using first watch with id="+selectedWatchId+" as default");
                persistCurrentWatch(selectedWatchId);
                selectedWatch = watches.get(0);
                setWatchName(selectedWatch);
            } else {
                Logger.warn("No watch yet!");
                unsetWatchName();
                persistCurrentWatch(-1);
            }
        } else {
            setWatchName(selectedWatch);
        }
        Logger.debug("Current watch = "+selectedWatch);

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
        ButtonFloat fab = (ButtonFloat) findViewById(R.id.buttonAddLog);
        if ( selectedWatch != null ) {
            prepareResultPager(vpPager, selectedWatch.getId());
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent checkWatchIntent = new Intent(getApplication(),CheckWatchActivity.class);
                    checkWatchIntent.putExtra(CheckWatchActivity.EXTRA_WATCH, selectedWatch);
                    Logger.debug("FROM MAIN");
                    startActivity(checkWatchIntent);
                }
            });
            fab.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
            fab.setVisibility(View.VISIBLE);

        } else {
            // Hide viewPager
            vpPager.setVisibility(View.INVISIBLE);
            // Hide Add button
            fab.setVisibility(View.INVISIBLE);
            // And maybe display a nice background instead
        }
    }

    private void prepareResultPager(ViewPager vpPager, long selectedWatchId) {
        FragmentManager manager = getSupportFragmentManager();
        if (manager.getBackStackEntryCount() > 0) {
            FragmentManager.BackStackEntry first = manager.getBackStackEntryAt(0);
            manager.popBackStack(first.getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
        if ( adapterViewPager!=null) {
            adapterViewPager.notifyDataSetChanged();
            Logger.debug("Clearing previous "+adapterViewPager.getCount()+" fragments");
        }


        if ( !logDao._queryWatch_Logs(selectedWatchId).isEmpty()) {
            vpPager.setVisibility(View.VISIBLE);
            adapterViewPager = new DisplayResultPagerAdapter(getApplicationContext(), getSupportFragmentManager(), selectedWatchId);
            vpPager.setAdapter(adapterViewPager);
            vpPager.setCurrentItem(adapterViewPager.getCount());

            /*
            PagerTabStrip mPagerTabStrip = (PagerTabStrip) findViewById(R.id.pager_header);
            for (int i = 0; i < mPagerTabStrip.getChildCount(); ++i) {
                View nextChild = mPagerTabStrip.getChildAt(i);
                if (nextChild instanceof TextView) {
                    TextView textViewToConvert = (TextView) nextChild;
                    textViewToConvert.setTextScaleX(2.5f);
                }
            }
            */
        } else {
            vpPager.setVisibility(View.INVISIBLE);
        }
    }

    private void init() {
        SharedPreferences sharedPref    = getSharedPreferences(PRIVATE_PREF, Context.MODE_PRIVATE);
        int currentVersionNumber        = 0;

        int savedVersionNumber          = sharedPref.getInt(VERSION_KEY, 0);

        try {
            PackageInfo pi          = getPackageManager().getPackageInfo(getPackageName(), 0);
            currentVersionNumber    = pi.versionCode;
        } catch (Exception e) {}

        if (currentVersionNumber > savedVersionNumber) {
            showWhatsNewDialog();

            SharedPreferences.Editor editor   = sharedPref.edit();

            editor.putInt(VERSION_KEY, currentVersionNumber);
            editor.commit();
        }
    }


    private void showWhatsNewDialog() {
        final Dialog whatsNewDialog = new Dialog(this,
                this.getString(R.string.whatsnew),"");
        whatsNewDialog.setButtonCancel(null);
        whatsNewDialog.show();
        whatsNewDialog.getButtonAccept().setText(getString(R.string.ok));
        TextView tvWhatsNew = whatsNewDialog.getMessageTextView();
        Logger.debug("MessageTextView="+tvWhatsNew);
        tvWhatsNew.setText(Html.fromHtml(getString(R.string.changelog)));
    }


    @Override
    public void onNavigationDrawerItemSelected(int position) {
        Logger.debug("Watch in position "+position+"="+watches.get(position));


        // Only persist, if a real watch (and not "add watch") is selected
        if ( watches!=null && watches.get(position)!=null && watches.get(position).getId()!=null ) {
            setWatchName(watches.get(position));
            persistCurrentWatch(watches.get(position).getId().intValue());

            if (logDao._queryWatch_Logs(watches.get(position).getId()).isEmpty()) {
                Logger.debug("Selecting watch w/o results from drawer");
                ViewPager vpPager = (ViewPager) findViewById(R.id.pager);
                vpPager.setVisibility(View.INVISIBLE);
            } else {
                selectedWatch = watches.get(position);
                Logger.debug("Selecting watch " + selectedWatch.getId() + " from drawer");
                // Otherwise: update the main content by replacing fragments
                ButtonFloat fab = (ButtonFloat) findViewById(R.id.buttonAddLog);
                ViewPager vpPager = (ViewPager) findViewById(R.id.pager);
                prepareResultPager(vpPager, selectedWatch.getId());
                fab.setVisibility(View.VISIBLE);
            }
        }
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

        return false;
    }
}
