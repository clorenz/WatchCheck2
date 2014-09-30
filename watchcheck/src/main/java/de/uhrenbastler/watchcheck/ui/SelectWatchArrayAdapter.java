package de.uhrenbastler.watchcheck.ui;

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
import de.uhrenbastler.watchcheck.data.Watch;

/**
 * Created by clorenz on 13.02.14.
 */
public class SelectWatchArrayAdapter extends ArrayAdapter {

    private final List<Watch> list;
    private final Activity activity;
    private final int currentWatchId;

    public SelectWatchArrayAdapter(Activity activity, Context context, int resource, int textViewResourceId, List<Watch> list, int currentWatchId) {
        super(context, resource, textViewResourceId, list);

        this.list = list;
        this.activity = activity;
        this.currentWatchId = currentWatchId;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        ViewHolder view;

        if(rowView == null)
        {
            // Get a new instance of the row layout view
            LayoutInflater inflater = activity.getLayoutInflater();
            rowView = inflater.inflate(R.layout.watchcheck_spinner_dropdown_item, null);

            // Hold the view objects in an object, that way the don't need to be "re-  finded"
            view = new ViewHolder();
            view.watchCheckHeadline= (TextView) rowView.findViewById(R.id.watchCheckHeadline);
            view.watchName= (TextView) rowView.findViewById(R.id.watchName);

            rowView.setTag(view);
        } else {
            view = (ViewHolder) rowView.getTag();
        }

        // This defines, how the selected(!) item is displayed on the spinner selection field in the actionbar
        String item = list.get(position).getName();
        view.watchCheckHeadline.setText(R.string.app_name);
        view.watchCheckHeadline.setVisibility(View.VISIBLE);
        view.watchCheckHeadline.setTextColor(Color.WHITE);
        view.watchName.setText(item);
        view.watchName.setPadding(0,5,0,0);
        view.watchName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
        view.watchName.setTextColor(Color.WHITE);

        // If the name of the active watch equals "Add watch", then we don't want to display it!
        if ( activity.getResources().getString(R.string.addWatch).equals(item)) {
            view.watchName.setVisibility(View.GONE);
        }

        return rowView;
    }

    protected static class ViewHolder {
        protected TextView watchCheckHeadline;
        protected TextView watchName;
    }
}
