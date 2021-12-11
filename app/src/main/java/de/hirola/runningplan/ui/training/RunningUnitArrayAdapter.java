package de.hirola.runningplan.ui.training;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import de.hirola.sportslibrary.model.RunningPlanEntry;
import de.hirola.sportslibrary.model.RunningUnit;

import java.util.List;

public class RunningUnitArrayAdapter extends ArrayAdapter<RunningUnit> {

    private final Context context;
    private List<RunningUnit> runningPlanEntries;

    public RunningUnitArrayAdapter(@NonNull Context context, int resource, @NonNull List<RunningUnit> runningPlanEntries) {
        super(context, resource, runningPlanEntries);
        this.context = context;
        this.runningPlanEntries = runningPlanEntries;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return super.getView(position, convertView, parent);
    }

    @Override
    public int getCount() {
        return runningPlanEntries.size();
    }

    @Nullable
    @Override
    public RunningUnit getItem(int position) {
        return runningPlanEntries.get(position);
    }

    public void submitList(List<RunningUnit> runningPlanEntries) {
        // update the data
        this.runningPlanEntries = runningPlanEntries;
        // refresh the view
        notifyDataSetChanged();
    }


}
