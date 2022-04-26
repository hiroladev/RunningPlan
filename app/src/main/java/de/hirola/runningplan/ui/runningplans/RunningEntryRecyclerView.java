package de.hirola.runningplan.ui.runningplans;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import de.hirola.runningplan.R;
import de.hirola.runningplan.RunningPlanApplication;
import de.hirola.sportsapplications.SportsLibrary;
import de.hirola.sportsapplications.model.MovementType;
import de.hirola.sportsapplications.model.RunningPlan;
import de.hirola.sportsapplications.model.RunningPlanEntry;
import de.hirola.sportsapplications.model.RunningUnit;
import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * Custom Adapter for running plan units to view in list.
 *
 * @author Michael Schmidt (Hirola)
 * @since v0.1
 */
public class RunningEntryRecyclerView extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final SportsLibrary sportsLibrary;
    private final Context context;
    private final LocalDate startDate;
    private final List<RunningPlanEntry> entries; // can be empty

    public RunningEntryRecyclerView(Context context, @Nullable RunningPlan runningPlan) {
        this.context = context;
        if (runningPlan != null) {
            entries = runningPlan.getEntries();
            startDate = runningPlan.getStartDate();
        } else {
            entries = new ArrayList<>();
            startDate = LocalDate.now();
        }
        sportsLibrary = ((RunningPlanApplication) context.getApplicationContext()).getSportsLibrary();
    }

    @NonNull
    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.running_entry_row, parent, false);
        return new RunningEntryRecyclerViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof RunningEntryRecyclerViewHolder) {
            RunningEntryRecyclerViewHolder viewHolder = (RunningEntryRecyclerViewHolder) holder;
            // show an entry of the running plan
            RunningPlanEntry entry = entries.get(position);
            // Status (mittels Bild darstellen)
            try {
                if (entry.isCompleted()) {
                    viewHolder.statusImageView.setImageResource(R.drawable.completed20x20);
                } else {
                    viewHolder.statusImageView.setImageResource(R.drawable.unused20x20);
                }
            } catch (Resources.NotFoundException exception) {
                if (sportsLibrary.isDebugMode()) {
                    Logger.debug(null, exception);
                }
            }
            // training date
            viewHolder.dateTextView.setText(getTrainingDayForEntryAsString(entry));
            // duration of entry
            viewHolder.durationTextView.setText(buildStringForDuration(entry));
            // running units
            viewHolder.unitsTextView.setText(getTrainingUnitsAsString(entry));
        }
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    @NotNull
    private String buildStringForDuration(RunningPlanEntry entry) {
        long duration = entry.getDuration();
        // show the complete training time
        StringBuilder durationString = new StringBuilder(context.getString(R.string.total_time)+ " ");
        // display in hour or minutes?
        if (duration < 60) {
            durationString.append(duration);
        } else {
            //  in h und min umrechnen
            long hours = (duration * 60) / 3600;
            long minutes = (duration / 60) % 60;
            durationString.append(hours);
            durationString.append(" h : ");
            durationString.append(minutes);
        }
        durationString.append(" min");
        return durationString.toString();
    }

    // list of training days from selected running plan as string
    @NotNull
    private String getTrainingDayForEntryAsString(RunningPlanEntry entry) {
        int day = entry.getDay();
        int week = entry.getWeek();
        LocalDate trainingDate = startDate.plusDays(day - 1).plusWeeks(week - 1);
        return trainingDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault())
                + " ("
                + trainingDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
                + ")";
    }

    // list of training unit from selected running plan entry as string
    @NotNull
    private String getTrainingUnitsAsString(RunningPlanEntry entry) {
        StringBuilder unitsAsString = new StringBuilder();
        List<RunningUnit> runningUnits = entry.getRunningUnits();
        int loop = 1;
        for (RunningUnit runningUnit: runningUnits) {
            MovementType movementType = runningUnit.getMovementType();
            String key = movementType.getKey();
            String durationString = String.valueOf(runningUnit.getDuration());
            unitsAsString.append(durationString).append(" min ");
            // load the string dynamically
            int movementTypeResourceStringId = context
                    .getResources()
                    .getIdentifier(key, "string", context.getPackageName());
            if (loop < runningUnits.size()) {
                try {

                    unitsAsString.append(context.getString(movementTypeResourceStringId)).append("\n");
                } catch (Resources.NotFoundException exception) {
                    unitsAsString.append(key).append("\n");
                    if (sportsLibrary.isDebugMode()) {
                        Logger.debug("Movement type resource not found,", exception);
                    }
                }
            } else {
                try {
                    unitsAsString.append(context.getString(movementTypeResourceStringId));
                } catch (Resources.NotFoundException exception) {
                    unitsAsString.append(key);
                    if (sportsLibrary.isDebugMode()) {
                        Logger.debug("Movement type resource not found,", exception);
                    }
                }
            }
            loop++;
        }
        return unitsAsString.toString();
    }
}
