package de.hirola.runningplan.ui.info.log;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hirola.runningplan.R;
import de.hirola.runningplan.ui.info.menu.InfoMenuItemRecyclerView;
import de.hirola.runningplan.util.AppLogManager;
import de.hirola.runningplan.util.ModalOptionDialog;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * Fragment to view and send debug logs.
 *
 * @author Michael Schmidt (Hirola)
 * @since 0.1
 */
public class InfoLogsFragment extends Fragment implements View.OnClickListener {

    private AppLogManager appLogManager;
    private RecyclerView recyclerView; // recycler view list adapter
    private Button sendLogButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appLogManager = AppLogManager.getInstance(requireContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View infoLogView = inflater.inflate(R.layout.fragment_info_log, container, false);
        // send debug logs
        sendLogButton = infoLogView.findViewById(R.id.fgmt_info_log_send_button);
        sendLogButton.setOnClickListener(this);
        //TODO: button on debug and if list not empty
        sendLogButton.setEnabled(false);
        // show log files
        AppLogManager appLogManager = AppLogManager.getInstance(requireContext());
        InfoLogsFileRecyclerView listAdapter = new InfoLogsFileRecyclerView(requireContext(),
                appLogManager.getLogContent());
        listAdapter.setOnClickListener(this); // view log file content on click
        recyclerView = infoLogView.findViewById(R.id.fgmt_info_log_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(listAdapter);
        return infoLogView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        if (v instanceof Button) {
            ModalOptionDialog.showYesNoDialog(
                    requireContext(),
                    getString(R.string.question), getString(R.string.ask_before_send_log),
                    getString(R.string.ok), getString(R.string.cancel),
                    button -> {
                        if (button == ModalOptionDialog.Button.OK) {
                            if (!appLogManager.sendDebugLog()) {
                                ModalOptionDialog.showMessageDialog(
                                        ModalOptionDialog.DialogStyle.WARNING,
                                        requireContext(),null,
                                        getString(R.string.sending_failed),
                                        null);
                            }
                        }
                    });
        }
    }

}