package de.hirola.runningplan.ui.training;

import android.os.*;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import de.hirola.runningplan.R;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * Fragment for the training.
 *
 * @author Michael Schmidt (Hirola)
 * @since 0.1
 */
public class TrainingContainerFragment extends Fragment {

    public TrainingContainerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            TrainingFragment trainingContentFragment1 = new TrainingFragment();
            getChildFragmentManager().beginTransaction().add(R.id.fragment_training_container, trainingContentFragment1).commit();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_training, container, false);
    }
}