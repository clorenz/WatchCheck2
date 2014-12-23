package de.uhrenbastler.watchcheck.views;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Timestamp;
import java.util.List;

import de.uhrenbastler.watchcheck.DisplayResultFragment;
import de.uhrenbastler.watchcheck.MainActivity;
import de.uhrenbastler.watchcheck.NavigationDrawerFragment;
import de.uhrenbastler.watchcheck.R;
import de.uhrenbastler.watchcheck.managers.WatchManager;
import de.uhrenbastler.watchcheck.models.Watch;
import de.uhrenbastler.watchcheck.tools.Logger;

/**
 * Created by clorenz on 22.12.14.
 */
public class EditWatchFragment extends Fragment {

    boolean isNewWatch=false;
    Watch currentWatch=null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_edit_watch, container, false);
        Bundle bundle = getArguments();
        final Watch watch = (Watch) (bundle.getSerializable("watch")!=null?bundle.getSerializable("watch"):new Watch());
        currentWatch = (Watch) bundle.getSerializable("currentWatch");

        if (watch == null) {
            // Indicator, that we will add a new watch
            TextView headline = (TextView) view.findViewById(R.id.textViewAddWatch);
            headline.setText(getString(R.string.addWatchHeadline));
        }

        if ( watch.getName() != null || watch.getSerial()!=null || watch.getComment()!=null || watch.getCreatedAt()!=null ) {
            Logger.debug("Editing watch " + watch);

            ((TextView)view.findViewById(R.id.editTextModel)).setText(watch.getName());
            ((TextView)view.findViewById(R.id.editTextSerial)).setText(watch.getSerial());
            ((TextView)view.findViewById(R.id.editTextRemarks)).setText(watch.getComment());
            ((Button)view.findViewById(R.id.buttonDelete)).setVisibility(View.VISIBLE);
        } else {
            Logger.debug("Creating new watch");
            isNewWatch=true;
            watch.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        }

        Button btnCancel = (Button) view.findViewById(R.id.buttonCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(watch.getId());
                return;
            }
        });

        Button btnOk = (Button) view.findViewById(R.id.buttonOk);
        btnOk.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                watch.setName(((TextView)view.findViewById(R.id.editTextModel)).getEditableText().toString());
                watch.setSerial(((TextView)view.findViewById(R.id.editTextSerial)).getEditableText().toString());
                watch.setComment(((TextView)view.findViewById(R.id.editTextRemarks)).getEditableText().toString());
                watch.save();

                Logger.debug("Updated watch "+watch);

                finish(watch.getId());

                updateNavigationDrawerAndHeadline(isNewWatch ? null : watch, currentWatch);

                Toast.makeText(getActivity().getApplicationContext(), String.format(getString(R.string.createdWatch),
                        watch.getName()), Toast.LENGTH_SHORT).show();

                return;
            }
        });

        Button btnDelete = (Button) view.findViewById(R.id.buttonDelete);
        btnDelete.setOnClickListener(new View.OnClickListener() {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            watch.delete();
                            updateNavigationDrawerAndHeadline(isNewWatch ? null : watch, currentWatch);
                            Toast.makeText(getActivity().getApplicationContext(), String.format(getString(R.string.deletedWatch),
                                    watch.getName()), Toast.LENGTH_SHORT).show();
                            finish(currentWatch.getId());
                            break;
                        case DialogInterface.BUTTON_NEGATIVE:
                            break;
                    }
                }
            };

            @Override
            public void onClick(View v) {
                AlertDialog.Builder deleteWatchAlertDialog = new AlertDialog.Builder(v.getContext());
                deleteWatchAlertDialog.setMessage(String.format(getString(R.string.deleteWatchQuestion),
                        watch.getName()+(watch.getSerial()!=null?"/"+watch.getSerial():"")))
                        .setPositiveButton(getString(R.string.yes), dialogClickListener)
                        .setNegativeButton(getString(R.string.no), dialogClickListener)
                        .show();
            }
        });

        return view;
    }

    /**
     * Handler for the "back" icon in the action bar / toolbar
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; finish activity
                finish(currentWatch.getId());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public void finish(Long watchId) {
        getView().setVisibility(View.GONE);

        Fragment displayResultFragment = DisplayResultFragment.newInstance(watchId, 0);

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.container, displayResultFragment);
        transaction.addToBackStack(null);
        transaction.commit();

        // TODO: Wenn es sich um eine neue Uhr handelt, sollte diese als currentWatch gewählt werden!
    }



    public void updateNavigationDrawerAndHeadline(Watch editWatch, Watch currentWatch) {
        Logger.debug("editWatch="+editWatch+", currentWatch="+currentWatch);


        // Re-Fetch all watches and re-populate the navigation drawer
        List<Watch> watches = WatchManager.retrieveAllWatchesWithCurrentFirstAndAddWatch(currentWatch);


        NavigationDrawerFragment mNavigationDrawerFragment = (NavigationDrawerFragment)
                getActivity().getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mNavigationDrawerFragment.setWatches(watches);
        if ( currentWatch!=null && editWatch.getId()==currentWatch.getId() ) {
            mNavigationDrawerFragment.setSelectedWatch(currentWatch);
            updateTitle(currentWatch.getName());
        }
    }


    private void updateTitle(String watchName) {
        // Find calling activity and update title there
        SpannableString subtitle = new SpannableString(watchName);
        subtitle.setSpan(new RelativeSizeSpan(0.8f), 0, watchName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        ((MainActivity)getActivity()).getSupportActionBar().setSubtitle(subtitle);
    }

}
