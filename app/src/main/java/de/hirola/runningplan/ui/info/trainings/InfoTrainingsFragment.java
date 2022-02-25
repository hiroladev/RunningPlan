package de.hirola.runningplan.ui.info.trainings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import de.hirola.runningplan.R;
import de.hirola.runningplan.util.AppLogManager;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * Fragment to view and edit saved trainings.
 *
 * @author Michael Schmidt (Hirola)
 * @since 1.1.1
 */
public class InfoTrainingsFragment extends Fragment {

    private final static String TAG = InfoTrainingsFragment.class.getSimpleName();

    private AppLogManager logManager; // app logger

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // app logger
        logManager = AppLogManager.getInstance(requireContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View aboutView = inflater.inflate(R.layout.fragment_info_trainings, container, false);
        return aboutView;
    }
}