package de.uhrenbastler.watchcheck;

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

    public DisplayResultPagerAdapter(FragmentManager fragmentManager, long watchId) {
        super(fragmentManager);
        this.watchId = watchId;
        periods = ResultManager.getPeriodsForWatch(watchId);

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
            String start = ResultManager.getPeriodStartDate(watchId, periods.get(position));
            String end = ResultManager.getPeriodEndDate(watchId, periods.get(position));

            return start+" - "+end;
        }
}
