package de.hirola.runningplan.ui.info.log;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import de.hirola.runningplan.R;
import de.hirola.sportslibrary.model.Track;
import de.hirola.sportslibrary.util.LogManager;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Map;

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
    List<LogManager.LogContent> logContentList;

    public InfoLogsFileRecyclerView(Context context, @NonNull List<LogManager.LogContent> logContentList) {
        this.logContentList = logContentList;
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
            InfoLogsRecyclerViewHolder viewHolder = (InfoLogsRecyclerViewHolder) holder;
            viewHolder.dateTextView.setText(String.valueOf(logContentList.get(position).creationDate));
        }
    }

    @Override
    public int getItemCount() {
        return logContentList.size();
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    /*public void submitList(@NonNull List<Track> tracks) {
        this.tracks = tracks;
    }*/
}
