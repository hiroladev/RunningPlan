package de.hirola.runningplan.ui.start;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hirola.runningplan.R;
import de.hirola.runningplan.model.RunningPlanViewModel;
import de.hirola.sportslibrary.model.RunningPlan;
import de.hirola.sportslibrary.model.UUID;
import de.hirola.sportslibrary.model.User;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * Fragment for a overview of trainings, like a dashboard.
 *
 * @author Michael Schmidt (Hirola)
 * @since 0.1
 */
public class StartFragment extends Fragment {

    private final static String TAG = StartFragment.class.getSimpleName();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View startView = inflater.inflate(R.layout.fragment_start, container, false);
        // get the app user
        RunningPlanViewModel viewModel = new RunningPlanViewModel(requireActivity().getApplication(), null);
        User appUser = viewModel.getAppUser();
        UUID runningPlanUUID = appUser.getActiveRunningPlanUUID();
        if (runningPlanUUID != null) {
            // get the active running plan
            RunningPlan runningPlan = viewModel.getRunningPlanByUUID(runningPlanUUID);
            if (runningPlan != null) {
                RunningEntryRecyclerView listAdapter = new RunningEntryRecyclerView(requireContext(), runningPlan);
                // recycler view list adapter
                RecyclerView recyclerView = startView.findViewById(R.id.fgmt_start_trainings_recyclerview);
                recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
                recyclerView.setAdapter(listAdapter);
            }
        }
        return startView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}