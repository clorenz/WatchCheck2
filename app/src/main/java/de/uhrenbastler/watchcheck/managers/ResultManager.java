package de.uhrenbastler.watchcheck.managers;

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import de.uhrenbastler.watchcheck.WatchCheckApplication;
import de.uhrenbastler.watchcheck.tools.Logger;
import watchcheck.db.Log;
import watchcheck.db.LogDao;

/**
 * Created by clorenz on 22.12.14.
 */
public class ResultManager {

    static SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

    public static List<Log> getLogsForWatchAndPeriod(Context context, long watchId, int period) {
        LogDao logDao = ((WatchCheckApplication)context.getApplicationContext()).getDaoSession().getLogDao();
        return logDao.queryBuilder().where(LogDao.Properties.WatchId.eq(watchId),
                LogDao.Properties.Period.eq(period)).orderAsc(LogDao.Properties.ReferenceTime).list();
    }

    public static Log getLastLogForWatch(Context context, long watchId) {
        LogDao logDao = ((WatchCheckApplication)context.getApplicationContext()).getDaoSession().getLogDao();
        List<Log> logs = logDao.queryBuilder().where(LogDao.Properties.WatchId.eq(watchId)).orderDesc(LogDao.Properties.ReferenceTime).list();
        if (logs!=null && !logs.isEmpty())
            return logs.get(0);

        return null;
    }

    public static List<List<Log>> getLogsForWatch(Context context, long watchId) {
        LogDao logDao = ((WatchCheckApplication)context.getApplicationContext()).getDaoSession().getLogDao();
        List<Log> allLogs = logDao.queryBuilder().where(LogDao.Properties.WatchId.eq(watchId)).
                orderAsc(LogDao.Properties.Period, LogDao.Properties.ReferenceTime).list();

        List<List<Log>> ret = new ArrayList<List<Log>>();
        ret.add(allLogs);

        return ret;
    }

    public static List<Integer> getPeriodsForWatch(Context context,  long watchId) {
        //TODO List<Log> periodLogs =  Log.findWithQuery(Log.class,"Select * from Log where watch_id=? group by period",watchId+"");
        LogDao logDao = ((WatchCheckApplication)context.getApplicationContext()).getDaoSession().getLogDao();
        List<Log> periodLogs = logDao.queryRawCreate("where watch_id=? group by period", watchId).list();

        List<Integer> ret = new ArrayList<Integer>();
        for ( Log log : periodLogs ) {
            ret.add(log.getPeriod());
        }

        return ret;
    }

    public static String getPeriodStartDate(Context context, long watchId, int period) {
        LogDao logDao = ((WatchCheckApplication)context.getApplicationContext()).getDaoSession().getLogDao();
        List<Log> startLogs = getLogsForWatchAndPeriod(context, watchId, period);

        if ( startLogs!=null && !startLogs.isEmpty()) {
            return sdf.format(startLogs.get(0).getReferenceTime());
        } else {
            return "";
        }
    }


    public static Long getPeriodStartMillis(Context context, long watchId, int period) {
        LogDao logDao = ((WatchCheckApplication)context.getApplicationContext()).getDaoSession().getLogDao();
        List<Log> startLogs = getLogsForWatchAndPeriod(context, watchId, period);

        if ( startLogs!=null && !startLogs.isEmpty()) {
            return startLogs.get(0).getReferenceTime().getTime();
        } else {
            return 0L;
        }
    }

    public static String getPeriodEndDate(Context context, long watchId, int period) {
        LogDao logDao = ((WatchCheckApplication)context.getApplicationContext()).getDaoSession().getLogDao();
        List<Log> endLogs = getLogsForWatchAndPeriod(context, watchId, period);

        if ( endLogs!=null && !endLogs.isEmpty()) {
            return sdf.format(endLogs.get(endLogs.size() - 1).getReferenceTime());
        } else {
            return "";
        }
    }

    public static Long getPeriodEndMillis(Context context, long watchId, int period) {
        LogDao logDao = ((WatchCheckApplication)context.getApplicationContext()).getDaoSession().getLogDao();
        List<Log> endLogs = getLogsForWatchAndPeriod(context, watchId, period);

        if ( endLogs!=null && !endLogs.isEmpty()) {
            return endLogs.get(endLogs.size()-1).getReferenceTime().getTime();
        } else {
            return 0L;
        }
    }


}
