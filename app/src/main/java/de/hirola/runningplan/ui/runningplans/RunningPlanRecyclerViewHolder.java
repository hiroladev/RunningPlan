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
 * @since 1.1.1
 */
public class RunningPlanRecyclerViewHolder extends RecyclerView.ViewHolder {

    TextView nameTextView;
    TextView remarksTextView;
    TextView startDateTextView;
    TextView durationTextView;
    TextView percentCompletedTextView;
    ImageView statusImageView;

    public RunningPlanRecyclerViewHolder(@NonNull @NotNull View itemView) {
        super(itemView);
        statusImageView = itemView.findViewById(R.id.menu_item_imageview);
        nameTextView = itemView.findViewById(R.id.menu_item_textview);
        remarksTextView = itemView.findViewById(R.id.running_entry_units_textview);
        startDateTextView = itemView.findViewById(R.id.runningplan_row_startdate_textview);
        durationTextView = itemView.findViewById(R.id.runningplan_row_duration_textView);
        percentCompletedTextView = itemView.findViewById(R.id.runningplan_row_percentcompleted_textview);
    }
}
