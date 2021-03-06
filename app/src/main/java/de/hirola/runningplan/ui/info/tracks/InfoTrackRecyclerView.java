package de.hirola.runningplan.ui.info.tracks;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import de.hirola.runningplan.R;
import de.hirola.sportsapplications.model.Track;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
 * @since v0.1
 */
public class InfoTrackRecyclerView extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private View.OnClickListener onClickListener;
    private List<Track> tracks;

    public InfoTrackRecyclerView(Context context, @NonNull List<Track> tracks) {
        this.tracks = tracks;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.track_row, parent, false);
        v.setOnClickListener(onClickListener);
        return new InfoTrackRecyclerViewHolder(v);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof InfoTrackRecyclerViewHolder) {
            InfoTrackRecyclerViewHolder viewHolder = (InfoTrackRecyclerViewHolder) holder;
            // view a track
            // on click is todo
            Track track = tracks.get(position);
            viewHolder.nameTextView.setText(track.getName());
            // format start time
            Instant startTime = Instant.ofEpochMilli(track.getStartTimeInMilli());
            LocalDateTime startDateTime = LocalDateTime.ofInstant(startTime, ZoneId.systemDefault());
            // format date
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL);
            viewHolder.dateTextView.setText(dateFormatter.format(startDateTime));
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            viewHolder.startTimeTextView.setText(timeFormatter.format(startDateTime));
            // format stop time
            Instant stopTime = Instant.ofEpochMilli(track.getStopTimeInMilli());
            LocalDateTime stopDateTime = LocalDateTime.ofInstant(stopTime, ZoneId.systemDefault());
            viewHolder.stopTimeTextView.setText(timeFormatter.format(stopDateTime));
            // duration in h or min
            long duration = track.getDuration();
            if (duration > 0 &&  duration < 60) {
                viewHolder.durationTextView.setText(String.format("%s%s", duration, " min"));
            } else if (duration > 59) {
                viewHolder.durationTextView.setText(String.format("%s%s%s%s",
                        duration / 60, "h : ", duration % 60, " min"));
            } else {
                viewHolder.durationTextView.setText("-");
            }
            // distance in m or km
            double distance = track.getDistance();
            if (distance > 0 && distance < 999) {
                viewHolder.distanceTextView.setText(String.format("%,.2f%s", distance, " m"));
            } else if (distance > 999) {
                viewHolder.distanceTextView.setText(String.format("%,.2f%s", distance / 1000, " km"));
            } else {
                viewHolder.distanceTextView.setText("-");
            }
            viewHolder.averageSpeedTextView.setText(String.format("%,.2f%s",track.getAverageSpeed(), " km/h"));
            //TODO: track altitude
            viewHolder.altitudeDifferenceTextView.setText("-");
        }
    }

    @Override
    public int getItemCount() {
        return tracks.size();
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public void submitList(@NonNull List<Track> tracks) {
        this.tracks = tracks;
    }
}
