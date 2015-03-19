package de.uhrenbastler.watchcheck.rss;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;

import org.apache.commons.lang3.ArrayUtils;

import java.util.List;
import java.util.Map;

import de.uhrenbastler.watchcheck.R;

/**
 * Created by clorenz on 19.03.15.
 */
public class UhrenbastlerRssFeedDisplayAdapter extends SimpleAdapter {

    private Context context;

    /**
     * Constructor
     *
     * @param context  The context where the View associated with this SimpleAdapter is running
     * @param data     A List of Maps. Each entry in the List corresponds to one row in the list. The
     *                 Maps contain the data for each row, and should include all the entries specified in
     *                 "from"
     * @param resource Resource identifier of a view layout that defines the views for this list
     *                 item. The layout file should include at least those named views defined in "to"
     * @param from     A list of column names that will be added to the Map associated with each
     *                 item.
     * @param to       The views that should display column in the "from" parameter. These should all be
     *                 TextViews. The first N views in this list are given the values of the first N columns
     */
    public UhrenbastlerRssFeedDisplayAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
        super(context, data, resource, from, to);
        this.context = context;
    }


    @Override
    public void setViewImage(ImageView v, String value) {
        Ion.with(v).resizeWidth(80).placeholder(R.drawable.icon).load(value);
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View v = super.getView(position, convertView, parent);

        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String,String> item = (Map) getItem(position);
                String url = item.get("source");

                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                context.startActivity(browserIntent);
            }
        });

        return v;
    }
}
