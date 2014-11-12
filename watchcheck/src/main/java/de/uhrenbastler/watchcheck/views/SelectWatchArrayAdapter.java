package de.uhrenbastler.watchcheck.views;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.uhrenbastler.watchcheck.R;
import de.uhrenbastler.watchcheck.models.Watch;
import de.uhrenbastler.watchcheck.tools.Logger;

/**
 * Created by clorenz on 13.02.14.
 */
public class SelectWatchArrayAdapter extends ArrayAdapter<Watch> {

    private final List<Watch> list;
    private final Activity activity;
    private final int currentWatchId;
    private final int textViewResourceIdSerial;

    public SelectWatchArrayAdapter(Activity activity, Context context, int resource, int textViewResourceIdName, int textViewResourceIdSerial, List<Watch> list, int currentWatchId) {
        super(context, resource, textViewResourceIdName, list);

        this.list = list;
        this.activity = activity;
        this.currentWatchId = currentWatchId;
        this.textViewResourceIdSerial = textViewResourceIdSerial;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;

        if ( v == null ) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.drawer_list_item, null);
        }

        Watch i = list.get(position);
        if ( i != null ) {
            TextView watchName = (TextView) v.findViewById(R.id.watchName);
            TextView watchSerial = (TextView) v.findViewById(R.id.watchSerial);

            if ( watchName!=null) {
                watchName.setText(i.getName());
            }
            if ( watchSerial!=null) {
                watchSerial.setText(i.getSerial());
            }
        }

        return v;
    }

    protected static class ViewHolder {
        protected TextView watchSerial;
        protected TextView watchName;
    }
}
