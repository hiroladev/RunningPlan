package de.hirola.runningplan.ui.info.tracks;

import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import de.hirola.runningplan.R;
import org.jetbrains.annotations.NotNull;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * Custom adapter for a track to view in a list.
 *
 * @author Michael Schmidt (Hirola)
 * @since 0.1
 */
public class InfoTrackRecyclerViewHolder extends RecyclerView.ViewHolder {

    final TextView nameTextView;
    final TextView dateTextView;
    final TextView startTimeTextView;
    final TextView stopTimeTextView;
    final TextView durationTextView;
    final TextView distanceTextView;
    final TextView averageSpeedTextView;
    final TextView altitudeDifferenceTextView;

    public InfoTrackRecyclerViewHolder(@NonNull @NotNull View itemView) {
        super(itemView);
        nameTextView = itemView.findViewById(R.id.running_entry_simple_row_date_label);
        dateTextView = itemView.findViewById(R.id.track_row_date_label);
        startTimeTextView = itemView.findViewById(R.id.track_row_start_time_label);
        stopTimeTextView = itemView.findViewById(R.id.track_row_stop_time_label);
        durationTextView = itemView.findViewById(R.id.track_row_duration_label);
        distanceTextView = itemView.findViewById(R.id.track_row_distance_label);
        averageSpeedTextView = itemView.findViewById(R.id.track_row_avg_label);
        altitudeDifferenceTextView = itemView.findViewById(R.id.track_row_altitude_difference_label);
    }
}
