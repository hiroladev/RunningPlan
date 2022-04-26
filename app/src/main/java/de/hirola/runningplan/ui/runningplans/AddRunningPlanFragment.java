package de.hirola.runningplan.ui.runningplans;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import de.hirola.runningplan.R;
import de.hirola.runningplan.RunningPlanApplication;
import de.hirola.runningplan.model.RunningPlanViewModel;
import de.hirola.sportsapplications.SportsLibrary;
import de.hirola.sportsapplications.SportsLibraryException;
import de.hirola.sportsapplications.model.RunningPlan;
import de.hirola.runningplan.util.ModalOptionDialog;
import de.hirola.sportsapplications.util.TemplateLoader;
import org.tinylog.Logger;

import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * A fragment to add or import new running plans.
 *
 * @author Michael Schmidt (Hirola)
 * @since v0.1
 */
    public class AddRunningPlanFragment extends Fragment implements View.OnClickListener {

    private SportsLibrary sportsLibrary;
    private RunningPlanApplication runningPlanApplication;
    private RunningPlanViewModel viewModel; // view model
    private RunningPlan runningPlan;
    private ActivityResultLauncher<Intent> someActivityResultLauncher;
    private TextView runningPlanNameTextView;
    private TextView runningPlanRemarksTextView;
    private TextView importFileNameLabel;
    private Button selectImportFileButton;
    private Button importRunningPlanButton;

    public AddRunningPlanFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        runningPlanApplication = (RunningPlanApplication) requireActivity().getApplication();
        sportsLibrary = runningPlanApplication.getSportsLibrary();
        // add ActivityResultLauncher for file dialog
        someActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent data = result.getData();
                        loadRunningPlanFromFile(data);
                    }
                });
        viewModel = new RunningPlanViewModel(requireActivity().getApplication(), null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_running_plan, container, false);
        // initialize the ui
        setViewElements(view);
        return view;
    }

    @Override
    public void onClick(View v) {
        if (v == selectImportFileButton) {
            // select the template file
            selectTemplateFile();
        }
        if (v == importRunningPlanButton) {
            // import the running plan template
            addRunningPlanToDatastore();
        }
    }

    private void setViewElements(View view) {
        // initialize the text views
        runningPlanNameTextView = view.findViewById(R.id.fgmt_add_running_plan_name_edittext);
        runningPlanRemarksTextView = view.findViewById(R.id.fgmt_add_running_plan_remarks_edittext);
        importFileNameLabel = view.findViewById(R.id.fgmt_add_running_plan_file_name_label);
        // initialize the button
        selectImportFileButton = view.findViewById(R.id.fgmt_add_running_plan_select_import_button);
        selectImportFileButton.setOnClickListener(this);
        importRunningPlanButton = view.findViewById(R.id.fgmt_add_running_plan_import_button);
        importRunningPlanButton.setOnClickListener(this);
        // initialize the switch
        SwitchCompat importAsTemplateSwitch = view.findViewById(R.id.fgmt_add_running_plan_template_switch);
    }

    private void selectTemplateFile() {
        // start the select file dialog
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT)
            .addCategory(Intent.CATEGORY_OPENABLE)
            .setType("*/*");

        someActivityResultLauncher.launch((Intent.createChooser(intent, getString(R.string.select_import_file))));
    }

    private void loadRunningPlanFromFile(Intent data) {
        if (data != null) {
            Uri uri = data.getData();
            importFileNameLabel.setText(uri.getLastPathSegment());
            try {
                InputStream inputStream = requireActivity().getContentResolver().openInputStream(uri);
                TemplateLoader templateLoader = new TemplateLoader(sportsLibrary, runningPlanApplication);
                importFileNameLabel.setText(getString(R.string.loading_file_succeed));
                // load the template from json
                runningPlan = templateLoader.loadRunningPlanFromJSON(inputStream);
                // show data from template in ui
                runningPlanNameTextView.setText(runningPlan.getName());
                runningPlanRemarksTextView.setText(runningPlan.getRemarks());
                // enable the import button
                importRunningPlanButton.setEnabled(true);
            } catch (FileNotFoundException | SportsLibraryException exception) {
                exception.printStackTrace();
            }
        } else {
            ModalOptionDialog.showMessageDialog(
                    ModalOptionDialog.DialogStyle.WARNING,
                    requireContext(),
                    null,
                    getString(R.string.no_file_selected),
                    null);
        }
    }

    private void addRunningPlanToDatastore() {
        if (runningPlan != null) {
            String name = runningPlanNameTextView.getText().toString();
            String remarks = runningPlanRemarksTextView.getText().toString();
            if (name.length() == 0) {
                ModalOptionDialog.showMessageDialog(
                        ModalOptionDialog.DialogStyle.WARNING,
                        requireContext(),
                        null,
                        getString(R.string.name_must_be_not_null),
                        null);
            } else {
                try {
                    runningPlan.setName(name);
                    runningPlan.setRemarks(remarks);
                    sportsLibrary.add(runningPlan);
                    if (viewModel.addObject(runningPlan)) {
                        // info to user
                        ModalOptionDialog.showMessageDialog(
                                ModalOptionDialog.DialogStyle.INFORMATION,
                                requireContext(),
                                null,
                                getString(R.string.import_succeed),
                                null);
                        // back to the running plan list
                        getParentFragmentManager().popBackStack();
                    } else {
                        ModalOptionDialog.showMessageDialog(
                                ModalOptionDialog.DialogStyle.WARNING,
                                requireContext(),
                                null,
                                getString(R.string.import_failed),
                                null);
                    }
                } catch (SportsLibraryException exception) {
                    if (sportsLibrary.isDebugMode()) {
                        Logger.debug(exception,"Import of running plan template failed.");
                    }
                }
            }
        }
    }
}