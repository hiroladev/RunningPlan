package de.hirola.runningplan.ui.runningplans;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hirola.runningplan.R;
import de.hirola.runningplan.model.RunningPlanViewModel;
import de.hirola.sportslibrary.model.RunningPlan;
import de.hirola.sportslibrary.model.UUID;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * A fragment to view the units of a running plan.
 *
 * @author Michael Schmidt (Hirola)
 * @since 0.1
 */
public class RunningPlanEntriesFragment extends Fragment {

    private UUID runningPlanUUID;
    private RunningPlan runningPlan; // can be null

    // needed to instantiate fragment
    public RunningPlanEntriesFragment() {}

    public static RunningPlanEntriesFragment newInstance(UUID runningPlanUUID) {
        RunningPlanEntriesFragment fragment = new RunningPlanEntriesFragment();
        Bundle args = new Bundle();
        args.putString("runningPlanUUID", runningPlanUUID.getString());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // get the argument - the uuid for the selected running plan
        // we use a class from library for JVM and Android
        // UUID does not implement Parcelable
        if (getArguments() != null) {
            runningPlanUUID = new UUID(getArguments().getString("runningPlanUUID"));
            RunningPlanViewModel viewModel = new RunningPlanViewModel(requireActivity().getApplication(), null);
            runningPlan = viewModel.getRunningPlanByUUID(runningPlanUUID);
        } else {
            runningPlanUUID = null;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_running_entries, container, false);
        // visualize the list of running plan entries
        RunningEntryRecyclerView listAdapter = new RunningEntryRecyclerView(requireContext(), runningPlan);
        // recycler view list adapter
        RecyclerView recyclerView = view.findViewById(R.id.fgmt_running_entries_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(listAdapter);
        return view;
    }

    @Nullable
    public UUID getUUID() {
        return runningPlanUUID;
    }
}