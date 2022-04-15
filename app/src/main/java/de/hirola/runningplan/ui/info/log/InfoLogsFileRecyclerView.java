package de.hirola.runningplan.ui.info.log;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import de.hirola.runningplan.R;
import de.hirola.sportslibrary.util.LogContent;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * Custom adapter for a track to view in a list.
 *
 * @author Michael Schmidt (Hirola)
 * @since 0.1
 */
public class InfoLogsFileRecyclerView extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private View.OnClickListener onClickListener;
    final List<LogContent> logContentList;
    LogContent selectedLogContent;

    public InfoLogsFileRecyclerView(Context context, @NonNull List<LogContent> logContentList) {
        this.logContentList = logContentList;
        selectedLogContent = null;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.log_file_row, parent, false);
        v.setOnClickListener(onClickListener);
        return new InfoLogsRecyclerViewHolder(v);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof InfoLogsRecyclerViewHolder) {
            if (logContentList.size() > position) {
                selectedLogContent = logContentList.get(position);
                InfoLogsRecyclerViewHolder viewHolder = (InfoLogsRecyclerViewHolder) holder;
                // view the date of log file
                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL);
                viewHolder.dateTextView.setText(dateTimeFormatter.format(selectedLogContent.creationDate));
                // add click listener to viewing details
                viewHolder.detailsButton.setOnClickListener(onClickListener);
            }
        }
    }

    @Override
    public int getItemCount() {
        return logContentList.size();
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    @Nullable
    public LogContent getSelectedLogContent() {
        return selectedLogContent;
    }
}
