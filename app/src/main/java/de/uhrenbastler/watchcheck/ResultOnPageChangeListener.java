package de.uhrenbastler.watchcheck;

import android.support.v4.view.ViewPager;

import de.uhrenbastler.watchcheck.managers.AppStateManager;
import de.uhrenbastler.watchcheck.tools.Logger;

/**
 * Created by clorenz on 12.03.15.
 */
public class ResultOnPageChangeListener implements ViewPager.OnPageChangeListener {

    public DisplayResultPagerAdapter adapterViewPager;
    int position;
    boolean enabled = false;

    public ResultOnPageChangeListener(DisplayResultPagerAdapter adapterViewPager) {
        this.adapterViewPager = adapterViewPager;
    }


    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        this.position = position;
    }

    @Override
    public void onPageSelected(int position) {
        this.position = position;
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        if (enabled && state == ViewPager.SCROLL_STATE_IDLE) {
            AppStateManager.getInstance().setPage(position);
            Logger.debug("period page=" + AppStateManager.getInstance().getPage());
        }
    }

    void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
