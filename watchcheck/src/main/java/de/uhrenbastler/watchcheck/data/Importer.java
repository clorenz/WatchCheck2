package de.uhrenbastler.watchcheck.data;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Environment;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import de.uhrenbastler.watchcheck.tools.Logger;

import static de.uhrenbastler.watchcheck.data.Log.Logs;

/**
 * Created by clorenz on 14.02.14.
 */
public class Importer {

    Activity act;

    public void doImport(Activity act) throws FileNotFoundException {
        this.act = act;

        String filename = Environment.getExternalStorageDirectory()+"/watchcheck.data";
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(filename)));

        List<String> watchesData=new ArrayList<String>();
        List<String> logsData=new ArrayList<String>();
        boolean readLogs=false;

        try {
            String line;
            while ( (line = reader.readLine()) != null) {
                if ( line.startsWith("----------------"))
                    readLogs=true;
                else {
                    if ( readLogs )
                        logsData.add(line);
                    else
                        watchesData.add(line);
                }
            }
        } catch ( Exception e) {
            Logger.error("Could not import data: "+e.getMessage(),e);
            return;
        } finally {
            if ( reader!=null)
                try {
                    reader.close();
                } catch (Exception e) {
                    Logger.warn("Could not close file: " + e.getMessage());
                }
        }

        Logger.info("CR=" + act.getContentResolver().getClass().getName());

        act.managedQuery(Uri.parse("content://" + WatchCheckLogContentProvider.AUTHORITY + "/close"), null, null, null, null);

        if ( act.deleteDatabase(WatchCheckLogContentProvider.WATCHCHECK_DB_NAME))
            Logger.info("Deleted database " + WatchCheckLogContentProvider.WATCHCHECK_DB_NAME);
        else
            Logger.error("Could not delete " + WatchCheckLogContentProvider.WATCHCHECK_DB_NAME);

        SQLiteDatabase sqlDb = act.openOrCreateDatabase(WatchCheckLogContentProvider.WATCHCHECK_DB_NAME,
                Context.MODE_WORLD_WRITEABLE, null);

        sqlDb.setVersion(WatchCheckLogContentProvider.DB_VERSION);

        importWatches(watchesData, sqlDb);
        Logger.info("Imported watches");
        importLogs(logsData, sqlDb);
        Logger.info("Imported logs. DONE with import!");
        sqlDb.close();
    }


    private void importWatches(List<String> watchesData, SQLiteDatabase sqlDb) {

        sqlDb.execSQL(Watch.Watches.CREATE_TABLE_STATEMENT);

        for ( String watchData: watchesData) {
            String[] watchDataParts = watchData.split("\\|");

            ContentValues values = new ContentValues();
            values.put(Watch.Watches._ID, watchDataParts[0]);
            values.put(Watch.Watches.NAME, watchDataParts[1]);
            values.put(Watch.Watches.SERIAL, watchDataParts[2]);
            if ( !"null".equals(watchDataParts[3]))
                values.put(Watch.Watches.DATE_CREATE, watchDataParts[3]);
            if ( watchDataParts.length == 5)
                values.put(Watch.Watches.COMMENT, watchDataParts[4]);

            Logger.debug(values.toString());
            sqlDb.insert(Watch.Watches.TABLE_NAME, null, values);
        }

    }


    private void importLogs(List<String> logsData, SQLiteDatabase sqlDb) {

        sqlDb.execSQL(Logs.CREATE_TABLE_STATEMENT);

        for ( String logData: logsData) {
            String[] logDataParts = logData.split("\\|");

            ContentValues values = new ContentValues();
            values.put(Logs._ID, logDataParts[0]);
            values.put(Logs.WATCH_ID, logDataParts[1]);
            values.put(Logs.MODUS, logDataParts[2]);
            values.put(Logs.LOCAL_TIMESTAMP, logDataParts[3]);
            values.put(Logs.NTP_DIFF, logDataParts[4]);
            values.put(Logs.DEVIATION, logDataParts[5]);
            values.put(Logs.FLAG_RESET, logDataParts[6]);
            if ( !"null".equals(logDataParts[7]))
                values.put(Logs.POSITION, logDataParts[7]);
            values.put(Logs.TEMPERATURE, logDataParts[8]);
            if ( logDataParts.length == 10)
                values.put(Logs.COMMENT, logDataParts[9]);

            Logger.debug(values.toString());
            sqlDb.insert(Logs.TABLE_NAME, null, values);
        }

    }
}
