package de.uhrenbastler.watchcheck;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.shamanland.fab.FloatingActionButton;
import com.shamanland.fab.ShowHideOnScroll;

import java.util.List;

import de.uhrenbastler.watchcheck.managers.ResultManager;
import de.uhrenbastler.watchcheck.tools.Logger;
import de.uhrenbastler.watchcheck.views.ResultListAdapter;
import watchcheck.db.Log;
import watchcheck.db.Watch;
import watchcheck.db.WatchDao;

/**
 * Created by clorenz on 17.12.14.
 */
public class DisplayResultFragment extends Fragment {

    // Store instance variables
    private Watch currentWatch;
    private List<Log> log;
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


        return view;
    }

}
