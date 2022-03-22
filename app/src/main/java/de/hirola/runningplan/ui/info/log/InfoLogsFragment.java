package de.hirola.runningplan.ui.info.log;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
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
import de.hirola.runningplan.util.AppLogManager;
import de.hirola.runningplan.util.ModalOptionDialog;
import de.hirola.sportslibrary.Global;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

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

    private AppLogManager logManager;
    private SharedPreferences sharedPreferences;
    private TextView logContentTextView;
    private Button sendLogButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        logManager = AppLogManager.getInstance(requireContext());
        // preferences - sending debug log?
        sharedPreferences = requireContext()
                .getSharedPreferences(getString(R.string.preference_file), Context.MODE_PRIVATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View infoLogView = inflater.inflate(R.layout.fragment_info_log, container, false);
        sendLogButton = infoLogView.findViewById(R.id.fgmt_info_log_send_button);
        sendLogButton.setOnClickListener(this);
        sendLogButton.setEnabled(false);
        logContentTextView = infoLogView.findViewById(R.id.fgmt_info_log_content_textview);
        logContentTextView.setMovementMethod(new ScrollingMovementMethod());
        loadLogContent();
        return infoLogView;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadLogContent();
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
                            if (!logManager.sendDebugLog()) {
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

    private void loadLogContent() {
        String logContentString = logManager.getLogContent();
        logContentTextView.setText(logContentString);
        if (!logContentString.isEmpty() &&
                sharedPreferences.getBoolean(Global.PreferencesKeys.sendDebugLog, false)) {
            sendLogButton.setEnabled(true);
        }
    }
}