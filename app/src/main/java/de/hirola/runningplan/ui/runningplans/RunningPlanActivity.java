package de.hirola.runningplan.ui.runningplans;

import de.hirola.runningplan.R;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * UI für die Darstellung von Laufplänen
 *
 *
 * @author Michael Schmidt (Hirola)
 * @since 1.1.1
 */
public class RunningPlanActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_running_plan);
    }
}
