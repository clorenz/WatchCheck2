package de.uhrenbastler.watchcheck;

import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import watchcheck.db.Log;

/**
 * Created by clorenz on 12.03.15.
 */
public class DeviationCalculator {

    public static final String ZO = "ZO";
    public static final String ZU = "ZU";
    public static final String O3 = "3O";
    public static final String O6 = "6O";
    public static final String O9 = "9O";
    public static final String O12 = "12O";

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
            long diffReference = logs.get(i).getReferenceTime().getTime() - logs.get(i - 1).getReferenceTime().getTime();
            long diffWatch = logs.get(i).getWatchTime().getTime() - logs.get(i - 1).getWatchTime().getTime();

            Long summaryReference = summaryReferenceInPosition.get(position);
            if (summaryReference == null) {
                summaryReference = diffReference;
            } else {
                summaryReference += diffReference;
            }
            summaryReferenceInPosition.put(position,summaryReference);

            Long summaryWatch = summaryWatchInPosition.get(position);
            if (summaryWatch == null) {
                summaryWatch = diffWatch;
            } else {
                summaryWatch += diffWatch;
            }
            summaryWatchInPosition.put(position,summaryWatch);
        }

        for ( String position: summaryReferenceInPosition.keySet() ) {
            double diffReference = summaryReferenceInPosition.get(position);
            double diffWatch = summaryWatchInPosition.get(position);

            double factor = (86400d * 1000) / ((double)diffReference);

            double deviation = ((((double)diffWatch) * factor)/1000) - 86400d;

            deviations.put(position, deviation);
        }
    }

    public HashMap<String,Double> getDeviations() {
        return deviations;
    }
}
