package de.hirola.runningplan.ui.runningplans;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import de.hirola.runningplan.R;
import de.hirola.runningplan.util.AppLogManager;
import de.hirola.sportslibrary.model.RunningPlan;

import android.content.res.Resources;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import org.jetbrains.annotations.NotNull;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * Custom Adapter for RunningPlans to view in list.
 *
 * @author Michael Schmidt (Hirola)
 * @since 1.1.1
 */
public class RunningPlanRecyclerView extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final static String TAG = RunningPlanRecyclerView.class.getSimpleName();

    private final AppLogManager logManager;
    private final Context context;
    private final List<RunningPlan> runningPlans;
    private View.OnClickListener onClickListener;

    public RunningPlanRecyclerView(Context context, List<RunningPlan> runningPlans) {
        this.context = context;
        this.runningPlans = runningPlans;
        logManager = AppLogManager.getInstance(context);
    }

    @NonNull
    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.running_plan_row, parent, false);
        v.setOnClickListener(onClickListener);
        return new RunningPlanRecyclerViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof RunningPlanRecyclerViewHolder) {
            RunningPlanRecyclerViewHolder viewHolder = (RunningPlanRecyclerViewHolder) holder;
            // Laufplan darstellen
            RunningPlan runningPlan = runningPlans.get(position);
            // Name und Status (mittels Bild darstellen)
            try {
                if (runningPlan.isActive()) {
                    viewHolder.statusImageView.setImageResource(R.drawable.active20x20);
                }
                if (runningPlan.isCompleted()) {
                    viewHolder.statusImageView.setImageResource(R.drawable.completed20x20);
                }
            } catch (Resources.NotFoundException exception) {
                if (logManager.isDebugMode()) {
                    logManager.log(TAG, null, exception);
                }
            }
            viewHolder.nameTextView.setText(runningPlan.getName());
            // Anmerkungen
            if (runningPlan.isTemplate()) {
                // Text sollte in Ressourcen hinterlegt sein
                try {
                    // load strings from res dynamically
                    String remarksResourceString = runningPlan.getRemarks();
                    if (remarksResourceString.length() > 0) {
                        int remarksResourceStringId = context.getResources().getIdentifier(remarksResourceString,
                                "string", context.getPackageName());
                        String remarks = context.getString(remarksResourceStringId);
                        viewHolder.remarksTextView.setText(remarks);
                    }
                } catch (Resources.NotFoundException exception) {
                    viewHolder.remarksTextView.setText(R.string.no_remarks);
                    if (logManager.isDebugMode()) {
                        logManager.log(TAG, null, exception);
                    }
                }
            } else {
                // user's running plans
                viewHolder.remarksTextView.setText(runningPlan.getRemarks());
            }
            // Startdatum
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            viewHolder.startDateTextView.setText(formatter.format(runningPlan.getStartDate()));
            // Gesamtdauer des Trainings (komplette Trainingszeit in Minuten!)
            viewHolder.durationTextView.setText("");
            long duration = runningPlan.getDuration();
            if (duration > 0) {
                //  Darstellung in hh:mm
                //  in h und min umrechnen
                long hours = duration / 60;
                long minutes = duration % 60;
                String durationString;
                if (hours > 0) {
                    durationString = hours
                            + " h "
                            + minutes
                            + " min";
                } else {
                    durationString = hours
                            + minutes
                            + " min";
                }
                viewHolder.durationTextView.setText(durationString);
            } else {
                viewHolder.durationTextView.setText(R.string.duration_null_hours_minutes);
            }
            // Laufplan abgeschlossen in Prozent
            viewHolder.percentCompletedTextView.setText(String.valueOf(runningPlan.percentCompleted()));
        }
    }

    @Override
    public int getItemCount() {
        if (runningPlans != null) {
            return runningPlans.size();
        }
        return 0;
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

}
