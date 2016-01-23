package de.uhrenbastler.watchcheck.views;

import android.content.Context;
import android.content.res.Resources;
import android.text.Layout;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TableRow;
import android.widget.TextView;

import com.gc.materialdesign.widgets.Dialog;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import de.uhrenbastler.watchcheck.Deviations;
import de.uhrenbastler.watchcheck.DisplayResultFragment;
import de.uhrenbastler.watchcheck.R;
import de.uhrenbastler.watchcheck.tools.Logger;
import de.uhrenbastler.watchcheck.utils.LocalizedTimeUtil;
import watchcheck.db.Log;

/**
 * Created by clorenz on 05.01.15.
 */
public class ResultListAdapter extends ArrayAdapter<Log> {
    private final Context context;
    private final List<Log> logs;
    private static final String[] POSITIONARR = { "",
                                                  Deviations.DU.name(),
                                                  Deviations.DD.name(),
                                                  "12U",
                                                  "3U",
                                                  "6U",
                                                  "9U",
                                                  Deviations.WINDER.name(),
                                                  Deviations.OTHER.name() };
    private static final int[] TEMPARR = { -273, 4, 20, 36 };

    public ResultListAdapter(Context context, List<Log> logs) {
        super(context, R.layout.result_row, logs);
        this.context = context;
        this.logs = logs;

    }


    @Override
    public View getView(int position, View convertView, final ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.result_row, parent, false);
        final Log log = logs.get(position);

        TextView tvTimestampReference = (TextView) rowView.findViewById(R.id.result_timestamp_reference);
        String showDate = LocalizedTimeUtil.getDate(context, log.getReferenceTime());
        String showTime = LocalizedTimeUtil.getTime(context, log.getReferenceTime());
        tvTimestampReference.setText(showDate + "\n" + showTime);

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

        final Resources res = rowView.getResources();
        String[] positions = res.getStringArray(R.array.result_positions);
        String[] temperatures = res.getStringArray(R.array.temperatures);

        int posIndex = ArrayUtils.indexOf(POSITIONARR, log.getPosition());
        if ( posIndex==-1) {
            posIndex=0;
        }
        TextView tvPosition = (TextView) rowView.findViewById(R.id.result_position);
        tvPosition.setText(positions[posIndex]);

        TextView tvTemperature = (TextView) rowView.findViewById(R.id.result_temperature);
        if ( posIndex>=1 && posIndex<=6) {
            // Watch was not worn, not on the winder and not unspecified
            int tempIndex = ArrayUtils.indexOf(TEMPARR, log.getTemperature());
            if (tempIndex > -1) {
                tvTemperature.setText(temperatures[tempIndex]);
            }
        } else {
            tvPosition.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT,0.32f));
            tvTemperature.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT,0.0f));
        }

        ImageView infoIcon = (ImageView) rowView.findViewById(R.id.result_info);
        if (StringUtils.isEmpty(log.getComment()) || "null".equals(log.getComment())) {
            infoIcon.setVisibility(View.INVISIBLE);
        } else {
            infoIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Dialog commentDialog = new Dialog(parent.getContext(),
                            res.getString(R.string.comment),
                            log.getComment());
                    commentDialog.setButtonCancel(null);
                    commentDialog.show();
                    commentDialog.getButtonAccept().setText(res.getString(R.string.ok));
                }
            });
        }

        return rowView;
    }
}
