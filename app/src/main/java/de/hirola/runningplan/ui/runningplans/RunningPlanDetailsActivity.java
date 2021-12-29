package de.hirola.runningplan.ui.runningplans;

import android.content.res.Resources;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.TooltipCompat;
import androidx.lifecycle.ViewModelProvider;
import de.hirola.runningplan.R;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import de.hirola.runningplan.model.RunningPlanViewModel;
import de.hirola.sportslibrary.Global;
import de.hirola.sportslibrary.model.RunningPlan;
import de.hirola.sportslibrary.model.User;
import de.hirola.sportslibrary.ui.ModalOptionDialog;
import de.hirola.sportslibrary.ui.ModalOptionDialogListener;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
public class RunningPlanDetailsActivity extends AppCompatActivity implements View.OnClickListener {

    private RunningPlanViewModel viewModel;
    private User appUser;
    private RunningPlan runningPlan;
    private boolean isUsersRunningPlan;
    private ModalOptionDialog alertDialog;
    private TextView runningPlanNameTextView;
    private TextView runningPlanRemarksTextView;
    private Button showTrainingDetailsButton;
    private Button saveRunningPlanButton;
    private SwitchCompat activeRunningPlanSwitch;
    private Spinner startWeekSpinner;
    private ArrayAdapter<String> startWeekSpinnerArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // initialize the alert dialog
        alertDialog = ModalOptionDialog.getInstance(this);
        // app data
        viewModel = new ViewModelProvider(this).get(RunningPlanViewModel.class);
        appUser = viewModel.getAppUser().getValue();
        // get the running plan
        String runningPlanUUID = getIntent().getStringExtra("uuid");
        if (runningPlanUUID != null) {
            // set the selected running plan for details
            List<RunningPlan> runningPlans = viewModel.getRunningPlans().getValue();
            if (runningPlans != null) {
                for (RunningPlan plan : runningPlans) {
                    if (plan.getUUID().equalsIgnoreCase(runningPlanUUID)) {
                        runningPlan = plan;
                    }
                }
            }
        }
        // active running plan?
        if (appUser != null) {
            RunningPlan activeRunningPlan = appUser.getActiveRunningPlan();
            if (activeRunningPlan != null) {
                if (activeRunningPlan.getUUID().equalsIgnoreCase(runningPlan.getUUID())) {
                    isUsersRunningPlan = true;
                } else {
                    isUsersRunningPlan = false;
                }
            }
        }
        // initialize the ui
        setContentView(R.layout.activity_running_plan_details);
        setViewElements();
        // show the running plan values in view
        showRunningPlanInView();
    }

    @Override
    public void onClick(View v) {
        if (v == saveRunningPlanButton) {
            // save running plan and go back
            if (runningPlan != null) {
                // text
                if (!runningPlan.isTemplate()) {
                    String runningPlanName = runningPlanNameTextView.getText().toString();
                    String runningPlanRemarks = runningPlanRemarksTextView.getText().toString();
                    if (runningPlanName.length() == 0) {
                        alertDialog.showMessageDialog(ModalOptionDialog.DialogStyle.WARNING,
                                getString(R.string.hint), getString(R.string.name_must_be_not_null),
                                getString(R.string.ok));
                        return;
                    }
                    runningPlan.setName(runningPlanName);
                    runningPlan.setRemarks(runningPlanRemarks);
                }
                // active running plan
                if (isUsersRunningPlan && !activeRunningPlanSwitch.isChecked()) {
                    // user would not like the running plan as active
                    alertDialog.showOptionDialog(getString(R.string.question), getString(R.string.remove_active_runningplan),
                            getString(R.string.ok), getString(R.string.cancel),
                            new ModalOptionDialogListener() {
                        @Override
                        public void onButtonClicked(int button) {
                            if (button == ModalOptionDialog.Button.OK) {
                                // set the active running plan to null
                                appUser.setActiveRunningPlan(null);
                            } else {
                                activeRunningPlanSwitch.setChecked(true);
                            }
                        }
                    });
                }
            }
        }
    }

    private void setViewElements() {
        // initialize the text views
        runningPlanNameTextView = findViewById(R.id.activity_running_plan_details_edit_runningplan_name);
        runningPlanRemarksTextView = findViewById(R.id.activity_running_plan_details_edit_runningplan_remarks);
        // initialize the button
        showTrainingDetailsButton = findViewById(R.id.activity_runningplan_details_button_training_details);
        showTrainingDetailsButton.setOnClickListener(this);
        saveRunningPlanButton = findViewById(R.id.activity_running_plan_details_button_save);
        saveRunningPlanButton.setOnClickListener(this);
        // initialize the switch
        activeRunningPlanSwitch = findViewById(R.id.activity_running_plan_details_switch_active_runningplan);
        // initialize the spinner
        startWeekSpinner = findViewById(R.id.activity_running_plan_details_spinner_start_week);
        // creating adapter for spinner with an empty list
        startWeekSpinnerArrayAdapter = new ArrayAdapter<>(
                getBaseContext(),
                android.R.layout.simple_spinner_item,
                new ArrayList<>());
        // attaching data adapter to spinner with empty list
        startWeekSpinner.setAdapter(startWeekSpinnerArrayAdapter);
    }

    private void showRunningPlanInView() {
        if (runningPlan != null) {
            // name
            runningPlanNameTextView.setText(runningPlan.getName());
            // remarks
            if (runningPlan.isTemplate()) {
                // Text sollte in Ressourcen hinterlegt sein
                try {
                    // load strings from res dynamically
                    String remarksResourceString = runningPlan.getRemarks();
                    if (remarksResourceString.length() > 0) {
                        int remarksResourceStringId = getResources().getIdentifier(remarksResourceString,
                                "string", getPackageName());
                        String remarks = getString(remarksResourceStringId);
                        runningPlanRemarksTextView.setText(remarks);
                    }
                } catch (Resources.NotFoundException exception) {
                    runningPlanRemarksTextView.setText(R.string.no_remarks);
                    if (Global.DEBUG) {
                        // TODO: Logging
                    }
                }
            } else {
                // user's running plans
                runningPlanRemarksTextView.setText(runningPlan.getRemarks());
            }
            // templates must be changed
            if (runningPlan.isTemplate()) {
                runningPlanNameTextView.setEnabled(false);
                TooltipCompat.setTooltipText(runningPlanNameTextView, getText(R.string.template_must_be_changed));
                runningPlanRemarksTextView.setEnabled(false);
                TooltipCompat.setTooltipText(runningPlanRemarksTextView, getText(R.string.template_must_be_changed));
            }
            // active plan for user?
            activeRunningPlanSwitch.setChecked(isUsersRunningPlan);
            // if plan not active then creating a list of possible start weeks
            if (!runningPlan.isActive()) {
                startWeekSpinnerArrayAdapter.clear();
                startWeekSpinnerArrayAdapter.addAll(getStartDays());
            } else {
                // add the week of start date to the spinner and disabled the spinner
                // TODO: format the week
                startWeekSpinnerArrayAdapter.clear();
                startWeekSpinnerArrayAdapter.add(runningPlan.getStartDate().toString());
                startWeekSpinner.setEnabled(false);
            }
        }
    }

    // list of training days from selected running plan as string
    @NotNull
    private List<String> getStartDays() {
        List<String> trainingStartDaysStringList = new ArrayList<>();
        if (runningPlan != null ) {
            // the first possible training start
            LocalDate startDate = runningPlan.getStartDate();
            String trainingDateAsString = startDate
                    .getDayOfWeek()
                    .getDisplayName(TextStyle.FULL, Locale.getDefault());
            trainingDateAsString+= " (";
            trainingDateAsString+= startDate
                    .format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
            trainingDateAsString+= ")";
            trainingStartDaysStringList.add(trainingDateAsString);
            // add more possible start days to the list
            int loop = 0;
            while (loop < Global.Defaults.numberOfSelectableTrainingStartWeeks) {
                loop++;
                LocalDate trainingDate = startDate.plusWeeks(loop);
                trainingDateAsString = trainingDate
                        .getDayOfWeek()
                        .getDisplayName(TextStyle.FULL, Locale.getDefault());
                trainingDateAsString+= " (";
                trainingDateAsString+= trainingDate
                        .format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
                trainingDateAsString+= ")";
                trainingStartDaysStringList.add(trainingDateAsString);
            }
        }
        return trainingStartDaysStringList;
    }
}
