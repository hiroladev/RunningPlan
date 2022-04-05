package de.hirola.runningplan.ui.info.log;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import de.hirola.runningplan.R;

import de.hirola.runningplan.util.AppLogManager;
import de.hirola.sportslibrary.util.LogManager;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * Fragment to show the content of a log file.
 *
 * @author Michael Schmidt (Hirola)
 * @since 1.1.1
 */
public class InfoLogContentFragment extends Fragment {

    private final LogManager.LogContent logContent;

    public InfoLogContentFragment(LogManager.LogContent logContent) {
        this.logContent = logContent;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View logContentView = inflater.inflate(R.layout.fragment_info_log_content, container, false);
        // show the log file content
        TextView logContentTextView = logContentView.findViewById(R.id.fgmt_info_log_content_textview);
        // enable button only if sending allowed
        Button sendDebugLogButton = logContentView.findViewById(R.id.fgmt_info_log_content_send_button);
        AppLogManager appLogManager = AppLogManager.getInstance(requireContext());
        sendDebugLogButton.setEnabled(appLogManager.canSendDebugLog());
        logContentTextView.setText(logContent.contentString);
        logContentTextView.setMovementMethod(new ScrollingMovementMethod());
        return logContentView;
    }

}