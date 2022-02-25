package de.hirola.runningplan.ui.info;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
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
public class InfoLicenseFragment extends Fragment {

    private final static String TAG = InfoLicenseFragment.class.getSimpleName();

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
        View aboutView = inflater.inflate(R.layout.fragment_info_licenses, container, false);
        TextView licenseTextView = aboutView.findViewById(R.id.fgmt_info_licenses_text_view);
        licenseTextView.setMovementMethod(new ScrollingMovementMethod());
        // load content from text file
        // TODO: english licenses
        String licenseString = "";
        try {
            InputStream is = getResources().openRawResource(R.raw.licenses);
            licenseString = IOUtils.toString(is, StandardCharsets.UTF_8);
        } catch (Exception exception) {
            if (logManager.isDebugMode()) {
                logManager.log(TAG, null, exception);
            }
        }
        licenseTextView.setText(licenseString);
        return aboutView;
    }
}