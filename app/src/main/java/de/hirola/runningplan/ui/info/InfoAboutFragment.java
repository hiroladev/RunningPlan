package de.hirola.runningplan.ui.info;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import de.hirola.runningplan.R;
import de.hirola.runningplan.util.AppLogManager;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * Fragment to load and view infos about the app from text resources.
 *
 * @author Michael Schmidt (Hirola)
 * @since 1.1.1
 */
public class InfoAboutFragment extends Fragment {

    private final static String TAG = InfoAboutFragment.class.getSimpleName();

    private AppLogManager logManager; // app logger

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // app logger
        logManager = AppLogManager.getInstance(requireContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View aboutView = inflater.inflate(R.layout.fragment_info_about, container, false);
        TextView aboutTextView = aboutView.findViewById(R.id.fgmt_info_about_text_view);
        aboutTextView.setMovementMethod(new ScrollingMovementMethod());
        // load content from text file
        // TODO: english about
        String aboutString = "";
        try {
            InputStream is = getResources().openRawResource(R.raw.about);
            aboutString = IOUtils.toString(is, StandardCharsets.UTF_8);
        } catch (Exception exception) {
            if (logManager.isDebugMode()) {
                logManager.log(TAG, null, exception);
            }
        }
        aboutTextView.setText(aboutString);
        return aboutView;
    }
}