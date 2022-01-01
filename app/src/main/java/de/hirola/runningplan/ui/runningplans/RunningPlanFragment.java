package de.hirola.runningplan.ui.runningplans;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import de.hirola.runningplan.R;


public class RunningPlanFragment extends Fragment {

    public RunningPlanFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            RunningPlanListFragment listFragment = new RunningPlanListFragment();
            getChildFragmentManager().beginTransaction().add(R.id.fragment_running_plan_container, listFragment).commit();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_running_plan, container, false);
    }
}