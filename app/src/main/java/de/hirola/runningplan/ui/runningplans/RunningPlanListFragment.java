package de.hirola.runningplan.ui.runningplans;

import android.os.Bundle;
import android.widget.ListView;
import androidx.annotation.NonNull;
import androidx.fragment.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import de.hirola.runningplan.R;
import de.hirola.sportslibrary.model.RunningPlan;

import java.time.LocalDate;
import java.util.ArrayList;

/**
 * A fragment representing a list of Items.
 */
public class RunningPlanListFragment extends ListFragment {

    ArrayList<RunningPlan> runningPlans;

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

        // sample data
        RunningPlan[] list = new RunningPlan[2];
        RunningPlan r1 = new RunningPlan();
        r1.setName("Laufplan 1");
        r1.setRemarks("Erster Laufplan");
        r1.setStartDate(LocalDate.now());
        RunningPlan r2 = new RunningPlan();
        r2.setName("Laufplan 2");
        r2.setRemarks("Zweiter Laufplan");

        list[0] = r1;
        list[1] = r2;

        // Adapter initialisieren
        RunningPlanArrayAdapter runningPlanArrayAdapter = new RunningPlanArrayAdapter(getContext(), list);

        setListAdapter(runningPlanArrayAdapter);

        return inflater.inflate(R.layout.fragment_running_plans, container, false);

    }

    @Override
    public void onListItemClick(@NonNull ListView l, @NonNull View v, int position, long id) {
        System.out.println("Click");
    }
}