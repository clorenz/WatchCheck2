package de.uhrenbastler.watchcheck.ui;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;

import de.uhrenbastler.watchcheck.R;

/**
 * Created by clorenz on 30.10.14.
 *
 * Based on http://antonioleiva.com/material-design-everywhere/
 */
public abstract class BaseActivity extends ActionBarActivity{

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResource());
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationIcon(R.drawable.ic_drawer);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
            toolbar.setSubtitle(watchName);
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
