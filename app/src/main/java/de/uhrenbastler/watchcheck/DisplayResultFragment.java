package de.uhrenbastler.watchcheck;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.gc.materialdesign.views.ButtonFloat;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;

import de.uhrenbastler.watchcheck.managers.AppStateManager;
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
    private ArrayAdapter resultAdapter;
    private ListView listView;
    private TextView averageDeviation;
    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    private LogDao logDao;
    private WatchDao watchDao;
    private boolean displaySummary;
    private View resultView;


    // newInstance constructor for creating fragment with arguments
    public static DisplayResultFragment newInstance(Long watchId, int page, boolean displaySummary) {
        DisplayResultFragment fragmentFirst = new DisplayResultFragment();
        Bundle args = new Bundle();
        args.putLong("watchId", watchId);
        args.putInt("period", page);
        fragmentFirst.setArguments(args);
        fragmentFirst.displaySummary = displaySummary;
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

        log = ResultManager.getLogsForWatchAndPeriod(getActivity().getApplicationContext(), watchId, period);

        lastLog = ResultManager.getLastLogForWatch(getActivity().getApplicationContext(), watchId);

        HashMap<String, Double> deviations = new DeviationCalculator(log).getDeviations();

        if ( displaySummary ) {
            if ( resultView!=null ) {
                displayDeviation(deviations.get(Deviations.DU.name()), (TextView) resultView.findViewById(R.id.summaryDialUp), R.string.deviation_format, R.string.empty_value);
                displayDeviation(deviations.get(Deviations.DD.name()), (TextView) resultView.findViewById(R.id.summaryDialDown), R.string.deviation_format, R.string.empty_value);
                displayDeviation(deviations.get(Deviations.O3.name()), (TextView) resultView.findViewById(R.id.summary3o), R.string.deviation_format, R.string.empty_value);
                displayDeviation(deviations.get(Deviations.O6.name()), (TextView) resultView.findViewById(R.id.summary6o), R.string.deviation_format, R.string.empty_value);
                displayDeviation(deviations.get(Deviations.O9.name()), (TextView) resultView.findViewById(R.id.summary9o), R.string.deviation_format, R.string.empty_value);
                displayDeviation(deviations.get(Deviations.O12.name()), (TextView) resultView.findViewById(R.id.summary12o), R.string.deviation_format, R.string.empty_value);
                displayDeviation(deviations.get(Deviations.WORN.name()), (TextView) resultView.findViewById(R.id.summary_worn), R.string.deviation_format, R.string.empty_value);
                displayDeviation(deviations.get(Deviations.ALL.name()), averageDeviation, R.string.deviation_format, R.string.empty_value);
            }
        } else {
            if (listView != null) {
                resultAdapter.clear();
                resultAdapter.addAll(log);
                resultAdapter.notifyDataSetChanged();
                listView.invalidateViews();
                preparePlusButton();
            }
            displayDeviation(deviations.get(Deviations.ALL.name()), averageDeviation, R.string.list_average_deviation, R.string.list_no_average_deviation);
        }
    }

    private void calculateAverageDeviation(int stringId, int stringNoDeviationId) {
        // avg. deviation
        if (averageDeviation != null) {
            String avgDeviationFormat = getString(stringId);
            if (log.size() > 1) {
                // We can calculate the avg. deviation only if we have at least one daily rate!
                long diffReferenceMillis = log.get(log.size() - 1).getReferenceTime().getTime() - log.get(0).getReferenceTime().getTime();
                long diffWatchMillis = log.get(log.size() - 1).getWatchTime().getTime() - log.get(0).getWatchTime().getTime();

                double diffReferenceInDays = (double) diffReferenceMillis / (double) 86400000d;
                double avgDeviation = ((double) diffWatchMillis / diffReferenceInDays) / 1000 - 86400d;

                averageDeviation.setText(String.format(avgDeviationFormat, avgDeviation));
            } else {
                averageDeviation.setText(getString(stringNoDeviationId));
            }
        }
    }

    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (displaySummary) {
            return onCreateSummaryView(inflater, container, savedInstanceState);
        } else {
            return onCreateResultView(inflater, container, savedInstanceState);
        }
    }


    private View onCreateResultView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        resultView = inflater.inflate(R.layout.fragment_display_result, container, false);
        listView = (ListView) resultView.findViewById(R.id.resultListView);
        averageDeviation = (TextView) resultView.findViewById(R.id.result_footer);
        resultAdapter = new ResultListAdapter(this.getActivity().getApplicationContext(), log);
        listView.setAdapter(resultAdapter);
        HashMap<String,Double> deviations = new DeviationCalculator(log).getDeviations();

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
                    resultAdapter.clear();
                    resultAdapter.addAll(log);
                    resultAdapter.notifyDataSetChanged();
                    listView.invalidateViews();
                }
                return true;
            }
        });

        Logger.debug("Before avg. deviation");
        displayDeviation(deviations.get(Deviations.ALL.name()), averageDeviation,R.string.list_average_deviation,R.string.list_no_average_deviation);

        return resultView;
    }


    private View onCreateSummaryView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_display_summary, container, false);
        averageDeviation = (TextView) view.findViewById(R.id.summary_overall);
        HashMap<String,Double> deviations = new DeviationCalculator(log).getDeviations();
        Logger.debug("Deviations="+deviations);

        displayDeviation(deviations.get(Deviations.DU.name()), (TextView) view.findViewById(R.id.summaryDialUp), R.string.deviation_format, R.string.empty_value);
        displayDeviation(deviations.get(Deviations.DD.name()), (TextView) view.findViewById(R.id.summaryDialDown), R.string.deviation_format, R.string.empty_value);
        displayDeviation(deviations.get(Deviations.O3.name()), (TextView) view.findViewById(R.id.summary3o), R.string.deviation_format, R.string.empty_value);
        displayDeviation(deviations.get(Deviations.O6.name()), (TextView) view.findViewById(R.id.summary6o), R.string.deviation_format, R.string.empty_value);
        displayDeviation(deviations.get(Deviations.O9.name()), (TextView) view.findViewById(R.id.summary9o), R.string.deviation_format, R.string.empty_value);
        displayDeviation(deviations.get(Deviations.O12.name()), (TextView) view.findViewById(R.id.summary12o), R.string.deviation_format, R.string.empty_value);
        displayDeviation(deviations.get(Deviations.WORN.name()), (TextView) view.findViewById(R.id.summary_worn), R.string.deviation_format, R.string.empty_value);
        displayDeviation(deviations.get(Deviations.ALL.name()), averageDeviation, R.string.deviation_format, R.string.empty_value);

        preparePlusButton();

        return view;
    }


    private void displayDeviation(Double deviation, TextView tvDeviation, int formatId, int emptyId) {
        String avgDeviationFormat = getString(formatId);

        if ( deviation!=null ) {
            tvDeviation.setText(String.format(avgDeviationFormat, deviation));
        } else {
            tvDeviation.setText(getString(emptyId));
        }
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