package de.hirola.runningplan.ui.runningplans;

import de.hirola.runningplan.R;

import android.content.res.Configuration;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * UI für die detaillierte Darstellung eines Laufplanes
 *
 *
 * @author Michael Schmidt (Hirola)
 * @since 1.1.1
 */
public class RunningPlanDetailsActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // in landscape orientation is no activity required
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            finish();
            return;
        }
        if (savedInstanceState == null) {
            RunningPlanDetailsFragment detailsFragment = new RunningPlanDetailsFragment();
            getSupportFragmentManager().beginTransaction().add(android.R.id.content, detailsFragment);
        }
    }
}
