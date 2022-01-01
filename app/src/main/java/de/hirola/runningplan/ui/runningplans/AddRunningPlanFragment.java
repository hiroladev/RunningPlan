package de.hirola.runningplan.ui.runningplans;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.lifecycle.ViewModelProvider;
import de.hirola.runningplan.R;
import de.hirola.runningplan.model.MutableListLiveData;
import de.hirola.runningplan.model.RunningPlanViewModel;
import de.hirola.sportslibrary.model.RunningPlan;

import java.util.List;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * A fragment to add or import new running plans.
 *
 * @author Michael Schmidt (Hirola)
 * @since 1.1.1
 */
public class AddRunningPlanFragment extends Fragment {

    // cached list of running plans
    private List<RunningPlan> runningPlans;

    public AddRunningPlanFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RunningPlanViewModel viewModel = new ViewModelProvider(requireActivity()).get(RunningPlanViewModel.class);
        MutableListLiveData<RunningPlan> mutableRunningPlans = viewModel.getMutableRunningPlans();
        runningPlans = mutableRunningPlans.getValue();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_running_plan, container, false);
    }
}