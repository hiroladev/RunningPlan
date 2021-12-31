package de.hirola.runningplan.ui.runningplans;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

public class RunningPlanActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            RunningPlanListFragment listFragment = new RunningPlanListFragment();
            getSupportFragmentManager().beginTransaction().add(android.R.id.content, listFragment).commit();
        }
    }
}