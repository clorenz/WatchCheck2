package de.uhrenbastler.watchcheck.managers;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.uhrenbastler.watchcheck.models.Watch;
import de.uhrenbastler.watchcheck.tools.Logger;

/**
 * Created by clorenz on 12.11.14.
 */
public class WatchManager {

    private static final String PREFERENCE_CURRENT_WATCH = "currentWatch";

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

        if ( currentWatchId>=0)
            return Watch.findById(Watch.class, Long.valueOf(currentWatchId));

        return null;
    }

    public static List<Watch> retrieveAllWatches() {
        List<Watch> watches = new ArrayList<Watch>();

        Iterator<Watch> watchesIterator = Watch.findAll(Watch.class);
        while ( watchesIterator.hasNext()) {
            watches.add(watchesIterator.next());
        }

        return watches;
    }
}
