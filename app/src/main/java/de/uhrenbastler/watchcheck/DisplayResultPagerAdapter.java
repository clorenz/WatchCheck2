package de.uhrenbastler.watchcheck;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

import de.uhrenbastler.watchcheck.managers.ResultManager;
import de.uhrenbastler.watchcheck.tools.Logger;

/**
 * Created by clorenz on 17.12.14.
 */
public class DisplayResultPagerAdapter extends FragmentPagerAdapter {

    private int numItems;
    private long watchId;
    private List<Integer>periods;
    private Context context;

    public DisplayResultPagerAdapter(Context context, FragmentManager fragmentManager, long watchId) {
        super(fragmentManager);
        this.context = context;
        this.watchId = watchId;
        periods = ResultManager.getPeriodsForWatch(context, watchId);

    }

    // Returns total number of pages
        @Override
        public int getCount() {
            return periods.size();
        }

        // Returns the fragment to display for that page
        @Override
        public Fragment getItem(int position) {
            return DisplayResultFragment.newInstance(watchId, periods.get(position));
        }

        // Returns the page title for the top indicator
        @Override
        public CharSequence getPageTitle(int position) {
            Logger.debug("Getting page title for " + position);
            String start = ResultManager.getPeriodStartDate(context, watchId, periods.get(position));
            String end = ResultManager.getPeriodEndDate(context, watchId, periods.get(position));

            return start+" - "+end;
        }
}
