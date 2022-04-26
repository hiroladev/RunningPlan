package de.hirola.runningplan.ui.info;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import de.hirola.runningplan.R;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * The main fragment for infos.
 *
 * @author Michael Schmidt (Hirola)
 * @since v0.1
 */
public class InfoFragment extends Fragment {

    // Required empty public constructor
    public InfoFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            InfoContentFragment infoContentFragment = new InfoContentFragment();
            getChildFragmentManager().beginTransaction().add(R.id.fragment_info_container, infoContentFragment).commit();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_info, container, false);
    }
}