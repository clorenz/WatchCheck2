package de.uhrenbastler.watchcheck.managers;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.uhrenbastler.watchcheck.WatchCheckApplication;
import de.uhrenbastler.watchcheck.tools.Logger;
import watchcheck.db.Watch;
import watchcheck.db.WatchDao;

/**
 * Created by clorenz on 12.11.14.
 */
public class WatchManager {

    private static final String PREFERENCE_CURRENT_WATCH = "currentWatch";
    private static Watch currentWatch;

    /**
     * Looks up the ID of the current watch from shared preferences and returns the associated watch
     * <br>
     * If not found, returns null
     * @return current watch or null
     */
    public static Watch retrieveCurrentWatch(Activity activity) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);

        int currentWatchId = preferences.getInt(PREFERENCE_CURRENT_WATCH, -1);

        Logger.debug("Retrieving current watch with ID=" + currentWatchId);

        if ( currentWatchId>=0) {
            WatchDao watchDao = ((WatchCheckApplication)activity.getApplicationContext()).getDaoSession().getWatchDao();
            return watchDao.load(Long.valueOf(currentWatchId));
        }

        return null;
    }

    public static List<Watch> retrieveAllWatches(Context context) {
        WatchDao watchDao = ((WatchCheckApplication)context.getApplicationContext()).getDaoSession().getWatchDao();
        return watchDao.loadAll();
    }


    public static List<Watch> retrieveAllWatchesWithCurrentFirstAndAddWatch(Context context, Watch firstItem) {
        List<Watch> origList = retrieveAllWatches(context);
        List<Watch> ret = new ArrayList<Watch>();

        if ( firstItem!=null ) {
            ret.add(firstItem);
        }

        for ( Watch watch : origList ) {
            if ( firstItem==null || watch.getId() != firstItem.getId()) {
                ret.add(watch);
            }
        }

        ret.add(new Watch(null, "add watch", null, null, null));

        return ret;
    }

    public static Watch getCurrentWatch() {
        return currentWatch;
    }

    public static void setCurrentWatch(Watch currentWatch) {
        WatchManager.currentWatch = currentWatch;
    }
}
