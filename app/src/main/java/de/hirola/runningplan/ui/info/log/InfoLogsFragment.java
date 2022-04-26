package de.hirola.runningplan.ui.info.log;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hirola.runningplan.R;
import de.hirola.runningplan.RunningPlanApplication;
import de.hirola.runningplan.util.ModalOptionDialog;
import de.hirola.sportsapplications.SportsLibrary;
import de.hirola.sportsapplications.util.LogContent;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * Fragment to view and send debug logs.
 *
 * @author Michael Schmidt (Hirola)
 * @since v0.1
 */
public class InfoLogsFragment extends Fragment implements View.OnClickListener {

    private SportsLibrary sportsLibrary;
    private InfoLogsFileRecyclerView listAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sportsLibrary = ((RunningPlanApplication) requireContext().getApplicationContext()).getSportsLibrary();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View infoLogView = inflater.inflate(R.layout.fragment_info_log, container, false);
        listAdapter = new InfoLogsFileRecyclerView(requireContext(), sportsLibrary.getLogContent());
        listAdapter.setOnClickListener(this); // view log file content on click
        // recycler view list adapter
        RecyclerView recyclerView = infoLogView.findViewById(R.id.fgmt_info_log_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(listAdapter);
        return infoLogView;
    }

    @Override
    public void onClick(View view) {
        if (view instanceof Button) {
            // view the content of the selected log file
            if (view.getId() == R.id.log_file_row_button) {
                // get the selected log content
                LogContent logContent = listAdapter.getSelectedLogContent();
                // open in new fragment
                InfoLogContentFragment infoLogContentFragment = new InfoLogContentFragment(logContent);
                // hide this fragment
                FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
                fragmentTransaction.hide(this);
                // starts the fragment
                fragmentTransaction.replace(R.id.fragment_info_container, infoLogContentFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
            // send the log file
            if (view.getId() == R.id.fgmt_info_log_content_send_button) {
                ModalOptionDialog.showYesNoDialog(
                        requireContext(),
                        getString(R.string.question), getString(R.string.ask_before_send_log),
                        getString(R.string.ok), getString(R.string.cancel),
                        button -> {
                            if (button == ModalOptionDialog.Button.OK) {
                                if (!sportsLibrary.sendDebugLogs()) {
                                    ModalOptionDialog.showMessageDialog(
                                            ModalOptionDialog.DialogStyle.WARNING,
                                            requireContext(), null,
                                            getString(R.string.sending_failed),
                                            null);
                                }
                            }
                        });
            }
        }
    }

}