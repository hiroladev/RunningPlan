package de.hirola.runningplan.ui.info.tracks;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hirola.runningplan.R;
import de.hirola.runningplan.services.training.TrackManager;
import de.hirola.runningplan.util.ModalOptionDialog;
import de.hirola.sportsapplications.model.Track;

import java.util.List;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * Fragment to view and edit recorded tracks.
 *
 * @author Michael Schmidt (Hirola)
 * @since v0.1
 */
public class InfoTracksFragment extends Fragment implements View.OnClickListener {

    private Button clearAllTracksButton;
    private InfoTrackRecyclerView listAdapter;
    private TrackManager trackManager; // track manager for handling tracks
    private List<Track> tracks; // list if tacks

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View infoTracksView = inflater.inflate(R.layout.fragment_info_tracks, container, false);
        clearAllTracksButton = infoTracksView.findViewById(R.id.fgmt_info_tracks_clear_all_button);
        clearAllTracksButton.setOnClickListener(this);
        // handling recorded tracks
        trackManager = new TrackManager(requireContext());
        tracks = trackManager.getRecordedTracks();
        if (tracks.isEmpty()) {
            clearAllTracksButton.setEnabled(false);
        }
        listAdapter = new InfoTrackRecyclerView(requireContext(), tracks);
        listAdapter.setOnClickListener(this);
        RecyclerView recyclerView = infoTracksView.findViewById(R.id.fgmt_info_tracks_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(listAdapter);
        return infoTracksView;
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onClick(View v) {
        if (v instanceof Button) {
            ModalOptionDialog.showYesNoDialog(
                    requireContext(),
                    getString(R.string.question), getString(R.string.ask_before_clear_all_tracks),
                    getString(R.string.ok), getString(R.string.cancel),
                    button -> {
                        if (button == ModalOptionDialog.Button.OK) {
                            int deletedTracks = trackManager.clearAll();
                            tracks.clear();
                            tracks = trackManager.getRecordedTracks();
                            if (deletedTracks > 0) {
                                // refresh list
                                listAdapter.submitList(tracks);
                                listAdapter.notifyDataSetChanged();
                                if (tracks.isEmpty()) {
                                    clearAllTracksButton.setEnabled(false);
                                }
                            }
                        }
                    });
        }
    }
}