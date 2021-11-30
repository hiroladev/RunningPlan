package de.hirola.runningplan.ui.runningplans;

import android.os.Bundle;
import android.widget.ListView;
import androidx.annotation.NonNull;
import androidx.fragment.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.lifecycle.ViewModelProvider;
import de.hirola.runningplan.R;
import de.hirola.runningplan.model.RunningPlanViewModel;
import de.hirola.sportslibrary.model.RunningPlan;

import java.time.LocalDate;
import java.util.ArrayList;

/**
 * A fragment representing a list of Items.
 */
public class RunningPlanListFragment extends ListFragment {

    // visualize a running plan in list view
    RunningPlanArrayAdapter runningPlanArrayAdapter;
    // holds the data (model) for the app
    private RunningPlanViewModel runningPlanViewModel;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public RunningPlanListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // initialize the view model
        runningPlanViewModel = new ViewModelProvider(this).get(RunningPlanViewModel.class);
        // initialize the custom adapter
        runningPlanArrayAdapter = new RunningPlanArrayAdapter(getContext(), runningPlanViewModel.getRunningPlans().getValue());
        // register for changes to refresh the view
        runningPlanViewModel.getRunningPlans().observe(this, runningPlans -> {
            // Update the cached copy of the running plans in the adapter.
            runningPlanArrayAdapter.submitList(runningPlans);
        });
        // bind the adapter to the fragment
        setListAdapter(runningPlanArrayAdapter);

        return inflater.inflate(R.layout.fragment_running_plans, container, false);

    }

    @Override
    public void onListItemClick(@NonNull ListView l, @NonNull View v, int position, long id) {
        RunningPlan runningPlan = runningPlanViewModel.getRunningPlans().getValue().get(position);
        if (runningPlan != null) {
            System.out.println(runningPlan.getName());
        }
    }
}