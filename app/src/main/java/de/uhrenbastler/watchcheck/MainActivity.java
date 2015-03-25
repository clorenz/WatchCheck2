package de.uhrenbastler.watchcheck;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.os.AsyncTask;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.internal.view.menu.ActionMenuItemView;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.gc.materialdesign.views.ButtonFloat;
import com.gc.materialdesign.widgets.Dialog;
import com.pkmmte.pkrss.Article;

import java.util.List;

import de.uhrenbastler.watchcheck.managers.AppStateManager;
import de.uhrenbastler.watchcheck.managers.WatchManager;
import de.uhrenbastler.watchcheck.reminder.ReminderManager;
import de.uhrenbastler.watchcheck.rss.AsyncRssLoaderForPopup;
import de.uhrenbastler.watchcheck.rss.AsyncRssResponse;
import de.uhrenbastler.watchcheck.rss.UhrenbastlerRssFeedDisplayDialog;
import de.uhrenbastler.watchcheck.tools.Logger;
import watchcheck.db.Watch;


public class MainActivity extends WatchCheckActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks, AsyncRssResponse {

    private List<Watch> watches;
    private static final String PRIVATE_PREF = "myapp";
    private static final String VERSION_KEY = "version_number";
    private boolean showSummary=false;

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
    private AsyncRssLoaderForPopup asyncRssLoaderForPopup;

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

        mNavigationDrawerFragment.setWatches(addAddWatch(watches));
        mNavigationDrawerFragment.setSelectedWatch(selectedWatch);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        ViewPager vpPager = (ViewPager) findViewById(R.id.pager);
        ButtonFloat fab = (ButtonFloat) findViewById(R.id.buttonAddLog);
        if ( selectedWatch != null ) {
            prepareResultPager(vpPager, selectedWatch.getId(), showSummary);
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
        
        asyncRssLoaderForPopup = new AsyncRssLoaderForPopup(getApplicationContext(), this, 10);
        asyncRssLoaderForPopup.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        if ( ReminderManager.getInitialAlarmDialogValue()!=null) {
            ReminderManager.displayInitialAlarmDialog(this);
        }
    }

    private List<Watch> addAddWatch(List<Watch> watches) {
        Watch addWatch = new Watch(-1L);
        addWatch.setName(getString(R.string.add_watch));
        watches.add(addWatch);
        return watches;
    }


    private void prepareResultPager(ViewPager vpPager, long selectedWatchId, boolean showSummary) {
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

            adapterViewPager = new DisplayResultPagerAdapter(getApplicationContext(), getSupportFragmentManager(), selectedWatchId, showSummary);

            vpPager.setAdapter(adapterViewPager);
            ResultOnPageChangeListener resultOnPageChangeListener = new ResultOnPageChangeListener(adapterViewPager);
            vpPager.setOnPageChangeListener(resultOnPageChangeListener);

            if ( AppStateManager.getInstance().getPage()>=0 ) {
                vpPager.setCurrentItem(AppStateManager.getInstance().getPage());
            } else {
                vpPager.setCurrentItem(adapterViewPager.getCount());
                AppStateManager.getInstance().setPage(adapterViewPager.getCount());
            }

            resultOnPageChangeListener.setEnabled(true);

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
            Logger.debug("Invisible pager");
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
            showSummary=false;
            ActionMenuItemView otherItem = (ActionMenuItemView)findViewById(R.id.action_results);
            otherItem.getItemData().getIcon().setAlpha(50);
            ActionMenuItemView thisItem = (ActionMenuItemView)findViewById(R.id.action_summary);
            thisItem.getItemData().getIcon().setAlpha(255);
            if ( watches.get(position).getId()>-1 ) {
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
                    prepareResultPager(vpPager, selectedWatch.getId(), showSummary);
                    fab.setVisibility(View.VISIBLE);
                }
            } else {
                // "Add watch"
                Intent addWatchIntent = new Intent(getApplicationContext(), EditWatchActivity.class);
                startActivity(addWatchIntent);
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
            menu.findItem(R.id.action_results).getIcon().setAlpha(50);
            menu.findItem(R.id.action_summary).getIcon().setAlpha(255);
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

        if ( id == R.id.action_summary) {
            showSummary=true;
            item.getIcon().setAlpha(50);
            ActionMenuItemView otherItem = (ActionMenuItemView)findViewById(R.id.action_results);
            otherItem.getItemData().getIcon().setAlpha(255);

            ViewPager vpPager = (ViewPager) findViewById(R.id.pager);
            prepareResultPager(vpPager, selectedWatch.getId(), showSummary);
            return true;
        }

        if ( id == R.id.action_results ) {
            showSummary=false;
            item.getIcon().setAlpha(50);
            ActionMenuItemView otherItem = (ActionMenuItemView)findViewById(R.id.action_summary);
            otherItem.getItemData().getIcon().setAlpha(255);

            ViewPager vpPager = (ViewPager) findViewById(R.id.pager);
            prepareResultPager(vpPager, selectedWatch.getId(), showSummary);
            return true;
        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return false;
    }


    @Override
    public void processFinish(List<Article> articles) {
        Logger.debug("RSS articles to display=" + articles);
        if ( articles!=null && !articles.isEmpty()) {
            MaterialDialog dialog = new UhrenbastlerRssFeedDisplayDialog(this, articles).getDialog();
        }
    }
}
