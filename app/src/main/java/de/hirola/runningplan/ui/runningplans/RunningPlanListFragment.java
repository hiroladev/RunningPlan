package de.hirola.runningplan.ui.runningplans;

import android.widget.Button;
import android.widget.ListView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import de.hirola.runningplan.R;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.ListFragment;
import de.hirola.runningplan.model.MutableListLiveData;
import de.hirola.runningplan.model.RunningPlanViewModel;
import de.hirola.sportslibrary.model.RunningPlan;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * List-Fragment für die Darstellung aller vorhandener Laufpläne (Überblick).
 *

 * @author Michael Schmidt (Hirola)
 * @since 1.1.1
 */
public class RunningPlanListFragment extends ListFragment {

    // last selected running plan (list index)
    private int lastSelectedIndex;
    // cached list of running plans
    private List<RunningPlan> runningPlans;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // get the last selected list index (running plan)
        if (savedInstanceState != null) {
            lastSelectedIndex = savedInstanceState.getInt("lastSelectedIndex");
        }
        // load the running plans
        // data
        RunningPlanViewModel viewModel = new ViewModelProvider(requireActivity()).get(RunningPlanViewModel.class);
        MutableListLiveData<RunningPlan> mutableRunningPlans = viewModel.getMutableRunningPlans();
        mutableRunningPlans.observe(requireActivity(), this::onListChanged);
        runningPlans = mutableRunningPlans.getValue();
        // visualize th list of running plans
        RunningPlanArrayAdapter listAdapter = new RunningPlanArrayAdapter(requireContext(), runningPlans);
        setListAdapter(listAdapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_running_plan_list, container, false);
        FloatingActionButton addRunningPlanButton = view.findViewById(R.id.button_add_runningplan);
        addRunningPlanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddRunningPlanFragment addRunningPlanFragment = null;
                List<Fragment> fragments = getParentFragmentManager().getFragments();
                for (Fragment fragment : fragments) {
                    if (fragment instanceof AddRunningPlanFragment) {
                        addRunningPlanFragment = (AddRunningPlanFragment) fragment;
                        break;
                    }
                }
                if (addRunningPlanFragment == null) {
                    // create a new fragment
                    addRunningPlanFragment = new AddRunningPlanFragment();
                }
                // starts the RunningPlanAddFragment
                showFragment(addRunningPlanFragment);
            }
        });
        return view;
    }

    @Override
    public void onSaveInstanceState(@NonNull @NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // save the selected running plan (list index)
        outState.putInt("lastSelectedIndex", lastSelectedIndex);
    }

    @Override
    public void onListItemClick(@NonNull ListView l, @NonNull View v, int position, long id) {
        showDetailsForRunningPlanAtIndex(position);
    }

    private void showDetailsForRunningPlanAtIndex(int index) {
        if (runningPlans.size() > index) {
            lastSelectedIndex = index;
            RunningPlan runningPlan = runningPlans.get(index);
            String uuid = runningPlan.getUUID();
            RunningPlanDetailsFragment detailsFragment = null;
            List<Fragment> fragments = getParentFragmentManager().getFragments();
            for (Fragment fragment : fragments) {
                if (fragment instanceof RunningPlanDetailsFragment) {
                    detailsFragment = (RunningPlanDetailsFragment) fragment;
                    break;
                }
            }
            if (detailsFragment == null || detailsFragment.getUUID().equalsIgnoreCase(uuid)) {
                // create a new fragment
                detailsFragment = RunningPlanDetailsFragment.newInstance(uuid);
            }
            // starts the RunningPlanDetailsFragment
            showFragment(detailsFragment);
        }
    }

    private void showFragment(Fragment fragment) {
        // hides the RunningPlanListFragment
        FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
        fragmentTransaction.hide(this);
        // starts the RunningPlanDetailsFragment
        fragmentTransaction.replace(R.id.fragment_running_plan_container, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    // refresh the cached list of running plans
    private void onListChanged(List<RunningPlan> changedList) {
        runningPlans = changedList;
    }
}