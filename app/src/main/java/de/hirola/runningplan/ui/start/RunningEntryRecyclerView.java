package de.hirola.runningplan.ui.start;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import de.hirola.runningplan.R;
import de.hirola.runningplan.RunningPlanApplication;
import de.hirola.sportsapplications.SportsLibrary;
import de.hirola.sportsapplications.model.MovementType;
import de.hirola.sportsapplications.model.RunningPlan;
import de.hirola.sportsapplications.model.RunningPlanEntry;
import de.hirola.sportsapplications.model.RunningUnit;
import org.tinylog.Logger;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * Custom adapter for entries of a running plan to view in list.
 *
 * @author Michael Schmidt (Hirola)
 * @since v0.1
 */
public class RunningEntryRecyclerView extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final SportsLibrary sportsLibrary;
    private final RunningPlan runningPlan;
    private final List<RunningPlanEntry> runningPlanEntries;
    private final List<String> trainingDaysAsString;

    public RunningEntryRecyclerView(Context context, @NonNull RunningPlan runningPlan) {
        this.runningPlan = runningPlan;
        this.runningPlanEntries = runningPlan.getEntries();
        trainingDaysAsString = getTrainingDaysAsStrings();
        sportsLibrary = ((RunningPlanApplication) context.getApplicationContext()).getSportsLibrary();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.running_entry_simple_row, parent, false);
        return new RunningEntryRecyclerViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof RunningEntryRecyclerViewHolder) {
            RunningEntryRecyclerViewHolder viewHolder = (RunningEntryRecyclerViewHolder) holder;
            // view a running plan entry with units
            RunningPlanEntry runningPlanEntry = runningPlanEntries.get(position);
            // state of entry
            try {
                if (runningPlanEntry.isCompleted()) {
                    viewHolder.statusImageView.setImageResource(R.drawable.baseline_done_black_36);
                } else {
                    viewHolder.statusImageView.setImageResource(R.drawable.baseline_self_improvement_black_24);
                }
            } catch (Resources.NotFoundException exception) {
                if (sportsLibrary.isDebugMode()) {
                    Logger.debug(null, exception);
                }
            }
            // training date
            if (trainingDaysAsString.size() > position) {
                viewHolder.dateTextView.setText(trainingDaysAsString.get(position));
            }
            // show the units
            StringBuilder unitsAsString = new StringBuilder();
            List<RunningUnit> runningUnits = runningPlanEntry.getRunningUnits();
            int loop = 1;
            for (RunningUnit runningUnit: runningUnits) {
                MovementType movementType = runningUnit.getMovementType();
                String durationString = String.valueOf(runningUnit.getDuration());
                unitsAsString.append(durationString).append(" min ");
                    if (loop < runningUnits.size()) {
                        unitsAsString.append(movementType.getName()).append("\n");
                    } else {
                        unitsAsString.append(movementType.getName());
                    }

                loop++;
            }
            viewHolder.unitsTextView.setText(unitsAsString.toString());

        }
    }

    @Override
    public int getItemCount() {
        return runningPlanEntries.size();
    }

    // list of training days from selected running plan as string
    @NonNull
    private List<String> getTrainingDaysAsStrings() {
        List<String> trainingDaysStringList = new ArrayList<>();
        // monday, day 1 and week 1
        LocalDate startDate = runningPlan.getStartDate();
        Iterator<RunningPlanEntry> iterator= runningPlan
                .getEntries()
                .stream()
                .iterator();
        while (iterator.hasNext()) {
            RunningPlanEntry entry = iterator.next();
            int day = entry.getDay();
            int week = entry.getWeek();
            LocalDate trainingDate = startDate.plusDays(day - 1);
            trainingDate = trainingDate.plusWeeks(week - 1);
            String trainingDateAsString = trainingDate
                    .getDayOfWeek()
                    .getDisplayName(TextStyle.FULL, Locale.getDefault());
            trainingDateAsString+= " (";
            trainingDateAsString+= trainingDate
                    .format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
            trainingDateAsString+= ")";
            trainingDaysStringList.add(trainingDateAsString);
        }
        return trainingDaysStringList;
    }

}
