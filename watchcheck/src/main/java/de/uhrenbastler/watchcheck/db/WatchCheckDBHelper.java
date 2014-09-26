package de.uhrenbastler.watchcheck.db;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

import de.uhrenbastler.watchcheck.R;
import de.uhrenbastler.watchcheck.data.Log;
import de.uhrenbastler.watchcheck.data.Watch;
import de.uhrenbastler.watchcheck.tools.Logger;

import static de.uhrenbastler.watchcheck.data.Watch.Watches;

/**
 * Created by clorenz on 14.02.14.
 */
public class WatchCheckDBHelper {

    public static Watch getWatchFromDatabase(long id, ContentResolver cr) {
        Uri selectedWatchUri = Uri.withAppendedPath(Watches.CONTENT_URI, ""+id);

        Logger.debug("Retrieving watch " + id + " from content provider with uri=" + selectedWatchUri);

        String[] columns = new String[] { Watches._ID, Watches.NAME, Watches.SERIAL, Watches.COMMENT };

        Cursor cur = null;

        try {
            cur = cr.query(selectedWatchUri, columns, Watches._ID+"="+id, null, null);

            if (cur.moveToFirst()) {
                String name = null;
                String serial = null;
                String comment = null;
                do {
                    id = cur.getLong(cur.getColumnIndex(Watches._ID));
                    name = cur.getString(cur.getColumnIndex(Watches.NAME));
                    serial = cur.getString(cur.getColumnIndex(Watches.SERIAL));
                    comment = cur.getString(cur.getColumnIndex(Watches.COMMENT));

                    return new Watch(id, name, serial, comment);

                } while (cur.moveToNext());
            }
        } finally {
            if ( cur !=null)
                cur.close();
        }

        return null;
    }



    // TODO: List<String> reicht hier nicht aus, da onNavigationItemSelected ja auch die ID der selektierten Watch benötigt!
    // TODO: And setting a fixed String here as addWatchName is also not very nice!
    public static List<String> getAllWatchesFromDatabaseAndPrependSelectedWatch(int selectedWatchId,
                                                                                String addWatchName,
                                                                                ContentResolver cr) {
        List<String> watches = new ArrayList<String>();

        Uri uriWatches = Watch.Watches.CONTENT_URI;
        String[] columns = new String[] { Watch.Watches._ID, Watch.Watches.NAME,
                Watch.Watches.SERIAL, Watch.Watches.COMMENT };

        Logger.debug("Retrieving all watches from database. Selected watch="+selectedWatchId);
        Cursor cur=null;
        try {
            cur = cr.query(uriWatches, columns, null, null, Watch.Watches.NAME+" collate nocase");

            if (cur!=null && cur.moveToFirst()) {
                Long id = null;
                String name = null;
                String serial = null;
                String comment = null;
                do {
                    id = cur.getLong(cur.getColumnIndex(Watch.Watches._ID));
                    name = cur.getString(cur.getColumnIndex(Watch.Watches.NAME));
                    serial = cur.getString(cur.getColumnIndex(Watch.Watches.SERIAL));
                    comment = cur.getString(cur.getColumnIndex(Watch.Watches.COMMENT));

                    Logger.debug("Found watch with id=" + id + ", name="
                            + name + ", serial=" + serial);

                    if ( id == selectedWatchId ) {
                        // Put selected watch into first position
                        watches.add(0, name);
                    } else {
                        watches.add(name);
                    }
                } while (cur.moveToNext());
            }

            watches.add(addWatchName);
        } finally {
            if ( cur !=null ) {
                cur.close();
            }
        }

        return watches;
    }



    /**
     * Verify, if there are results for the selected watch
     * @param selectedWatchId
     * @return
     */
    public static boolean resultsAvailableForCurrentWatch(int selectedWatchId, ContentResolver cr) {
        Logger.debug("Querying for results for watch "+selectedWatchId);
        Uri uriLogs = Log.Logs.CONTENT_URI;
        String[] columns = new String[] { Log.Logs._ID, Log.Logs.WATCH_ID };
        Cursor cur=null;
        try {
            cur = cr.query(uriLogs, columns, Log.Logs.WATCH_ID+"="+selectedWatchId, null, Log.Logs._ID);
            if (cur.moveToFirst()) {
                Logger.debug("Found results for watch "+selectedWatchId);
                return true;
            } else {
                Logger.debug("NO results for watch "+selectedWatchId);
                return false;
            }
        } finally {
            if ( cur !=null )
                cur.close();
        }
    }


    /**
     * Verify, that a watch with the given watchId exists
     * @param watchIdToValidate
     * @param cr ContentResolver
     * @return
     */
    public static int validateWatchId(int watchIdToValidate, ContentResolver cr) {
        Logger.debug("Validating watch "+watchIdToValidate);
        Uri uriWatches = Watches.CONTENT_URI;
        String[] columns = new String[] { Watches._ID };
        Cursor cur=null;
        try {
            cur = cr.query(uriWatches, columns, Watches._ID+"="+watchIdToValidate, null, Watches._ID);
            if (cur!=null && cur.moveToFirst()) {
                return watchIdToValidate;
            }
        } finally {
            if ( cur !=null )
                cur.close();
        }

        Logger.warn("No watch with ID "+watchIdToValidate+" found in database!");
        return -1;
    }



    public static Log getLastLogOfWatch(Activity activity, int watchId) {
        Logger.debug("Get latest logs of watch "+watchId);
        Uri uriLogs = Log.Logs.CONTENT_URI;
        String[] columns = new String[] { Log.Logs._ID, Log.Logs.WATCH_ID, Log.Logs.DEVIATION, Log.Logs.POSITION, Log.Logs.TEMPERATURE };

        Cursor cur=null;
        try {
            cur = activity.managedQuery(uriLogs, columns, Log.Logs.WATCH_ID+"="+watchId, null, Log.Logs._ID+" DESC");
            if (cur.moveToFirst()) {
                Log log = new Log();
                log.setDeviation(cur.getDouble(cur.getColumnIndex(Log.Logs.DEVIATION)));
                log.setPosition(cur.getString(cur.getColumnIndex(Log.Logs.POSITION)));
                log.setTemperature(cur.getInt(cur.getColumnIndex(Log.Logs.TEMPERATURE)));
                return log;
            } else {
                return null;
            }
        } finally {
            if ( cur !=null )
                cur.close();
        }
    }



    public static int getLatestWatchId(ContentResolver cr) {
        Uri uriWatches = Watches.CONTENT_URI;
        String[] columns = new String[] { Watches._ID };

        Cursor cur=null;
        try {
            cur = cr.query(uriWatches, columns, null, null, Log.Logs._ID+" DESC");
            if (cur.moveToFirst()) {
                return cur.getInt(cur.getColumnIndex(Watches._ID));
            } else {
                return -1;
            }
        } finally {
            if ( cur !=null )
                cur.close();
        }
    }
}
