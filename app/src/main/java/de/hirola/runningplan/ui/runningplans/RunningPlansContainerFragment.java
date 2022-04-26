package de.hirola.runningplan.ui.runningplans;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import de.hirola.runningplan.R;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * The main fragment for the running plans.
 *
 * @author Michael Schmidt (Hirola)
 * @since v0.1
 */
public class RunningPlansContainerFragment extends Fragment {

    public RunningPlansContainerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            RunningPlansFragment listFragment = new RunningPlansFragment();
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