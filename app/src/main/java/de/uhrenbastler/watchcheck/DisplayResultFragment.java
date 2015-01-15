package de.uhrenbastler.watchcheck;

import android.content.Intent;
import android.os.Bundle;
import android.service.media.MediaBrowserService;
import android.support.v4.app.Fragment;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.shamanland.fab.FloatingActionButton;
import com.shamanland.fab.ShowHideOnScroll;

import java.util.Date;
import java.util.List;

import de.uhrenbastler.watchcheck.managers.ResultManager;
import de.uhrenbastler.watchcheck.tools.Logger;
import de.uhrenbastler.watchcheck.views.ResultListAdapter;
import watchcheck.db.Log;
import watchcheck.db.LogDao;
import watchcheck.db.Watch;
import watchcheck.db.WatchDao;

/**
 * Created by clorenz on 17.12.14.
 */
public class DisplayResultFragment extends Fragment {

    // Store instance variables
    private Watch currentWatch;
    private List<Log> log=null;
    private Log lastLog;
    private long watchId;
    private int period;
    private ArrayAdapter resultListAdapter;
    private ListView listView;

    // newInstance constructor for creating fragment with arguments
    public static DisplayResultFragment newInstance(Long watchId, int page) {
        DisplayResultFragment fragmentFirst = new DisplayResultFragment();
        Bundle args = new Bundle();
        args.putLong("watchId", watchId);
        args.putInt("period", page);
        fragmentFirst.setArguments(args);
        return fragmentFirst;
    }

    // Store instance variables based on arguments passed
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        watchId=getArguments().getLong("watchId");
        period=getArguments().getInt("period");
        log = ResultManager.getLogsForWatchAndPeriod(getActivity().getApplicationContext(),watchId, period);
        lastLog = ResultManager.getLastLogForWatch(getActivity().getApplicationContext(),watchId);
        WatchDao watchDao = ((WatchCheckApplication)getActivity().getApplicationContext()).getDaoSession().getWatchDao();
        currentWatch = watchDao.load(watchId);
    }


    @Override
    public void onResume() {
        super.onResume();
        log = ResultManager.getLogsForWatchAndPeriod(getActivity().getApplicationContext(),watchId, period);
        lastLog = ResultManager.getLastLogForWatch(getActivity().getApplicationContext(),watchId);
        if ( listView!=null ) {
            resultListAdapter.clear();
            resultListAdapter.addAll(log);
            resultListAdapter.notifyDataSetChanged();
            listView.invalidateViews();
        }
    }

    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_display_result, container, false);
        listView = (ListView) view.findViewById(R.id.resultListView);
        resultListAdapter = new ResultListAdapter(this.getActivity().getApplicationContext(), log);
        listView.setAdapter(resultListAdapter);
        registerForContextMenu(listView);

        FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.buttonAddLog);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent checkWatchIntent = new Intent(getActivity(),CheckWatchActivity.class);
                checkWatchIntent.putExtra(CheckWatchActivity.EXTRA_WATCH, currentWatch);
                checkWatchIntent.putExtra(CheckWatchActivity.EXTRA_LAST_LOG, lastLog);
                startActivity(checkWatchIntent);
            }
        });
        listView.setOnTouchListener(new ShowHideOnScroll(fab));

        /*
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Log log = (Log) parent.getItemAtPosition(position);
                Logger.debug("Position="+position+", id="+id+", item="+log.getId());
                return false;
            }
        });
        */


        return view;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if ( v.getId() == R.id.resultListView) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            Log logToHandle = (Log)listView.getAdapter().getItem(info.position);
            info.id=logToHandle.getId();            // Yuck!
            menu.setHeaderTitle("Log from "+logToHandle.getReferenceTime());
            String[] menuItems = getResources().getStringArray(R.array.resultlist_contextmenu);
            for (int i = 0; i<menuItems.length; i++) {
                menu.add(Menu.NONE, i, i, menuItems[i]);
            }
        }
    }


    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        int menuItemIndex = item.getItemId();

        LogDao logDao = ((WatchCheckApplication)getActivity().getApplicationContext()).getDaoSession().getLogDao();
        Log logToHandle = logDao.load(info.id);


        switch ( menuItemIndex ) {
            case 0: Logger.debug("Edit item "+logToHandle.getReferenceTime());
                break;
            case 1: Logger.debug("Delete item "+logToHandle.getReferenceTime());
                break;
        }



        return true;
    }
}
