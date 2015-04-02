package de.uhrenbastler.watchcheck;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.view.View;

import de.uhrenbastler.watchcheck.managers.ResultManager;
import de.uhrenbastler.watchcheck.managers.WatchManager;
import de.uhrenbastler.watchcheck.tools.Logger;
import watchcheck.db.Watch;

/**
 * Created by clorenz on 02.04.15.
 */
public class PlusButtonOnClickListener implements View.OnClickListener{

    private Activity activity;

    public PlusButtonOnClickListener(Activity activity, Watch selectedWatch) {
        this.activity = activity;
        WatchManager.setCurrentWatch(selectedWatch);
        Logger.debug("PlusButton: watch="+(selectedWatch!=null?selectedWatch.getId():"NULL"));
    }

    public void setSelectedWatch(Watch selectedWatch) {
        WatchManager.setCurrentWatch(selectedWatch);
        Logger.debug(this.hashCode()+": PlusButton: (2) watch="+(selectedWatch!=null?selectedWatch.getId():"NULL"));
    }

    @Override
    public void onClick(View v) {
        Watch selectedWatch = WatchManager.getCurrentWatch();
        Intent checkWatchIntent = new Intent(activity.getApplication(),CheckWatchActivity.class);
        checkWatchIntent.putExtra(CheckWatchActivity.EXTRA_WATCH, selectedWatch);
        if ( selectedWatch!=null) {
            checkWatchIntent.putExtra(CheckWatchActivity.EXTRA_LAST_LOG,
                    ResultManager.getLastLogForWatch(activity.getApplicationContext(), selectedWatch.getId()));
        }
        Logger.debug(this.hashCode() + ": Starting CheckWatchActivity for watch " + (selectedWatch != null ? selectedWatch.getId() : "NULL"));
        activity.startActivity(checkWatchIntent);
    }
}
