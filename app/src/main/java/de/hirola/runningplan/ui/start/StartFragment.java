package de.hirola.runningplan.ui.start;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hirola.runningplan.R;
import de.hirola.runningplan.model.RunningPlanViewModel;
import de.hirola.sportslibrary.model.RunningPlan;
import de.hirola.sportslibrary.model.User;

public class StartFragment extends Fragment {

    private final static String TAG = StartFragment.class.getSimpleName();

    // recycler view list adapter
    private RecyclerView recyclerView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View startView = inflater.inflate(R.layout.fragment_start, container, false);
        // get the app user
        RunningPlanViewModel viewModel = new ViewModelProvider(requireActivity()).get(RunningPlanViewModel.class);
        User appUser = viewModel.getAppUser();
        RunningPlan runningPlan = appUser.getActiveRunningPlan();
        if (runningPlan != null) {
            RunningEntryRecyclerView listAdapter = new RunningEntryRecyclerView(requireContext(), runningPlan);
            recyclerView = startView.findViewById(R.id.fgmt_start_trainings_recyclerview);
            recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
            recyclerView.setAdapter(listAdapter);
        }
        return startView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}