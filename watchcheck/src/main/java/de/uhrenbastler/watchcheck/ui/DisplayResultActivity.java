package de.uhrenbastler.watchcheck.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.uhrenbastler.watchcheck.R;
import de.uhrenbastler.watchcheck.data.Exporter;
import de.uhrenbastler.watchcheck.data.Importer;
import de.uhrenbastler.watchcheck.data.Watch;
import de.uhrenbastler.watchcheck.db.WatchCheckDBHelper;
import de.uhrenbastler.watchcheck.tools.Logger;

public class DisplayResultActivity extends Activity implements android.app.ActionBar.OnNavigationListener {

    private static final String PREFERENCE_CURRENT_WATCH = "currentWatch";
    private int selectedWatchId;
    SelectWatchArrayAdapter adapter;
    List<Watch> watches;

    // TODO: Ensure, that the currently selected watch is shown!
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_result);

        selectedWatchId = retrieveCurrentWatch();
        if ( selectedWatchId==-1) {
            selectedWatchId=1;
            Logger.warn("No watch selected. Using first watch as default");
            persistCurrentWatch(selectedWatchId);
        }
        Logger.debug("Current watch has got ID "+selectedWatchId);

        // Set up action bar with a drop down list of watches
        final android.app.ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

        watches = WatchCheckDBHelper.getAllWatchesFromDatabaseAndPrependSelectedWatch(selectedWatchId,
                getResources().getString(R.string.addWatch), this.getContentResolver());

        // Specify a SpinnerAdapter to populate the dropdown list.
        adapter = new SelectWatchArrayAdapter(this, actionBar.getThemedContext(),
                R.layout.watchcheck_spinner_dropdown_item, R.id.watchName, watches, selectedWatchId);

        adapter.setDropDownViewResource(R.layout.watchcheck_spinner_dropdown_item);

        // Set up the dropdown list navigation in the action bar.
        actionBar.setListNavigationCallbacks(adapter, this);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }


    private int retrieveCurrentWatch() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        int currentWatchId = preferences.getInt(PREFERENCE_CURRENT_WATCH, -1);

        Logger.debug("Retrieving current watch with ID="+currentWatchId);

        int selectedWatchId = WatchCheckDBHelper.validateWatchId(currentWatchId, getContentResolver());

        Logger.debug("Selected Watch ID from preferences = "+selectedWatchId);

        return selectedWatchId;
    }


    private void persistCurrentWatch(int currentWatchId) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(PREFERENCE_CURRENT_WATCH, currentWatchId);
        editor.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.display_result, menu);
        return super.onCreateOptionsMenu(menu);

        // TODO: If no watch was yet selected, hide the "Edit this watch" and "Delete this watch" menu items
    }

    /**
     * Handles the overflow menu with "about", "import" and "export".
     * Corresponding menu definition is display_result.xml
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menuAbout:
                displayAboutDialog();
                return true;

            case R.id.menuExportData:
                displayExportDialog();
                return true;

            case R.id.menuImportData:
                displayImportDialog();
                return true;

            case R.id.menuEditThisWatch:
                Intent editWatchIntent = new Intent(DisplayResultActivity.this, EditWatchActivity.class);
                editWatchIntent.putExtra(Watch.Watches._ID, selectedWatchId);
                startActivity(editWatchIntent);
                return true;

            case R.id.menuDeleteThisWatch:
                // TODO: Display delete watch dialog
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void displayAboutDialog() {
        PackageInfo pInfo=null;
        try {
            pInfo = this.getPackageManager().getPackageInfo(this.getPackageName(), PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {}
        new AlertDialog.Builder(this).setTitle(this.getString(R.string.app_name)+
                "\nVersion: "+(pInfo!=null?pInfo.versionName:"unknown"))
                .setCancelable(true).setIcon(R.drawable.ic_launcher)
                .setMessage(this.getString(R.string.app_about))
                .setPositiveButton(this.getString(android.R.string.ok), null).create().show();
    }

    private void displayExportDialog() {
        try {
            String filename = new Exporter().export(this);
            new AlertDialog.Builder(this).setTitle(this.getString(R.string.dataExported))
                    .setMessage(filename)
                    .setCancelable(true)
                    .setPositiveButton(this.getString(android.R.string.ok), null).create().show();
        } catch (Exception e) {
            Logger.error(e.getMessage(), e);
        }
    }

    private void displayImportDialog() {
        new AlertDialog.Builder(this).setTitle(this.getString(R.string.warning))
                .setMessage(this.getString(R.string.reloadDatabase))
                .setCancelable(false)
                .setPositiveButton(this.getString(android.R.string.yes),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    new Importer().doImport(DisplayResultActivity.this);
                                    Intent intent = new Intent(DisplayResultActivity.this, FinActivity.class).
                                            setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY);
                                    finish();
                                    startActivity(intent);
                                } catch (FileNotFoundException e) {
                                    Logger.error(e.getMessage(),e);
                                }
                            }
                        })
                .setNegativeButton(this.getString(android.R.string.no), null)
                .create().show();
    }


    // Required for setting the watch
    //
    // TODO: Handle ID -1 = add watch
    @Override
    public boolean onNavigationItemSelected(int position, long id) {
        // When the given dropdown item is selected, show its contents in the
        // container view.
        Watch selectedWatch = (Watch)(adapter.getItem(position));

        Logger.info("Selected item with id="+id+" at position "+position+" with data id="+selectedWatch.getId()+", name="+selectedWatch.getName());

        /* android.app.Fragment fragment = new DummySectionFragment();
        Bundle args = new Bundle();
        args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, position + 1);
        fragment.setArguments(args);
        getFragmentManager().beginTransaction()
                .replace(R.id.container, fragment).commit();
                */

        MeasureFragement measureFragment = (MeasureFragement) getFragmentManager().findFragmentById(R.id.measureFragment);
        Logger.info("measureFragment="+measureFragment);
        if ( measureFragment==null ) {
            getFragmentManager().beginTransaction().replace(R.id.measureFragment, new MeasureFragement()).commit();
        }

        if ( selectedWatch.getId()>-1) {
            persistCurrentWatch((int) selectedWatch.getId());
        } else {
            // Maybe insert an "edit watch" fragement???
        }
        return true;
    }



    /**
     * A placeholder fragment containing a simple view. Currently it displays the number of the watch (starting with 1) in the main container
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_display_result, container, false);
            return rootView;
        }
    }


    /**
     * * A dummy fragment
     */

    public static class DummySectionFragment extends android.app.Fragment {

        public static final String ARG_SECTION_NUMBER = "placeholder_text";

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            TextView textView = new TextView(getActivity());
            textView.setGravity(Gravity.CENTER);
            textView.setText(Integer.toString(getArguments().getInt(ARG_SECTION_NUMBER)));
            return textView;
        }
    }


    /**
     * A very basic "finishing" activity, which is required for closing an app
     */
    public static class FinActivity extends Activity {

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            finish();
        }
    }

}
