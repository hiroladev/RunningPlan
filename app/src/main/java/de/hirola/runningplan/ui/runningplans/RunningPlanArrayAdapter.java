package de.hirola.runningplan.ui.runningplans;

import de.hirola.runningplan.R;
import de.hirola.sportslibrary.Global;
import de.hirola.sportslibrary.model.RunningPlan;

import android.content.res.Resources;
import android.widget.ImageView;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * Custom Adapter for RunningPlans to view in list.
 *
 * @author Michael Schmidt (Hirola)
 * @since 1.1.1
 */
public class RunningPlanArrayAdapter extends ArrayAdapter<RunningPlan> {

    private final Context context;
    private List<RunningPlan> runningPlans;

    public RunningPlanArrayAdapter(Context context, List<RunningPlan> runningPlans) {
        super(context, R.layout.runningplan_row, runningPlans);
        this.context = context;
        this.runningPlans = runningPlans;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.runningplan_row, parent, false);
        // Laufplan darstellen
        RunningPlan runningPlan = runningPlans.get(position);
        // Name und Status (mittels Bild darstellen)
        ImageView statusImageView = rowView.findViewById(R.id.runningplan_row_state_imageview);
        try {
            if (runningPlan.isActive()) {
                statusImageView.setImageDrawable(context.getResources().getDrawable(R.drawable.active20x20, null));
            }
            if (runningPlan.completed()) {
                statusImageView.setImageDrawable(context.getResources().getDrawable(R.drawable.completed20x20, null));
            }
        } catch (Resources.NotFoundException exception) {
            //TODO:Logging
            exception.printStackTrace();
        }
        TextView nameTextView = rowView.findViewById(R.id.runningplan_row_name_textview);
        nameTextView.setText(runningPlan.getName());
        // Anmerkungen
        TextView remarksTextView = rowView.findViewById(R.id.runningplan_row_remarks_textview);
        if (runningPlan.isTemplate()) {
            // Text sollte in Ressourcen hinterlegt sein
            try {
                // load strings from res dynamically
                String remarksResourceString = runningPlan.getRemarks();
                if (remarksResourceString.length() > 0) {
                    int remarksResourceStringId = getContext().getResources().getIdentifier(remarksResourceString,
                            "string", getContext().getPackageName());
                    String remarks = context.getString(remarksResourceStringId);
                    remarksTextView.setText(remarks);
                }
            } catch (Resources.NotFoundException exception) {
                remarksTextView.setText(R.string.no_remarks);
                if (Global.DEBUG) {
                    // TODO: Logging
                }
            }
        }
        remarksTextView.setText(runningPlan.getRemarks());
        // Startdatum
        TextView startDateTextView = rowView.findViewById(R.id.runningplan_row_startdate_textview);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        startDateTextView.setText(formatter.format(runningPlan.getStartDate()));
        // Gesamtdauer des Trainings (komplette Trainingszeit in Minuten!)
        TextView durationTextView = rowView.findViewById(R.id.runningplan_row_duration_textView);
        durationTextView.setText("");
        int duration = runningPlan.getDuration();
        if (duration > 0) {
            //  Darstellung in hh:mm
            //  in h und min umrechnen
            int hours = duration / 60;
            int minutes = duration % 60;
            durationTextView.setText(String.format(Locale.GERMANY,"%d:%d",hours, minutes));
        } else {
            durationTextView.setText("00h:00min");
        }
        // Laufplan abgeschlossen in Prozent
        String percentCompleted = String.valueOf(runningPlan.percentCompleted()).concat(" %");
        TextView percentCompletedTextView = rowView.findViewById(R.id.runningplan_row_percentcompleted_textview);
        percentCompletedTextView.setText(percentCompleted);
        return rowView;
    }

    public void submitList(List<RunningPlan> runningPlans) {
        // update the data
        this.runningPlans = runningPlans;
        // refresh the view
        notifyDataSetChanged();
    }
}
