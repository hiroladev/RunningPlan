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
 * Custom Adapter for RunningPlans to view in list.
 *
 * @author Michael Schmidt (Hirola)
 * @since 0.1
 */
public class RunningPlanRecyclerViewHolder extends RecyclerView.ViewHolder {

    final TextView nameTextView;
    final TextView remarksTextView;
    final TextView startDateTextView;
    final TextView durationTextView;
    final TextView percentCompletedTextView;
    final ImageView statusImageView;

    public RunningPlanRecyclerViewHolder(@NonNull @NotNull View itemView) {
        super(itemView);
        statusImageView = itemView.findViewById(R.id.running_entry_simple_row_imageview);
        nameTextView = itemView.findViewById(R.id.track_row_name_label);
        remarksTextView = itemView.findViewById(R.id.running_entry_row_duration_textview);
        startDateTextView = itemView.findViewById(R.id.track_row_start_time_label);
        durationTextView = itemView.findViewById(R.id.track_row_stop_time_label);
        percentCompletedTextView = itemView.findViewById(R.id.track_row_duration_label);
    }
}
