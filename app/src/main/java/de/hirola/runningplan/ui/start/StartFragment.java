package de.hirola.runningplan.ui.start;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hirola.runningplan.R;
import de.hirola.runningplan.model.RunningPlanViewModel;
import de.hirola.sportslibrary.model.RunningPlan;
import de.hirola.sportslibrary.model.Training;
import de.hirola.sportslibrary.model.UUID;
import de.hirola.sportslibrary.model.User;

import java.util.List;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * Fragment for an overview of trainings, like a dashboard.
 *
 * @author Michael Schmidt (Hirola)
 * @since 0.1
 */
public class StartFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView trainingsOverviewTextView;
    private RunningPlanViewModel viewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new RunningPlanViewModel(requireActivity().getApplication(), null);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View startView = inflater.inflate(R.layout.fragment_start, container, false);
        recyclerView = startView.findViewById(R.id.fgmt_start_active_running_plan_recyclerview);
        trainingsOverviewTextView = startView.findViewById(R.id.fgmt_start_training_training_overview_text);
        // show active running plan overview
        showRunningPlanOverview();
        // show training data
        showTrainingOverview();
        return startView;
    }

    @Override
    public void onResume() {
        super.onResume();
        // show active running plan overview
        showRunningPlanOverview();
        // show training data
        showTrainingOverview();
    }

    private void showRunningPlanOverview() {
        User appUser = viewModel.getAppUser();
        UUID runningPlanUUID = appUser.getActiveRunningPlanUUID();
        if (runningPlanUUID != null) {
            // get the active running plan
            RunningPlan runningPlan = viewModel.getRunningPlanByUUID(runningPlanUUID);
            if (runningPlan != null) {
                RunningEntryRecyclerView listAdapter = new RunningEntryRecyclerView(requireContext(), runningPlan);
                // recycler view list adapter
                recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
                recyclerView.setAdapter(listAdapter);
            }
        }
    }

    @SuppressLint("DefaultLocale")
    private void showTrainingOverview() {
        // show all trainings summary
        List<Training> trainings = viewModel.getTrainings();
        if (trainings.size() > 0) {
            // summaries some values
            long duration = 0L;
            double distance = 0.0;
            for (Training training : trainings) {
                duration += training.getDuration();
                distance += training.getDistance();
            }
            StringBuilder trainingOverviewString = new StringBuilder(getString(R.string.total_durations));
            trainingOverviewString.append(": ");
            // duration in minutes or hour
            if (duration > 0 &&  duration < 60) {
                trainingOverviewString.append(String.format("%s%s", duration, " min"));
            } else if (duration > 59) {
                trainingOverviewString.append(String.format("%s%s%s%s",
                        duration / 60, "h : ", duration % 60, " min"));
            }
            trainingOverviewString.append("\n");
            trainingOverviewString.append(getString(R.string.total_distance));
            trainingOverviewString.append(": ");
            // distance in m or km
            if (distance > 0 && distance < 999) {
                trainingOverviewString.append(String.format("%,.2f%s", distance, " m"));
            } else if (distance > 999) {
                trainingOverviewString.append(String.format("%,.2f%s", distance / 1000, " km"));
            }
            trainingsOverviewTextView.setText(trainingOverviewString);
        } else {
            trainingsOverviewTextView.setText(R.string.no_trainings);
        }
    }
}