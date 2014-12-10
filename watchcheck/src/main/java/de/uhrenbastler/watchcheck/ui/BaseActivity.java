package de.uhrenbastler.watchcheck.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;

import com.google.inject.Inject;
import com.google.inject.Key;

import java.util.HashMap;
import java.util.Map;

import de.uhrenbastler.watchcheck.R;
import de.uhrenbastler.watchcheck.ui.appcompat.RoboActionBarActivity;
import roboguice.RoboGuice;
import roboguice.activity.event.OnContentChangedEvent;
import roboguice.activity.event.OnCreateEvent;
import roboguice.activity.event.OnDestroyEvent;
import roboguice.activity.event.OnStopEvent;
import roboguice.event.EventManager;
import roboguice.inject.ContentViewListener;
import roboguice.inject.RoboInjector;

/**
 * Created by clorenz on 30.10.14.
 *
 * Based on http://antonioleiva.com/material-design-everywhere/
 */
public abstract class BaseActivity extends RoboActionBarActivity {

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        setContentView(getLayoutResource());
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationIcon(R.drawable.ic_drawer);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayUseLogoEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(false);
        }
    }

    protected abstract int getLayoutResource();

    protected void setActionBarIcon(int iconRes) {
        if ( toolbar != null ) {
            toolbar.setNavigationIcon(iconRes);
        }
    }

    protected void setActionBarWatchName(String watchName) {
        if ( toolbar != null) {
            SpannableString subtitle = new SpannableString(watchName);
            subtitle.setSpan(new RelativeSizeSpan(0.8f), 0, watchName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            toolbar.setSubtitle(subtitle);
        }
    }

    protected void setActionBarTitle(String title) {
        if ( toolbar != null) {
            toolbar.setTitle(title);
        }
    }

    protected String getActionBarTitle() {
        if ( toolbar != null ) {
            return toolbar.getTitle().toString();
        }
        return "";
    }

    protected Toolbar getToolbar() {
        return toolbar;
    }
}
