package de.hirola.runningplan.ui.runningplans;

import android.content.Intent;
import android.widget.ListView;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import de.hirola.runningplan.R;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.ListFragment;
import de.hirola.runningplan.model.RunningPlanViewModel;
import de.hirola.sportslibrary.model.RunningPlan;

import java.util.List;
import java.util.Objects;

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

    private List<RunningPlan> runningPlans; // list of all running plans
    private boolean tabletMode; // list and details fragments in on view

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // load the running plans
        RunningPlanViewModel viewModel = new ViewModelProvider(this).get(RunningPlanViewModel.class);
        runningPlans = (viewModel.getRunningPlans()).getValue();
        // visualize th list of running plans
        RunningPlanArrayAdapter listAdapter = new RunningPlanArrayAdapter(getContext(),runningPlans);
        // refresh the ui when the observed data changes
        // Update the cached copy of the words in the adapter.
        viewModel.getRunningPlans().observe(this, listAdapter::submitList);
        // determine the mode
        View detailsFragment = requireActivity().findViewById(R.id.fragmentContainerViewRunningPlanDetails);
        if (detailsFragment != null && detailsFragment.getVisibility() == View.VISIBLE) {
            tabletMode = true;
            // select the first element in view to showing details
            if (runningPlans.size() > 0) {
                showDetailsForRunningPlanAtIndex(0);
            }
        } else {
            tabletMode = false;
        }
        setListAdapter(listAdapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_running_plans, container, false);
    }

    /**
     * This method will be called when an item in the list is selected.
     * Subclasses should override. Subclasses can call
     * getListView().getItemAtPosition(position) if they need to access the
     * data associated with the selected item.
     *
     * @param l        The ListView where the click happened
     * @param v        The view that was clicked within the ListView
     * @param position The position of the view in the list
     * @param id       The row id of the item that was clicked
     */
    @Override
    public void onListItemClick(@NonNull ListView l, @NonNull View v, int position, long id) {
        showDetailsForRunningPlanAtIndex(position);
    }

    private void showDetailsForRunningPlanAtIndex(int index) {
        if(tabletMode){
            // the RunningPlanActivity has two fragments
            // select the element at index in view to showing details
            getListView().setItemChecked(index, true);
            // get a reference from RunningPlanDetailsFragment
            RunningPlanDetailsFragment detailsFragment =
                    (RunningPlanDetailsFragment) getParentFragmentManager().findFragmentById(R.id.fragmentContainerViewRunningPlanDetails);
            if (detailsFragment == null) {
                // no details fragment still present
                // create new details fragment
                detailsFragment = new RunningPlanDetailsFragment();
                FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fragmentContainerViewRunningPlanDetails,detailsFragment);
                // make a transition
                fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                fragmentTransaction.commit();
            }
        } else {
            // starts the RunningPlanDetailsActivity
            Intent intent = new Intent();
            intent.setClass(getActivity(), RunningPlanDetailsActivity.class);
            startActivity(intent);
        }
    }
}