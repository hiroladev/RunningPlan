package de.hirola.runningplan.ui.runningplans;

import android.content.res.Resources;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.TooltipCompat;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.lifecycle.ViewModelProvider;
import de.hirola.runningplan.R;
import de.hirola.runningplan.model.MutableListLiveData;
import de.hirola.runningplan.model.RunningPlanViewModel;
import de.hirola.runningplan.util.AppLogManager;
import de.hirola.sportslibrary.Global;
import de.hirola.sportslibrary.SportsLibraryException;
import de.hirola.sportslibrary.model.RunningPlan;
import de.hirola.sportslibrary.model.User;
import de.hirola.runningplan.util.ModalOptionDialog;
import org.jetbrains.annotations.NotNull;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

public class RunningPlanDetailsFragment extends Fragment implements View.OnClickListener {

    private final static String TAG = RunningPlanDetailsFragment.class.getSimpleName();

    private AppLogManager logManager;
    private RunningPlanViewModel viewModel;
    private User appUser;
    private String runningPlanUUID;
    private RunningPlan runningPlan;
    private boolean isUsersRunningPlan;
    private TextView runningPlanNameTextView;
    private TextView runningPlanRemarksTextView;
    private Button showTrainingDetailsButton;
    private Button saveRunningPlanButton;
    private SwitchCompat activeRunningPlanSwitch;
    private Spinner startWeekSpinner;
    private StartDateArrayAdapter startWeekSpinnerArrayAdapter;

    // needed to instantiate fragment
    public RunningPlanDetailsFragment() {
        // Required empty public constructor
    }

    public static RunningPlanDetailsFragment newInstance(String uuid) {
        RunningPlanDetailsFragment fragment = new RunningPlanDetailsFragment();
        Bundle args = new Bundle();
        args.putString("uuid", uuid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // get the argument - the uuid for the selected running plan
        if (getArguments() != null) {
            runningPlanUUID = getArguments().getString("uuid");
        } else {
            runningPlanUUID = "";
        }
        // app logger
        logManager = AppLogManager.getInstance(requireContext());
        // app data
        viewModel = new ViewModelProvider(requireActivity()).get(RunningPlanViewModel.class);
        appUser = viewModel.getAppUser();
        MutableListLiveData<RunningPlan> mutableRunningPlans = viewModel.getMutableRunningPlans();
        List<RunningPlan> runningPlans = mutableRunningPlans.getValue();

        if (runningPlanUUID != null) {
            // set the selected running plan for details
            if (runningPlans != null) {
                for (RunningPlan plan : runningPlans) {
                    if (plan.getUUID().equalsIgnoreCase(runningPlanUUID)) {
                        runningPlan = plan;
                    }
                }
            }
        }
        // active running plan?
        if (appUser != null && runningPlan != null) {
            RunningPlan activeRunningPlan = appUser.getActiveRunningPlan();
            if (activeRunningPlan != null) {
                isUsersRunningPlan = activeRunningPlan.getUUID().equalsIgnoreCase(runningPlan.getUUID());
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
    public void onClick(View v) {
        if (v == saveRunningPlanButton) {
            // save running plan and go back
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
                            getString(R.string.question), getString(R.string.remove_active_running_plan),
                            getString(R.string.ok), getString(R.string.cancel),
                            button -> {
                                if (button == ModalOptionDialog.Button.OK) {
                                    // set the active running plan to null
                                    appUser.setActiveRunningPlan(null);
                                } else {
                                    activeRunningPlanSwitch.setChecked(true);
                                }
                            });
                } else {
                    appUser.setActiveRunningPlan(runningPlan);
                }
                // save the user and the running plan
                try {
                    viewModel.save(appUser);
                    viewModel.save(runningPlan);
                } catch (SportsLibraryException exception) {
                    ModalOptionDialog.showMessageDialog(
                            ModalOptionDialog.DialogStyle.CRITICAL,
                            requireContext(),
                            getString(R.string.error), getString(R.string.save_data_error),
                            getString(R.string.ok));
                    // disable switch again
                    activeRunningPlanSwitch.setChecked(false);
                    if (logManager.isDebugMode()) {
                        logManager.log(TAG, null, exception);
                    }
                }

            }
        }
    }

    public String getUUID() {
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
        // initialize the switch
        activeRunningPlanSwitch = view.findViewById(R.id.fgmt_running_plan_details_active_plan_switch);
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
                    if (logManager.isDebugMode()) {
                        logManager.log(TAG, null, exception);
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