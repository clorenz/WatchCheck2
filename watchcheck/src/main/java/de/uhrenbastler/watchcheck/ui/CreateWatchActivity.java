package de.uhrenbastler.watchcheck.ui;

import android.content.Intent;
import android.view.MenuItem;

import de.uhrenbastler.watchcheck.R;

/**
 * Created by clorenz on 13.11.14.
 */
public class CreateWatchActivity extends BaseActivity {

    @Override protected int getLayoutResource() {
        return R.layout.activity_create_watch;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; go home
                Intent intent = new Intent(this, DisplayResultActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
