package de.uhrenbastler.watchcheck;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.internal.widget.AdapterViewCompat;
import android.view.ContextMenu;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.gc.materialdesign.views.ButtonFloat;
import com.gc.materialdesign.widgets.Dialog;

import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.List;

import de.uhrenbastler.watchcheck.managers.ResultManager;
import de.uhrenbastler.watchcheck.managers.WatchManager;
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
    private List<Log> log = null;
    private Log lastLog;
    private long watchId;
    private int period;
    private ArrayAdapter resultListAdapter;
    private ListView listView;
    private TextView averageDeviation;
    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    private LogDao logDao;
    private WatchDao watchDao;


    // newInstance constructor for creating fragment with arguments
    public static DisplayResultFragment newInstance(Long watchId, int page) {
        Logger.debug("No resume, but new instance: watchId="+watchId);
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
        watchId = getArguments().getLong("watchId");
        period = getArguments().getInt("period");
        log = ResultManager.getLogsForWatchAndPeriod(getActivity().getApplicationContext(), watchId, period);
        lastLog = ResultManager.getLastLogForWatch(getActivity().getApplicationContext(), watchId);
        watchDao = ((WatchCheckApplication) getActivity().getApplicationContext()).getDaoSession().getWatchDao();
        logDao = ((WatchCheckApplication) getActivity().getApplicationContext()).getDaoSession().getLogDao();
        currentWatch = watchDao.load(watchId);
    }


    @Override
    public void onResume() {
        super.onResume();

        currentWatch = WatchManager.retrieveCurrentWatch(this.getActivity());
        if ( currentWatch!=null) {
            watchId = currentWatch.getId();
        }

        Logger.debug("on resume: currentWatchId=" + watchId);
        log = ResultManager.getLogsForWatchAndPeriod(getActivity().getApplicationContext(), watchId, period);

        lastLog = ResultManager.getLastLogForWatch(getActivity().getApplicationContext(), watchId);
        if (listView != null) {
            resultListAdapter.clear();
            resultListAdapter.addAll(log);
            resultListAdapter.notifyDataSetChanged();
            listView.invalidateViews();
            preparePlusButton();
        }
        calculateAverageDeviation();


    }

    private void calculateAverageDeviation() {
        // avg. deviation
        if (averageDeviation != null) {
            String avgDeviationFormat = getString(R.string.list_average_deviation);
            if (log.size() > 1) {
                // We can calculate the avg. deviation only if we have at least one daily rate!
                long diffReferenceMillis = log.get(log.size() - 1).getReferenceTime().getTime() - log.get(0).getReferenceTime().getTime();
                long diffWatchMillis = log.get(log.size() - 1).getWatchTime().getTime() - log.get(0).getWatchTime().getTime();

                double diffReferenceInDays = (double) diffReferenceMillis / (double) 86400000d;
                double avgDeviation = ((double) diffWatchMillis / diffReferenceInDays) / 1000 - 86400d;

                averageDeviation.setText(String.format(avgDeviationFormat, avgDeviation));
            } else {
                averageDeviation.setText(getString(R.string.list_no_average_deviation));
            }
        }
    }

    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_display_result, container, false);
        listView = (ListView) view.findViewById(R.id.resultListView);
        averageDeviation = (TextView) view.findViewById(R.id.result_footer);
        resultListAdapter = new ResultListAdapter(this.getActivity().getApplicationContext(), log);
        listView.setAdapter(resultListAdapter);

        preparePlusButton();
        listView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return false;
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Log logToHandle = ((Log) listView.getAdapter().getItem(position));
                Intent addLogIntent = new Intent(getActivity(), AddLogActivity.class);
                addLogIntent.putExtra(AddLogActivity.EXTRA_WATCH, currentWatch);
                addLogIntent.putExtra(AddLogActivity.EXTRA_EDIT_LOG, logToHandle);
                startActivity(addLogIntent);
                if (listView != null) {
                    resultListAdapter.clear();
                    resultListAdapter.addAll(log);
                    resultListAdapter.notifyDataSetChanged();
                    listView.invalidateViews();
                }
                return true;
            }
        });

        Logger.debug("Before avg. deviation");
        calculateAverageDeviation();

        return view;
    }

    private void preparePlusButton() {
        Logger.debug("Plus button: currentWatch="+currentWatch+" with id="+currentWatch.getId());
        ButtonFloat fab = (ButtonFloat) getActivity().findViewById(R.id.buttonAddLog);
        fab.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent checkWatchIntent = new Intent(getActivity(), CheckWatchActivity.class);
                checkWatchIntent.putExtra(CheckWatchActivity.EXTRA_WATCH, currentWatch);
                checkWatchIntent.putExtra(CheckWatchActivity.EXTRA_LAST_LOG, lastLog);
                startActivity(checkWatchIntent);
            }
        });
    }
}