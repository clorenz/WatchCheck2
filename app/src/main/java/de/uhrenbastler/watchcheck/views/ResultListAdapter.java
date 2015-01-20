package de.uhrenbastler.watchcheck.views;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import org.apache.commons.lang3.ArrayUtils;

import java.text.SimpleDateFormat;
import java.util.List;

import de.uhrenbastler.watchcheck.DisplayResultFragment;
import de.uhrenbastler.watchcheck.R;
import de.uhrenbastler.watchcheck.tools.Logger;
import watchcheck.db.Log;

/**
 * Created by clorenz on 05.01.15.
 */
public class ResultListAdapter extends ArrayAdapter<Log> {
    private final Context context;
    private final List<Log> logs;
    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy\nHH:mm:ss");
    private static final String[] POSITIONARR = { "","DU","DD","12U","3U","6U","9U" };
    private static final int[] TEMPARR = { -273, 4, 20, 36 };

    public ResultListAdapter(Context context, List<Log> logs) {
        super(context, R.layout.result_row, logs);
        this.context = context;
        this.logs = logs;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.result_row, parent, false);
        Log log = logs.get(position);

        TextView tvTimestampReference = (TextView) rowView.findViewById(R.id.result_timestamp_reference);
        tvTimestampReference.setText(sdf.format(log.getReferenceTime()));

        double offset = ((double)(log.getWatchTime().getTime() - log.getReferenceTime().getTime())) / 1000d;
        TextView tvOffset = (TextView) rowView.findViewById(R.id.result_offset);
        tvOffset.setText(String.format("%+.1f s", offset));

        if ( position > 0 ) {
            Log logBefore = logs.get(position-1);
            long diffReference = log.getReferenceTime().getTime() - logBefore.getReferenceTime().getTime();
            long diffWatch = log.getWatchTime().getTime() - logBefore.getWatchTime().getTime();

            // Calculate the correcting factor for displaying the result of one day
            double factor = (86400d * 1000) / ((double)diffReference);

            double deviation = ((((double)diffWatch) * factor)/1000) - 86400d;

            TextView tvDeviation = (TextView) rowView.findViewById(R.id.result_deviation_per_day);
            tvDeviation.setText(String.format("%+.1f s/d", deviation));
        }

        Resources res = rowView.getResources();
        String[] positions = res.getStringArray(R.array.result_positions);
        String[] temperatures = res.getStringArray(R.array.temperatures);

        int posIndex = ArrayUtils.indexOf(POSITIONARR, log.getPosition());
        if ( posIndex==-1) {
            posIndex=0;
        }
        TextView tvPosition = (TextView) rowView.findViewById(R.id.result_position);
        tvPosition.setText(positions[posIndex]);

        if ( posIndex>0) {
            // Watch was not worn
            int tempIndex = ArrayUtils.indexOf(TEMPARR, log.getTemperature());
            if (tempIndex > -1) {
                TextView tvTemperature = (TextView) rowView.findViewById(R.id.result_temperature);
                tvTemperature.setText(temperatures[tempIndex]);
            }
        }

        return rowView;
    }
}
