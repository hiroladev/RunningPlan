package de.hirola.runningplan.ui.info.log;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import de.hirola.runningplan.R;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * Custom adapter for a track to view in a list.
 *
 * @author Michael Schmidt (Hirola)
 * @since v0.1
 */
public class InfoLogsRecyclerViewHolder extends RecyclerView.ViewHolder {

    final TextView dateTextView;
    final Button detailsButton;

    public InfoLogsRecyclerViewHolder(@NonNull View itemView) {
        super(itemView);
        dateTextView = itemView.findViewById(R.id.log_file_row_date_label);
        detailsButton = itemView.findViewById(R.id.log_file_row_button);

    }
}
