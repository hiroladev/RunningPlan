package de.hirola.runningplan.ui.runningplans;

import android.content.res.Resources;
import android.os.Bundle;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.TooltipCompat;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.FragmentTransaction;
import de.hirola.runningplan.R;
import de.hirola.runningplan.RunningPlanApplication;
import de.hirola.runningplan.model.RunningPlanViewModel;
import de.hirola.sportslibrary.Global;
import de.hirola.sportslibrary.SportsLibrary;
import de.hirola.sportslibrary.model.RunningPlan;
import de.hirola.sportslibrary.model.UUID;
import de.hirola.sportslibrary.model.User;
import de.hirola.runningplan.util.ModalOptionDialog;
import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * A fragment to edit running plan details.
 *
 * @author Michael Schmidt (Hirola)
 * @since 0.1
 */
public class RunningPlanDetailsFragment extends Fragment
        implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private SportsLibrary sportsLibrary;
    private RunningPlanViewModel viewModel;
    private User appUser;
    private UUID runningPlanUUID;
    private RunningPlan runningPlan;
    private boolean isUsersRunningPlan;
    private TextView runningPlanNameTextView;
    private TextView runningPlanRemarksTextView;
    private Button showTrainingDetailsButton;
    private Button saveRunningPlanButton;
    private CheckBox resetRunningPlanCheckBox;
    private SwitchCompat activeRunningPlanSwitch;
    private Spinner startWeekSpinner;
    private StartDateArrayAdapter startWeekSpinnerArrayAdapter;

    // needed to instantiate fragment
    public RunningPlanDetailsFragment() {
        // Required empty public constructor
    }

    public static RunningPlanDetailsFragment newInstance(UUID runningPlanUUID) {
        RunningPlanDetailsFragment fragment = new RunningPlanDetailsFragment();
        Bundle args = new Bundle();
        // we use a class from library for JVM and Android
        // UUID does not implement Parcelable
        args.putString("runningPlanUUID", runningPlanUUID.getString());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // get the argument - the uuid for the selected running plan
        // we use a class from library for JVM and Android
        // UUID does not implement Parcelable
        if (getArguments() != null) {
            runningPlanUUID = new UUID(getArguments().getString("runningPlanUUID"));
        } else {
            runningPlanUUID = null;
        }
        sportsLibrary = ((RunningPlanApplication) requireActivity().getApplication()).getSportsLibrary();

        // app data
        viewModel = new RunningPlanViewModel(requireActivity().getApplication(), null);
        appUser = viewModel.getAppUser();
        if (runningPlanUUID != null) {
            runningPlan = viewModel.getRunningPlanByUUID(runningPlanUUID);
        }
        // active running plan?
        if (runningPlan != null) {
            UUID activeRunningPlanUUID = appUser.getActiveRunningPlanUUID();
            if (activeRunningPlanUUID != null) {
                isUsersRunningPlan = runningPlan.getUUID().equals(activeRunningPlanUUID);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_running_plan_details, container, false);
        // initialize the ui
        setViewElements(view);
        // show the running plan values in view
        showRunningPlanInView();
        return view;
    }

    @Override
    public void onClick(View view) {
        if (view == saveRunningPlanButton) {
            // add running plan and go back
            if (runningPlan != null) {
                // text
                if (!runningPlan.isTemplate()) {
                    String runningPlanName = runningPlanNameTextView.getText().toString();
                    String runningPlanRemarks = runningPlanRemarksTextView.getText().toString();
                    if (runningPlanName.length() == 0) {
                        ModalOptionDialog.showMessageDialog(ModalOptionDialog.DialogStyle.WARNING,
                                requireContext(),
                                getString(R.string.hint), getString(R.string.name_must_be_not_null),
                                getString(R.string.ok));
                        return;
                    }
                    runningPlan.setName(runningPlanName);
                    runningPlan.setRemarks(runningPlanRemarks);
                }
                // start date of running plan
                LocalDate startDate = (LocalDate) startWeekSpinner.getSelectedItem();
                if (startDate != null) {
                    runningPlan.setStartDate(startDate);
                }

                // active running plan
                if (isUsersRunningPlan && !activeRunningPlanSwitch.isChecked()) {
                    // user would not like the running plan as active
                    ModalOptionDialog.showYesNoDialog(
                            requireContext(),
                            getString(R.string.question), getString(R.string.stop_active_running_plan),
                            getString(R.string.ok), getString(R.string.cancel),
                            button -> {
                                if (button == ModalOptionDialog.Button.OK) {
                                    // set the active running plan to null
                                    appUser.setActiveRunningPlanUUID(null);
                                } else {
                                    activeRunningPlanSwitch.setChecked(true);
                                }
                            });
                } else {
                    appUser.setActiveRunningPlanUUID(runningPlan.getUUID());
                }
                // update the user and the running plan
                if (!viewModel.updateObject(appUser) || !viewModel.updateObject(runningPlan)) {
                    ModalOptionDialog.showMessageDialog(
                            ModalOptionDialog.DialogStyle.CRITICAL,
                            requireContext(),
                            getString(R.string.error), getString(R.string.save_data_error),
                            getString(R.string.ok));
                    // disable switch again
                    activeRunningPlanSwitch.setChecked(false);
                }
            }
        }
        if (view == resetRunningPlanCheckBox) {
            if (resetRunningPlanCheckBox.isChecked()) {
                ModalOptionDialog.showYesNoDialog(
                        requireContext(),
                        getString(R.string.question), getString(R.string.reset_running_plan),
                        getString(R.string.ok), getString(R.string.cancel),
                        button -> {
                            if (button == ModalOptionDialog.Button.OK) {
                                // reset the running plan
                                runningPlan.setUncompleted();
                                if (!viewModel.updateObject(runningPlan)) {
                                    ModalOptionDialog.showMessageDialog(
                                            ModalOptionDialog.DialogStyle.CRITICAL,
                                            requireContext(),
                                            getString(R.string.error), getString(R.string.save_data_error),
                                            getString(R.string.ok));
                                }
                                // disable switch again
                                resetRunningPlanCheckBox.setChecked(false);
                            } else {
                                resetRunningPlanCheckBox.setChecked(false);
                            }
                        });
            }
        }
        if (view == showTrainingDetailsButton) {
            // show the running units
            showRunningPLanUnitDetails();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        if (isUsersRunningPlan && !isChecked) {
            // the plan should no longer be trained.
            ModalOptionDialog.showYesNoDialog(
                    requireContext(),
                    getString(R.string.question), getString(R.string.stop_active_running_plan),
                    getString(R.string.ok), getString(R.string.cancel),
                    button -> {
                        if (button == ModalOptionDialog.Button.OK) {
                            // remove the running plan uuid from user
                            appUser.setActiveRunningPlanUUID(null);
                            if (!viewModel.updateObject(appUser)) {
                                ModalOptionDialog.showMessageDialog(
                                        ModalOptionDialog.DialogStyle.CRITICAL,
                                        requireContext(),
                                        getString(R.string.error), getString(R.string.save_data_error),
                                        getString(R.string.ok));
                                // activate switch again
                                activeRunningPlanSwitch.setChecked(true);
                            } else {
                                activeRunningPlanSwitch.setChecked(false);
                            }
                        } else {
                            activeRunningPlanSwitch.setChecked(true);
                        }
                    });
        }
    }

    @Nullable
    public UUID getUUID() {
        return runningPlanUUID;
    }

    private void setViewElements(View view) {
        // initialize the text views
        runningPlanNameTextView = view.findViewById(R.id.fgmt_add_running_plan_name_edittext);
        runningPlanRemarksTextView = view.findViewById(R.id.fgmt_running_plan_details_remarks_edittext);
        // initialize the button
        showTrainingDetailsButton = view.findViewById(R.id.fgmt_running_plan_details_show_details_button);
        showTrainingDetailsButton.setOnClickListener(this);
        saveRunningPlanButton = view.findViewById(R.id.fgmt_running_plan_details_save_button);
        saveRunningPlanButton.setOnClickListener(this);
        // initialize check box
        resetRunningPlanCheckBox = view.findViewById(R.id.fgmt_running_plan_details_reset_check_box);
        resetRunningPlanCheckBox.setOnClickListener(this);
        // initialize the switch
        activeRunningPlanSwitch = view.findViewById(R.id.fgmt_running_plan_details_active_plan_switch);
        activeRunningPlanSwitch.setOnCheckedChangeListener(this);
        // initialize the spinner
        startWeekSpinner = view.findViewById(R.id.fgmt_running_plan_details_start_week_spinner);
        // creating adapter for spinner with an empty list
        startWeekSpinnerArrayAdapter = new StartDateArrayAdapter(
                requireContext(),
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
                                "string", requireActivity().getPackageName());
                        String remarks = getString(remarksResourceStringId);
                        runningPlanRemarksTextView.setText(remarks);
                    }
                } catch (Resources.NotFoundException exception) {
                    runningPlanRemarksTextView.setText(R.string.no_remarks);
                    if (sportsLibrary.isDebugMode()) {
                        Logger.debug(null, exception);
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
                startWeekSpinnerArrayAdapter.addAll(getStartDates());
                // select the start date
                int position = startWeekSpinnerArrayAdapter.getPosition(runningPlan.getStartDate());
                if (position > -1) {
                    startWeekSpinner.setSelection(position);
                }
            } else {
                // add the week of start date to the spinner and disabled the spinner
                // TODO: format the week
                startWeekSpinnerArrayAdapter.clear();
                startWeekSpinnerArrayAdapter.add(runningPlan.getStartDate());
                startWeekSpinner.setEnabled(false);
            }
        }
    }

    private void showRunningPLanUnitDetails() {
        RunningPlanEntriesFragment entriesFragment = null;
        List<Fragment> fragments = getParentFragmentManager().getFragments();
        for (Fragment fragment : fragments) {
            if (fragment instanceof RunningPlanEntriesFragment) {
                entriesFragment = (RunningPlanEntriesFragment) fragment;
                break;
            }
        }
        // if fragment or uuid null or a fragment for another running plan (uuid)
        // then create a new fragment
        if (entriesFragment == null || entriesFragment.getUUID() == null) {
            entriesFragment = RunningPlanEntriesFragment.newInstance(runningPlanUUID);
        } else if (!entriesFragment.getUUID().equals(runningPlanUUID)) {
            entriesFragment = RunningPlanEntriesFragment.newInstance(runningPlanUUID);
        }
        // hides the RunningPlansFragment
        FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
        fragmentTransaction.hide(this);
        // starts the RunningPlanDetailsFragment
        fragmentTransaction.replace(R.id.fragment_running_plan_container, entriesFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    // list of training days from selected running plan as string
    @NotNull
    private List<LocalDate> getStartDates() {
        List<LocalDate> startDates = new ArrayList<>();
        if (runningPlan != null ) {
            if (!runningPlan.isActive()) {
                LocalDate startDate = runningPlan.getStartDate();
                LocalDate today = LocalDate.now();
                LocalDate maxForwardTrainingStartDate = today
                        .plusWeeks(Global.Defaults.numberOfSelectableTrainingStartWeeks);
                Period period = Period.between(today, startDate);
                int weeks = period.getDays() / 7;
                if (startDate.isAfter(maxForwardTrainingStartDate) ||
                        weeks < Global.Defaults.numberOfSelectableTrainingStartWeeks) {
                    // reset the first start day of running plan
                    // set the correct monday
                    DayOfWeek dayOfWeek = today.getDayOfWeek();
                    if (dayOfWeek != DayOfWeek.MONDAY) {
                        // ab Dienstag ist das Startdatum der nächste Montag
                        long daysToAdd = 8 - dayOfWeek.getValue();
                        startDate = today.plusDays(daysToAdd);
                    }
                }
                startDates.add(startDate);
                // add more possible start days to the list
                int loop = 0;
                while (loop < Global.Defaults.numberOfSelectableTrainingStartWeeks) {
                    loop++;
                    LocalDate trainingDate = startDate.plusWeeks(loop);
                    startDates.add(trainingDate);
                }
            }
        }
        return startDates;
    }
}