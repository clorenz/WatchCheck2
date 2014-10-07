package de.uhrenbastler.watchcheck.ui;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TimePicker;

import de.uhrenbastler.watchcheck.R;

/**
 * Created by clorenz on 07.10.14.
 */
public class MeasureFragement extends Fragment {

    TimePicker watchtimePicker;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.measure, container, false);

        watchtimePicker = (TimePicker) view.findViewById(R.id.timePicker);
        watchtimePicker.setIs24HourView(true);
        watchtimePicker.setEnabled(false);                  // as long, as we don't have NTP or GPS data

        return view;
    }


}
