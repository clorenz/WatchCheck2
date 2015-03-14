package de.uhrenbastler.watchcheck;

import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import watchcheck.db.Log;

/**
 * Created by clorenz on 12.03.15.
 */
public class DeviationCalculator {

    HashMap<String,Double> deviations = new HashMap<String,Double>();
    HashMap<String,Long> summaryReferenceInPosition = new HashMap<String,Long>();
    HashMap<String,Long> summaryWatchInPosition = new HashMap<String,Long>();

    List<Log> logs;

    public DeviationCalculator(List<Log> logs) {
        this.logs = logs;

        calculateDeviations();
    }


    private void calculateDeviations() {
        for ( int i=1; i<logs.size(); i++ ) {
            Log log = logs.get(i);
            String position = log.getPosition();
            if (StringUtils.isBlank(position))
                position=Deviations.WORN.name();

            long diffReference = logs.get(i).getReferenceTime().getTime() - logs.get(i - 1).getReferenceTime().getTime();
            long diffWatch = logs.get(i).getWatchTime().getTime() - logs.get(i - 1).getWatchTime().getTime();

            addDiffReferenceToSummary(position, diffReference);
            addDiffWatchToSummary(position, diffWatch);

            addDiffReferenceToSummary(Deviations.ALL.name(), diffReference);
            addDiffWatchToSummary(Deviations.ALL.name(), diffWatch);
        }

        for ( String position: summaryReferenceInPosition.keySet() ) {
            double diffReference = summaryReferenceInPosition.get(position);
            double diffWatch = summaryWatchInPosition.get(position);

            double factor = (86400d * 1000) / ((double)diffReference);

            double deviation = ((((double)diffWatch) * factor)/1000) - 86400d;

            deviations.put(position, deviation);
        }
    }

    private void addDiffWatchToSummary(String position, long diffWatch) {
        Long summaryWatch = summaryWatchInPosition.get(position);
        if (summaryWatch == null) {
            summaryWatch = diffWatch;
        } else {
            summaryWatch += diffWatch;
        }
        summaryWatchInPosition.put(position,summaryWatch);
    }

    private void addDiffReferenceToSummary(String position, long diffReference) {
        Long summaryReference = summaryReferenceInPosition.get(position);
        if (summaryReference == null) {
            summaryReference = diffReference;
        } else {
            summaryReference += diffReference;
        }
        summaryReferenceInPosition.put(position,summaryReference);
    }

    public HashMap<String,Double> getDeviations() {
        return deviations;
    }
}
