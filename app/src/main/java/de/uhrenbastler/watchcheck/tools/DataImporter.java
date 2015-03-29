package de.uhrenbastler.watchcheck.tools;


import android.support.v4.app.FragmentActivity;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import de.uhrenbastler.watchcheck.WatchCheckApplication;
import watchcheck.db.Log;
import watchcheck.db.LogDao;
import watchcheck.db.Watch;
import watchcheck.db.WatchDao;

/**
 * Created by clorenz on 24.01.15.
 */
public class DataImporter {

    FragmentActivity act;
    List<String> watchesData=new ArrayList<String>();
    List<String> logsData=new ArrayList<String>();
    LogDao logDao;
    WatchDao watchDao;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private boolean lastImportWasAWatch=false;


    public void doImport(FragmentActivity act, String filename) throws Exception {
        logDao = ((WatchCheckApplication)act.getApplicationContext()).getDaoSession().getLogDao();
        watchDao = ((WatchCheckApplication)act.getApplicationContext()).getDaoSession().getWatchDao();

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        new FileInputStream(filename)
                )
        );

        boolean readLogs = false;
        boolean isVersion2 = false;

        try {
            String line;
            while ((line = reader.readLine()) != null) {
                if ("WatchCheck2".equals(line)) {
                    isVersion2 = true;
                } else if (line.startsWith("----------------")) {
                    readLogs = true;          // only version 1
                } else {
                    parseLine(line, isVersion2, readLogs);
                }
            }
        } catch (Exception e) {
            Logger.error("Could not import data: " + e.getMessage(), e);
            return;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                    Logger.error("Could not close input: ", e);
                }
            }
        }

        if (isVersion2) {
            importDataVersion2();
        } else {
            importDataVersion1();
        }
    }


    private void parseLine(String rawData, boolean isVersion2, boolean isVersion1Logs) {
        if ( !isVersion2) {
            // Version 1:
            if ( isVersion1Logs) {
                logsData.add(rawData);
            } else {
                watchesData.add(rawData);
            }
        } else {

            if ( rawData.startsWith("WATCH: ")) {
                watchesData.add(rawData.substring(7));
                lastImportWasAWatch=true;
            } else if ( rawData.startsWith("LOG: ")) {
                logsData.add(rawData.substring(5));
                lastImportWasAWatch=false;
            } else if ( isVersion2 ) {
                Logger.warn("Version 2: Found illegal line, trying to append it: "+rawData);
                // There was a newline in the data. Append it to former line
                if ( lastImportWasAWatch ) {
                    String lastWatchesDataItem = watchesData.get(watchesData.size()-1);
                    lastWatchesDataItem += " " + rawData;
                    watchesData.set(watchesData.size()-1, lastWatchesDataItem);
                    Logger.warn("Fixed newline on watchesData item="+lastWatchesDataItem);
                } else {
                    String lastLogDataItem = logsData.get(logsData.size()-1);
                    lastLogDataItem += " " + rawData;
                    logsData.set(logsData.size()-1, lastLogDataItem);
                    Logger.warn("Fixed newline on logsData item="+lastLogDataItem);
                }
            } else {
                Logger.error("Bogus line on version1 import found: "+rawData);
            }
        }
    }


    // TODO: Buggy, when watches change, periods are broken!! TODO: Sort strings according to watchIDs
    private void importDataVersion1() throws Exception {
        watchDao.deleteAll();
        logDao.deleteAll();

        for ( String watchData : watchesData ) {
            if ( watchData.endsWith("|")) {
                watchData += "null";
            }
            String[] watchDataParts = watchData.split("\\|");
            Logger.debug("WatchData="+ArrayUtils.toString(watchDataParts));
            long watchId = Long.parseLong(watchDataParts[0]);
            String watchName = watchDataParts[1];
            String serial = watchDataParts[2];
            if ( "null".equals(serial)) {
                serial=null;
            }
            Date createDate = null;         // always null - bug in old app!
            String comment = watchDataParts[4];
            if ( "null".equals(comment)) {
                comment=null;
            }
            Watch watch = new Watch(watchId, watchName, serial, createDate, comment);
            watchDao.insert(watch);
        }

        Collections.sort(logsData, new LogsDataSorter());
        Logger.debug("Sorted logsData = ");
        for ( String logData : logsData ) {
            String[] logDataParts = logData.split("\\|");
            Logger.debug(ArrayUtils.toString(logDataParts));
        }
        Logger.debug("-----------------------");


        int period=-1;
        int oldWatchId=-1;
        for ( String logData : logsData ) {
            if ( logData.endsWith("|")) {
                logData += "null";
            }
            String[] logDataParts = logData.split("\\|");
            Logger.debug("LogData="+ArrayUtils.toString(logDataParts));
            long logId = Long.parseLong(logDataParts[0]);
            int watchId = Integer.parseInt(logDataParts[1]);
            if ( watchId!=oldWatchId) {
                oldWatchId=watchId;
                period=-1;
            }
            boolean isNtp = "1".equals(logDataParts[2]);
            Date handyTime = sdf.parse(logDataParts[3]);
            Date referenceTime = new Date(handyTime.getTime() - (int)(1000d * Double.parseDouble(logDataParts[4])));
            long differenceInMillis = (long) (1000d * Double.parseDouble(logDataParts[5]));     // Precise difference in milliseconds

            long watchTimeInMillis = referenceTime.getTime() + differenceInMillis;

            // We assume, that the seconds of the timed watch are always exact zero!
            long watchTimeInMillisPrecisionSeconds = (long)Math.ceil((double)watchTimeInMillis / 1000) * 1000;

            long millisForWatchToZero =  watchTimeInMillisPrecisionSeconds - watchTimeInMillis;

            // This number of millisForWatchToZero has now to be added to the referenceTime
            referenceTime = new Timestamp(referenceTime.getTime() + millisForWatchToZero);

            Timestamp watchTime = new Timestamp(watchTimeInMillisPrecisionSeconds);
            if ( logDataParts[6].equals("1")) {
                // Reset
                period++;
            }
            String position = logDataParts[7];
            if ( "null".equals(position)) {
                position=null;
            }
            int temperature = Integer.parseInt(logDataParts[8]);
            String comment = logDataParts[9];
            if ( "null".equals(comment)) {
                comment=null;
            }

            Log log = new Log(logId, watchId, period, referenceTime, watchTime, position, temperature, comment);
            logDao.insert(log);
        }
    }


    private void importDataVersion2() throws Exception {
        watchDao.deleteAll();
        logDao.deleteAll();

        for (String watchData : watchesData) {
            String[] watchDataParts = watchData.split("\\|");
            Logger.info("watchData='"+watchData+"', parts="+ArrayUtils.toString(watchDataParts));
            long watchId = Long.parseLong(watchDataParts[0]);
            String watchName = watchDataParts[1];
            String serial = null;
            if ( watchDataParts.length>2) {
                serial = watchDataParts[2];
            }
            Date createdAt=null;
            if ( watchDataParts.length>3 && !StringUtils.isBlank(watchDataParts[3])) {
                createdAt = new Date(Long.parseLong(watchDataParts[3]));
            }
            String comment=null;
            if ( watchDataParts.length>4) {
                comment = watchDataParts[4];
            }
            Watch watch = new Watch(watchId, watchName, serial, createdAt, comment);
            watchDao.insert(watch);
        }

        for (String logData : logsData) {
            String[] logDataParts = logData.split("\\|");
            Long logId = Long.parseLong(logDataParts[0]);
            int watchId = Integer.parseInt(logDataParts[1]);
            int period = Integer.parseInt(logDataParts[2]);
            Date referenceTime = new Date(Long.parseLong(logDataParts[3]));
            Date watchTime = new Date(Long.parseLong(logDataParts[4]));
            String position = logDataParts[5];
            int temperature = Integer.parseInt(logDataParts[6]);
            String comment;
            if ( logDataParts.length>7) {
                comment = logDataParts[7];
            } else {
                comment=null;
            }
            Log log = new Log(logId, watchId, period, referenceTime, watchTime, position, temperature, comment);
            logDao.insert(log);
        }
    }

    private class LogsDataSorter implements java.util.Comparator<String> {
        @Override
        public int compare(String o1, String o2) {
            if ( o1.equals(o2))
                return 0;

            String[] o1Parts = o1.split("\\|");
            String[] o2Parts = o2.split("\\|");

            int o1WatchId = Integer.parseInt(o1Parts[1]);
            int o2WatchId = Integer.parseInt(o2Parts[1]);

            if ( o1WatchId==o2WatchId) {
                // sort according to log id
                long o1LogId = Long.parseLong(o1Parts[0]);
                long o2LogId = Long.parseLong(o2Parts[0]);

                return Long.valueOf(o1LogId).compareTo(o2LogId);
            } else
                return Integer.valueOf(o1WatchId).compareTo(o2WatchId);
        }
    }
}
