package de.uhrenbastler.watchcheck;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.text.style.TextAppearanceSpan;

import java.text.SimpleDateFormat;
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
    boolean displaySummary=false;

    public DisplayResultPagerAdapter(Context context, FragmentManager fragmentManager, long watchId, boolean displaySummary) {
        super(fragmentManager);

        this.context = context;
        this.watchId = watchId;
        this.displaySummary = displaySummary;
        periods = ResultManager.getPeriodsForWatch(context, watchId);

        Logger.debug("Found fragments: "+fragmentManager.getFragments().size());
        while ( fragmentManager.getFragments().size()>0) {
            fragmentManager.getFragments().remove(0);
            Logger.debug("Pop");
        }
    }

    // Returns total number of pages
        @Override
        public int getCount() {
            return periods.size();
        }

        // Returns the fragment to display for that page
        @Override
        public Fragment getItem(int position) {
            return DisplayResultFragment.newInstance(watchId, periods.get(position), displaySummary);
        }

        // Returns the page title for the top indicator
        @Override
        public CharSequence getPageTitle(int position) {
            String start = ResultManager.getPeriodStartDate(context, watchId, periods.get(position));
            String end = ResultManager.getPeriodEndDate(context, watchId, periods.get(position));

            String titleString;
            if ( start.equals(end)) {
                titleString = start;
            } else {
                String[] startParts = start.split("\\.");
                String[] endParts = end.split("\\.");

                if (endParts[2].equals(startParts[2])) {
                    // Same year. Omit this from start!
                    if (endParts[1].equals(startParts[1])) {
                        // Even the same month! Omit this from start, too
                        start = startParts[0] + ".";
                    } else {
                        start = startParts[0] + "." + startParts[1] + ".";
                    }
                }

                titleString = start + " - " + end;
            }
            SpannableStringBuilder sb = new SpannableStringBuilder(titleString);
            TextAppearanceSpan headerSpan = new TextAppearanceSpan(context, R.style.TextAppearance_AppCompat_Small_Inverse);
            sb.setSpan(headerSpan,0,titleString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            return sb;
        }


    public int getPeriodOfPosition(int position) {
        return periods.get(position);
    }
}
