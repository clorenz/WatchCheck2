package de.uhrenbastler.watchcheck;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.format.DateUtils;
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
            long startMillis = ResultManager.getPeriodStartMillis(context, watchId, periods.get(position));
            long endMillis = ResultManager.getPeriodEndMillis(context, watchId, periods.get(position));

            String titleString = DateUtils.formatDateRange(context, startMillis, endMillis, DateUtils.FORMAT_SHOW_DATE|DateUtils.FORMAT_ABBREV_MONTH);

            SpannableStringBuilder sb = new SpannableStringBuilder(titleString);
            TextAppearanceSpan headerSpan = new TextAppearanceSpan(context, R.style.TextAppearance_AppCompat_Small_Inverse);
            sb.setSpan(headerSpan,0,titleString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            return sb;
        }


    public int getPeriodOfPosition(int position) {
        return periods.get(position);
    }
}
