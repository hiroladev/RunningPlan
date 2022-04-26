package de.hirola.runningplan.ui.runningplans;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * Custom Adapter for start dates in spinner.
 *
 * @author Michael Schmidt (Hirola)
 * @since v0.1
 */
public class StartDateArrayAdapter extends ArrayAdapter<LocalDate> {

    private final List<LocalDate> startDates;

    public StartDateArrayAdapter(Context context, int resource, List<LocalDate> startDates) {
        super(context, resource, startDates);
        this.startDates = startDates;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // a simple label with the date
        TextView label = (TextView) super.getView(position, convertView, parent);
        if (startDates != null) {
            if (position < startDates.size()) {
                LocalDate startDate = startDates.get(position);
                String trainingDateAsString = startDate
                        .getDayOfWeek()
                        .getDisplayName(TextStyle.FULL, Locale.getDefault());
                trainingDateAsString += " (";
                trainingDateAsString += startDate
                        .format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
                trainingDateAsString += ")";
                label.setText(trainingDateAsString);
            }
        }
        return label;
    }

    @Override
    public View getDropDownView(int position, @Nullable @org.jetbrains.annotations.Nullable View convertView, @NonNull @NotNull ViewGroup parent) {
        // a simple label with the date
        TextView label = (TextView) super.getView(position, convertView, parent);
        if (startDates != null) {
            if (position < startDates.size()) {
                LocalDate startDate = startDates.get(position);
                String trainingDateAsString = startDate
                        .getDayOfWeek()
                        .getDisplayName(TextStyle.FULL, Locale.getDefault());
                trainingDateAsString += " (";
                trainingDateAsString += startDate
                        .format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
                trainingDateAsString += ")";
                label.setText(trainingDateAsString);
            }
        }
        return label;
    }

    @Nullable
    @Override
    public LocalDate getItem(int position) {
        if (startDates != null) {
            return startDates.get(position);
        }
        return null;
    }

    @Override
    public int getCount(){
        if (startDates != null) {
            return startDates.size();
        }
        return 0;
    }
}
