package de.hirola.runningplan.ui.training;

import androidx.lifecycle.ViewModelProvider;
import de.hirola.runningplan.model.RunningPlanViewModel;
import de.hirola.sportslibrary.model.RunningPlan;

import de.hirola.runningplan.R;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.widget.Spinner;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import de.hirola.sportslibrary.model.RunningPlanEntry;

import java.util.ArrayList;
import java.util.List;

public class TrainingFragment extends Fragment implements AdapterView.OnItemSelectedListener{

    // app data
    private RunningPlanViewModel viewModel;
    // the actual running plan, selected by the user
    // if no running plan selected, the plan with the lowest order number will be selected
    private RunningPlan selectedRunningPlan;
    //  aktuell aktive Trainingseinheit zum ausgewählten Trainingsplan
    //  z.B. Woche: 3, Tag: 1 (Montag), 7 min gesamt,  2 min Laufen, 3 min langsames Gehen, 2 min Laufen
    private RunningPlanEntry runningPlanEntry;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(RunningPlanViewModel.class);
        List<RunningPlan> runningPlans = viewModel.getRunningPlans().getValue();
        if (runningPlans != null && !runningPlans.isEmpty()) {
            // TODO: get plan from user
            selectedRunningPlan = runningPlans.get(0);
        }
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View trainingView = inflater.inflate(R.layout.fragment_training, container, false);

        // Spinner element
        Spinner spinner = trainingView.findViewById(R.id.spinner);

        // Spinner click listener
        spinner.setOnItemSelectedListener(this);

        // Spinner Drop down elements
        List<String> runningPlanEntriesNames = new ArrayList<>();
        if (selectedRunningPlan != null) {
            List<RunningPlanEntry> entries = selectedRunningPlan.getEntries();
        }

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item, runningPlanEntriesNames);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);

        // Inflate the layout for this fragment
        return trainingView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    /**
     * <p>Callback method to be invoked when an item in this view has been
     * selected. This callback is invoked only when the newly selected
     * position is different from the previously selected position or if
     * there was no selected item.</p>
     * <p>
     * Implementers can call getItemAtPosition(position) if they need to access the
     * data associated with the selected item.
     *
     * @param parent   The AdapterView where the selection happened
     * @param view     The view within the AdapterView that was clicked
     * @param position The position of the view in the adapter
     * @param id       The row id of the item that is selected
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    /**
     * Callback method to be invoked when the selection disappears from this
     * view. The selection can disappear for instance when touch is activated
     * or when the adapter becomes empty.
     *
     * @param parent The AdapterView that now contains no selected item.
     */
    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}