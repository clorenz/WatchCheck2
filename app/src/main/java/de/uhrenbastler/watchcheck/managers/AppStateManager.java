package de.uhrenbastler.watchcheck.managers;

import watchcheck.db.Log;

/**
 * Created by clorenz on 12.03.15.
 */
public class AppStateManager {

    private static AppStateManager _instance = new AppStateManager();
    private long watchId;
    private int page=-1;

    private AppStateManager() {};

    public static AppStateManager getInstance() {
        return _instance;
    }

    public long getWatchId() {
        return watchId;
    }

    public void setWatchId(long watchId) {
        this.watchId = watchId;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }
}
