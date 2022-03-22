package de.hirola.runningplan.ui.runningplans;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import de.hirola.runningplan.R;
import org.jetbrains.annotations.NotNull;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * Custom Adapter for running plan units to view in list.
 *
 * @author Michael Schmidt (Hirola)
 * @since 0.1
 */
public class RunningEntryRecyclerViewHolder extends RecyclerView.ViewHolder {

    final ImageView statusImageView;
    final TextView dateTextView;
    final TextView durationTextView;
    final TextView unitsTextView;

    public RunningEntryRecyclerViewHolder(@NonNull @NotNull View itemView) {
        super(itemView);
        statusImageView = itemView.findViewById(R.id.running_entry_simple_row_imageview);
        dateTextView = itemView.findViewById(R.id.track_row_name_label);
        durationTextView = itemView.findViewById(R.id.running_entry_row_duration_textview);
        unitsTextView = itemView.findViewById(R.id.running_entry_row_units_textview);
    }
}
