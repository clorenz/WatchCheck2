package de.uhrenbastler.watchcheck;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.app.Activity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.gc.materialdesign.widgets.Dialog;

import java.util.ArrayList;
import java.util.List;

import de.uhrenbastler.watchcheck.tools.DataExporter;
import de.uhrenbastler.watchcheck.tools.DataImporter;
import de.uhrenbastler.watchcheck.tools.Logger;
import de.uhrenbastler.watchcheck.views.EditWatchFragment;
import de.uhrenbastler.watchcheck.views.SelectWatchArrayAdapter;
import watchcheck.db.Watch;

/**
 * Fragment used for managing interactions for and presentation of a navigation drawer.
 * See the <a href="https://developer.android.com/design/patterns/navigation-drawer.html#Interaction">
 * design guidelines</a> for a complete explanation of the behaviors implemented here.
 */
public class NavigationDrawerFragment extends Fragment {

    /**
     * Remember the position of the selected item.
     */
    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";

    /**
     * Per the design guidelines, you should show the drawer on launch until the user manually
     * expands it. This shared preference tracks this.
     */
    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";

    /**
     * A pointer to the current callbacks instance (the Activity).
     */
    private NavigationDrawerCallbacks mCallbacks;

    /**
     * Helper component that ties the action bar to the navigation drawer.
     */
    private ActionBarDrawerToggle mDrawerToggle;

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerListView;
    private View mFragmentContainerView;

    private int mCurrentSelectedPosition = 0;
    private boolean mFromSavedInstanceState;
    private boolean mUserLearnedDrawer;

    SelectWatchArrayAdapter adapter;
    List<Watch> watches = new ArrayList<Watch>();
    Watch selectedWatch;


    public NavigationDrawerFragment() {
    }

    public void setSelectedWatch(Watch selectedWatch) {
        this.selectedWatch = selectedWatch;
        mCurrentSelectedPosition = watches.indexOf(selectedWatch);
        mDrawerListView.setItemChecked(mCurrentSelectedPosition, true);
    }

    public void setWatches(List<Watch> watches) {
        this.watches = watches;
        adapter.clear();
        adapter.addAll(watches);
        adapter.notifyDataSetInvalidated();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Read in the flag indicating whether or not the user has demonstrated awareness of the
        // drawer. See PREF_USER_LEARNED_DRAWER for details.
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);

        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            mFromSavedInstanceState = true;
        }

        // Select either the default item (0) or the last selected item.
        //selectItem(mCurrentSelectedPosition);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Indicate that this fragment would like to influence the set of actions in the action bar.
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        LinearLayout drawerLayout = (LinearLayout)inflater.inflate(
                R.layout.fragment_navigation_drawer, container, false);

        mDrawerListView = (ListView) drawerLayout.findViewById(R.id.drawer_listview);
        mDrawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectItem(position);
            }
        });
        mDrawerListView.setLongClickable(true);
        mDrawerListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                return selectItemForEdit(position);
            }
        });

        int selectedWatchId = selectedWatch!=null?selectedWatch.getId().intValue():0;

        adapter = new SelectWatchArrayAdapter(getActivity(), getActivity().getApplicationContext(),
                R.layout.drawer_list_item, R.id.watchName, R.id.watchSerial, watches, selectedWatchId);

        mDrawerListView.setAdapter(adapter);
        mDrawerListView.setItemChecked(mCurrentSelectedPosition, true);

        TextView addWatch = (TextView) drawerLayout.findViewById(R.id.drawer_add_watch);
        addWatch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addWatchIntent = new Intent(getActivity().getApplicationContext(), EditWatchActivity.class);
                startActivity(addWatchIntent);
                mDrawerLayout.closeDrawer(mFragmentContainerView);
            }
        });

        return drawerLayout;
    }

    public boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
    }

    /**
     * Users of this fragment must call this method to set up the navigation drawer interactions.
     *
     * @param fragmentId   The android:id of this fragment in its activity's layout.
     * @param drawerLayout The DrawerLayout containing this fragment's UI.
     */
    public void setUp(int fragmentId, DrawerLayout drawerLayout) {
        mFragmentContainerView = getActivity().findViewById(fragmentId);
        mDrawerLayout = drawerLayout;

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the navigation drawer and the action bar app icon.
        mDrawerToggle = new ActionBarDrawerToggle(
                getActivity(),                    /* host Activity */
                mDrawerLayout,                    /* DrawerLayout object */
                R.drawable.ic_drawer,             /* nav drawer image to replace 'Up' caret */
                R.string.navigation_drawer_open,  /* "open drawer" description for accessibility */
                R.string.navigation_drawer_close  /* "close drawer" description for accessibility */
        ) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (!isAdded()) {
                    return;
                }

                getActivity().supportInvalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (!isAdded()) {
                    return;
                }

                if (!mUserLearnedDrawer) {
                    // The user manually opened the drawer; store this flag to prevent auto-showing
                    // the navigation drawer automatically in the future.
                    mUserLearnedDrawer = true;
                    SharedPreferences sp = PreferenceManager
                            .getDefaultSharedPreferences(getActivity());
                    sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).apply();
                }

                getActivity().supportInvalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }
        };

        // If the user hasn't 'learned' about the drawer, open it to introduce them to the drawer,
        // per the navigation drawer design guidelines.
        if (!mUserLearnedDrawer && !mFromSavedInstanceState) {
            mDrawerLayout.openDrawer(mFragmentContainerView);
        }

        // Defer code dependent on restoration of previous instance state.
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });

        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    private void selectItem(int position) {
        mCurrentSelectedPosition = position;
        if (mDrawerListView != null) {
            mDrawerListView.setItemChecked(position, true);
        }
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mFragmentContainerView);
        }
        if (mCallbacks != null) {
            mCallbacks.onNavigationDrawerItemSelected(position);
        }
    }


    private boolean selectItemForEdit(int position) {
        if (mDrawerListView != null) {
            mDrawerListView.setItemChecked(position, true);
        }
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mFragmentContainerView);
        }
        Watch selectedWatch = watches.get(position);
        if ( selectedWatch.getId() != null) {
            Logger.debug("Selected watch for edit: "+selectedWatch+". Starting new activity");
            mDrawerListView.invalidate();

            Intent editWatchIntent = new Intent(getActivity().getApplicationContext(), EditWatchActivity.class);
            editWatchIntent.putExtra("watch", selectedWatch);
            editWatchIntent.putExtra("id",selectedWatch.getId());
            startActivity(editWatchIntent);
            return true;
        }
        return false;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (NavigationDrawerCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Forward the new configuration the drawer toggle component.
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // If the drawer is open, show the global app actions in the action bar. See also
        // showGlobalContextActionBar, which controls the top-left area of the action bar.
        if (mDrawerLayout != null && isDrawerOpen()) {
            inflater.inflate(R.menu.main, menu);
            showGlobalContextActionBar();
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        switch (item.getItemId()) {
            case R.id.menu_about: {
                PackageInfo pInfo=null;
                try {
                    pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), PackageManager.GET_META_DATA);
                } catch (PackageManager.NameNotFoundException e) {}
                final Dialog aboutDialog = new Dialog(getActivity(),
                        this.getString(R.string.app_name)+" "+
                                (pInfo!=null?pInfo.versionName:"unknown"),
                        this.getString(R.string.app_about));
                aboutDialog.setButtonCancel(null);
                aboutDialog.show();
                return true;
            }
            case R.id.menu_export: {
                try {
                    String filename = new DataExporter().export(getActivity());
                    new AlertDialog.Builder(getActivity())
                            .setTitle(getActivity().getString(R.string.dataExported))
                            .setMessage(filename)
                            .setCancelable(true)
                            .setPositiveButton(this.getString(android.R.string.ok), null).create().show();
                } catch (Exception e) {
                    Logger.error(e.getMessage(),e);
                }
                return true;
            }
            case R.id.menu_import: {
                SimpleFileDialog FileOpenDialog =  new SimpleFileDialog(getActivity(), "Import",
						new SimpleFileDialog.SimpleFileDialogListener(){
                            @Override public void onChosenDir(String chosenDir) {
                                // The code in this function will be executed when the dialog OK button is pushed
                                String filename = chosenDir;
                                try {
                                    new DataImporter().doImport(getActivity(), filename);
                                    updateDrawer();
                                    new AlertDialog.Builder(getActivity())
                                            .setTitle(R.string.dataImported)
                                            .setMessage(filename)
                                            .setCancelable(true)
                                            .setPositiveButton(getActivity().getString(android.R.string.ok), null).create().show();
                                } catch (Exception e) {
                                    Logger.error(e.getMessage(),e);
                                }
                            }
                        }
                ); //You can change the default filename using the public variable "Default_File_Name"
                FileOpenDialog.Default_File_Name = "";
                FileOpenDialog.chooseFile_or_Dir();
                return true;
            }
            case R.id.menu_whatsnew: {
                final Dialog whatsNewDialog = new Dialog(getActivity(),
                        this.getString(R.string.whatsnew),"");
                whatsNewDialog.setButtonCancel(null);
                whatsNewDialog.show();
                whatsNewDialog.getButtonAccept().setText(getString(R.string.ok));
                TextView tvWhatsNew = whatsNewDialog.getMessageTextView();
                Logger.debug("MessageTextView="+tvWhatsNew);
                tvWhatsNew.setText(Html.fromHtml(getString(R.string.changelog)));
                return true;
            }
            case R.id.menu_help: {
                String helpUrl = getActivity().getString(R.string.url_help);
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(helpUrl));
                startActivity(browserIntent);
            }
        }


        return super.onOptionsItemSelected(item);
    }



    /**
     * Per the navigation drawer design guidelines, updates the action bar to show the global app
     * 'context', rather than just what's in the current screen.
     */
    private void showGlobalContextActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setTitle(R.string.app_name);
    }

    private ActionBar getActionBar() {
        return ((ActionBarActivity) getActivity()).getSupportActionBar();
    }


    public void updateDrawer() {
        adapter.notifyDataSetChanged();
        mDrawerListView.invalidate();
        mDrawerListView.refreshDrawableState();
        Logger.debug("Notified adapter");
    }


    /**
     * Callbacks interface that all activities using this fragment must implement.
     */
    public static interface NavigationDrawerCallbacks {
        /**
         * Called when an item in the navigation drawer is selected.
         */
        void onNavigationDrawerItemSelected(int position);
    }

}
