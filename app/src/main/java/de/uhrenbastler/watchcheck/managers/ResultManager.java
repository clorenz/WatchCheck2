package de.uhrenbastler.watchcheck.managers;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import de.uhrenbastler.watchcheck.models.Log;

/**
 * Created by clorenz on 22.12.14.
 */
public class ResultManager {

    static SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");

    public static List<Log> getLogsForWatchAndPeriod(long watchId, int period) {
        return Log.find(Log.class, "watch_id=? and period=?", new String[]{"" + watchId, "" + period});
    }

    public static List<List<Log>> getLogsForWatch(long watchId) {
        List<Log> allLogs = Log.find(Log.class, "watch_id=?", new String[]{"" + watchId}, null, "period,reference_time asc",null);

        List<List<Log>> ret = new ArrayList<List<Log>>();
        ret.add(allLogs);

        return ret;
    }

    public static List<Integer> getPeriodsForWatch(long watchId) {
        List<Log> periodLogs =  Log.findWithQuery(Log.class,"Select * from Log where watch_id=? group by period",watchId+"");
        List<Integer> ret = new ArrayList<Integer>();
        for ( Log log : periodLogs ) {
            ret.add(log.getPeriod());
        }

        return ret;
    }

    public static String getPeriodStartDate(long watchId, int period) {
        List<Log> startLogs = Log.find(Log.class, "watch_id=? and period=?", new String[]{"" + watchId, "" + period},null,"reference_time asc","1");
        if ( startLogs!=null && !startLogs.isEmpty()) {
            return sdf.format(startLogs.get(0).getReferenceTime());
        } else {
            return "";
        }
    }

    public static String getPeriodEndDate(long watchId, int period) {
        List<Log> endLogs = Log.find(Log.class, "watch_id=? and period=?", new String[]{"" + watchId, "" + period},null,"reference_time desc","1");
        if ( endLogs!=null && !endLogs.isEmpty()) {
            return sdf.format(endLogs.get(0).getReferenceTime());
        } else {
            return "";
        }
    }
}
