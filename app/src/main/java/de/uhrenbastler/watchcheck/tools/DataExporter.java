package de.uhrenbastler.watchcheck.tools;

import android.app.Activity;
import android.os.Environment;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import de.uhrenbastler.watchcheck.WatchCheckApplication;
import watchcheck.db.Log;
import watchcheck.db.LogDao;
import watchcheck.db.Watch;
import watchcheck.db.WatchDao;

/**
 * Created by clorenz on 20.01.15.
 */
public class DataExporter {

    Activity act;

    public String export(Activity act) throws ExportException, FileNotFoundException {
        String date = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
        String filename = Environment.getExternalStorageDirectory()+"/watchcheck2-"+date+".data";

        LogDao logDao = ((WatchCheckApplication)act.getApplicationContext()).getDaoSession().getLogDao();
        WatchDao watchDao = ((WatchCheckApplication)act.getApplicationContext()).getDaoSession().getWatchDao();

        List<Log> allLogs = logDao.loadAll();
        List<Watch> allWatches = watchDao.loadAll();

        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename)));
        try {
            generateHeader(writer);
            exportWatches(writer, allWatches);
            exportLogs(writer, allLogs);
            writer.flush();
        } catch ( Exception e ) {
            Logger.error("WatchCheck", e);
        } finally {
            if ( writer!=null ) {
                try {
                    writer.close();
                } catch ( Exception e) {
                    Logger.error("WatchCheck", e);
                }
            }
        }

        return filename;
    }

    private void generateHeader(BufferedWriter writer) throws IOException {
        writer.append("WatchCheck2\n");
    }

    private void exportWatches(BufferedWriter writer, List<Watch> allWatches) throws IOException {
        for ( Watch watch : allWatches) {
            writer.append("WATCH: "+filterNewlines(watch.getId()+"|"+watch.getName()+"|"+watch.getSerial()+"|"+(watch.getCreatedAt()!=null?watch.getCreatedAt().getTime():"")+"|"+noNull(watch.getComment()))+"\n");
        }
    }

    private void exportLogs(BufferedWriter writer, List<Log> allLogs) throws IOException {
        for ( Log log : allLogs) {
            writer.append("LOG: "+filterNewlines(log.getId()+"|"+log.getWatchId()+"|"+log.getPeriod()+"|"+log.getReferenceTime().getTime()
                    +"|"+log.getWatchTime().getTime()+"|"+log.getPosition()+"|"+log.getTemperature()+"|"+noNull(log.getComment()))+"\n");
        }
    }

    private String noNull(String comment) {
        if ( comment==null || "null".equalsIgnoreCase(comment)) {
            return "";
        } else {
            return comment;
        }
    }

    private String filterNewlines(String s) {
        return s.replaceAll("\\n"," ").replaceAll("\\r"," ");
    }
}
