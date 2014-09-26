package de.uhrenbastler.watchcheck.data;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import de.uhrenbastler.watchcheck.tools.Logger;

import static de.uhrenbastler.watchcheck.data.Log.Logs;

/**
 * Created by clorenz on 15.02.14.
 */
public class Exporter {

    Activity act;

    public String export(Activity act) throws ExportException, FileNotFoundException {
        this.act = act;
        String filename = Environment.getExternalStorageDirectory()+"/watchcheck.data";
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(filename)));

        try {
            exportWatches(writer);
            writer.write("------------------------------\n");
            exportLogs(writer);
        } catch ( Exception e ) {
            Logger.error("Cannot export "+filename+": ",e);
        } finally {
            if ( writer!=null )
                try {
                    writer.close();
                } catch (IOException e) {
                    Logger.error("Error on closing export writer at " + filename + ": ", e);
                }
        }

        return filename;
    }

    public void exportWatches(BufferedWriter writer) throws ExportException, FileNotFoundException {
        Uri uriWatches = Watch.Watches.CONTENT_URI;
        String[] columns = new String[] { Watch.Watches._ID,
                Watch.Watches.NAME,
                Watch.Watches.SERIAL,
                Watch.Watches.DATE_CREATE,
                Watch.Watches.COMMENT, };

        try {
            Cursor cur=null;
            try {
                cur = act.managedQuery(uriWatches, columns, null, null, Watch.Watches._ID+" asc");
                if (cur.moveToFirst()) {
                    Long id = null;
                    String name = null;
                    String serial = null;
                    String dateCreate = null;
                    String comment = null;
                    do {
                        id = cur.getLong(cur.getColumnIndex(Watch.Watches._ID));
                        name = cur.getString(cur.getColumnIndex(Watch.Watches.NAME));
                        serial = cur.getString(cur.getColumnIndex(Watch.Watches.SERIAL));
                        dateCreate = cur.getString(cur.getColumnIndex(Watch.Watches.DATE_CREATE));
                        comment = cur.getString(cur.getColumnIndex(Watch.Watches.COMMENT));

                        writer.write(id+"|"+name+"|"+serial+"|"+dateCreate+"|"+comment+"\n");
                        writer.flush();
                    } while (cur.moveToNext());
                }
            } finally {
                if ( cur !=null )
                    cur.close();
            }
        } catch ( IOException e) {
            Logger.error("Cannot retrieve watches for export: ", e);
        }
    }

    public void exportLogs(BufferedWriter writer) throws ExportException, FileNotFoundException {
        Uri uriLogs = Logs.CONTENT_URI;
        String[] columns = new String[] { Logs._ID,
                Logs.WATCH_ID,
                Logs.MODUS,
                Logs.LOCAL_TIMESTAMP,
                Logs.NTP_DIFF,
                Logs.DEVIATION,
                Logs.FLAG_RESET,
                Logs.POSITION,
                Logs.TEMPERATURE,
                Logs.COMMENT};
        try {
            Cursor cur=null;
            try {
                cur = act.managedQuery(uriLogs, columns, null, null, Logs._ID+" asc");
                if (cur.moveToFirst()) {
                    Long id = null;
                    Long watchId = null;
                    String modus = null;
                    String localTimestamp = null;
                    Double ntpDiff = null;
                    Double deviation = null;
                    String flagReset = null;
                    String position = null;
                    Integer temperature = null;
                    String comment = null;
                    do {
                        id = cur.getLong(cur.getColumnIndex(Logs._ID));
                        watchId = cur.getLong(cur.getColumnIndex(Logs.WATCH_ID));
                        modus = cur.getString(cur.getColumnIndex(Logs.MODUS));
                        localTimestamp = cur.getString(cur.getColumnIndex(Logs.LOCAL_TIMESTAMP));
                        ntpDiff = cur.getDouble(cur.getColumnIndex(Logs.NTP_DIFF));
                        deviation = cur.getDouble(cur.getColumnIndex(Logs.DEVIATION));
                        flagReset = cur.getString(cur.getColumnIndex(Logs.FLAG_RESET));
                        position = cur.getString(cur.getColumnIndex(Logs.POSITION));
                        temperature = cur.getInt(cur.getColumnIndex(Logs.TEMPERATURE));
                        comment = cur.getString(cur.getColumnIndex(Logs.COMMENT));

                        writer.write(id+"|"+watchId+"|"+modus+"|"+localTimestamp+
                                "|"+ntpDiff+"|"+deviation+"|"+flagReset+
                                "|"+position+"|"+temperature+"|"+comment+"\n");
                        writer.flush();
                    } while (cur.moveToNext());
                }
            } finally {
                if ( cur !=null )
                    cur.close();
            }
        } catch ( IOException e) {
            Logger.error("Cannot export logs: ", e);
        }
    }
}
