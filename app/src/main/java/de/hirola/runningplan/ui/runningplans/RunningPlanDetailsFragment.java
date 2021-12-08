package de.hirola.runningplan.ui.runningplans;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.lifecycle.ViewModelProvider;
import de.hirola.runningplan.R;
import de.hirola.runningplan.model.RunningPlanViewModel;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RunningPlanDetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RunningPlanDetailsFragment extends Fragment {

    // app data
    private RunningPlanViewModel viewModel;

    public RunningPlanDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(RunningPlanViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_running_plan_details, container, false);
    }
}