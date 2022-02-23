package de.hirola.runningplan.ui.runningplans;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import de.hirola.runningplan.R;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import de.hirola.runningplan.model.MutableListLiveData;
import de.hirola.runningplan.model.RunningPlanViewModel;
import de.hirola.runningplan.util.AppLogManager;
import de.hirola.sportslibrary.Global;
import de.hirola.sportslibrary.SportsLibraryException;
import de.hirola.sportslibrary.model.RunningPlan;
import de.hirola.runningplan.util.ModalOptionDialog;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * List-Fragment für die Darstellung aller vorhandener Laufpläne (Überblick).
 *

 * @author Michael Schmidt (Hirola)
 * @since 1.1.1
 */
public class RunningPlanListFragment extends Fragment implements View.OnClickListener {

    private final static String TAG = RunningPlanListFragment.class.getSimpleName();

    private AppLogManager logManager; // app logger
    // view model
    private RunningPlanViewModel viewModel;
    // recycler view list adapter
    private RecyclerView recyclerView;
    // last selected running plan (list index)
    private int lastSelectedIndex;
    // cached list of running plans
    private List<RunningPlan> runningPlans;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // get the last selected list index (running plan)
        if (savedInstanceState != null) {
            lastSelectedIndex = savedInstanceState.getInt("lastSelectedIndex");
        }
        // load the running plans
        // data
        viewModel = new ViewModelProvider(requireActivity()).get(RunningPlanViewModel.class);
        MutableListLiveData<RunningPlan> mutableRunningPlans = viewModel.getMutableRunningPlans();
        mutableRunningPlans.observe(requireActivity(), this::onListChanged);
        runningPlans = mutableRunningPlans.getValue();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_running_plan_list, container, false);
        // app logger
        logManager = AppLogManager.getInstance(requireContext());
        // should I hide templates?
        SharedPreferences sharedPreferences =
                requireContext().getSharedPreferences(requireContext().getString(R.string.preference_file), Context.MODE_PRIVATE);
        boolean hideTemplates = sharedPreferences.getBoolean(Global.PreferencesKeys.hideTemplates,false);
        // visualize the list of running plans
        RunningPlanRecyclerView listAdapter;
        if (hideTemplates) {
            Stream<RunningPlan> filteredStream = runningPlans.stream().filter(runningPlan -> !runningPlan.isTemplate());
            listAdapter = new RunningPlanRecyclerView(requireContext(), filteredStream.collect(Collectors.toList()));
        } else {
            listAdapter = new RunningPlanRecyclerView(requireContext(), runningPlans);
        }
        // details on click
        listAdapter.setOnClickListener(this);
        recyclerView = view.findViewById(R.id.recyclerView_running_plans);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        // remove running plan on swipe to left
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(
                new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT ) {

                    @Override
                    public boolean onMove(@NonNull @NotNull RecyclerView recyclerView,
                                          @NonNull @NotNull RecyclerView.ViewHolder viewHolder,
                                          @NonNull @NotNull RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(@NotNull RecyclerView.ViewHolder viewHolder, int swipeDir) {
                        // TODO: show menu on swipe (https://www.freecodecamp.org/news/how-to-implement-swipe-for-options-in-recyclerview/)
                        int position = viewHolder.getBindingAdapterPosition();
                        // is it allowed to remove templates
                        if (runningPlans.size() > position) {
                            RunningPlan runningPlan = runningPlans.get(position);
                            if (!runningPlan.isTemplate()) {
                                // warning before remove
                                ModalOptionDialog.showYesNoDialog(requireContext(),
                                        null,
                                        getString(R.string.ask_before_remove_running_plan),
                                        null,
                                        null,
                                        button -> {
                                            if (button == ModalOptionDialog.Button.OK) {
                                                // get the running plan
                                                int position1 = viewHolder.getBindingAdapterPosition();
                                                RunningPlan runningPlan1 = runningPlans.get(position1);
                                                // remove running plan
                                                try {
                                                    viewModel.delete(runningPlan1);
                                                    listAdapter.notifyItemRemoved(position1);
                                                } catch (SportsLibraryException exception) {
                                                    ModalOptionDialog.showMessageDialog(
                                                            ModalOptionDialog.DialogStyle.CRITICAL,
                                                            requireContext(),
                                                            null, getString(R.string.remove_active_running_plan),
                                                            null);
                                                    if (logManager.isDebugMode()) {
                                                        logManager.log(TAG, null, exception);
                                                    }
                                                }
                                            }
                                        });
                            } else {
                                //TODO: Nutzer informieren
                                listAdapter.notifyDataSetChanged();
                            }

                    }
                }
        });

        itemTouchHelper.attachToRecyclerView(recyclerView);
        recyclerView.setAdapter(listAdapter);
        // button to add (import) new templates
        FloatingActionButton addRunningPlanButton = view.findViewById(R.id.button_add_running_plan);
        addRunningPlanButton.setOnClickListener(v -> {
            AddRunningPlanFragment addRunningPlanFragment = null;
            List<Fragment> fragments = getParentFragmentManager().getFragments();
            for (Fragment fragment : fragments) {
                if (fragment instanceof AddRunningPlanFragment) {
                    addRunningPlanFragment = (AddRunningPlanFragment) fragment;
                    break;
                }
            }
            if (addRunningPlanFragment == null) {
                // create a new fragment
                addRunningPlanFragment = new AddRunningPlanFragment();
            }
            // starts the RunningPlanAddFragment
            showFragment(addRunningPlanFragment);
        });
        return view;
    }

    @Override
    public void onSaveInstanceState(@NonNull @NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // save the selected running plan (list index)
        outState.putInt("lastSelectedIndex", lastSelectedIndex);
    }

    @Override
    public void onClick(View v) {
        int itemPosition = recyclerView.getChildLayoutPosition(v);
        showDetailsForRunningPlanAtIndex(itemPosition);
    }

    private void showDetailsForRunningPlanAtIndex(int index) {
        if (runningPlans.size() > index) {
            lastSelectedIndex = index;
            RunningPlan runningPlan = runningPlans.get(index);
            String uuid = runningPlan.getUUID();
            RunningPlanDetailsFragment detailsFragment = null;
            List<Fragment> fragments = getParentFragmentManager().getFragments();
            for (Fragment fragment : fragments) {
                if (fragment instanceof RunningPlanDetailsFragment) {
                    detailsFragment = (RunningPlanDetailsFragment) fragment;
                    break;
                }
            }
            if (detailsFragment == null || detailsFragment.getUUID().equalsIgnoreCase(uuid)) {
                // create a new fragment
                detailsFragment = RunningPlanDetailsFragment.newInstance(uuid);
            }
            // starts the RunningPlanDetailsFragment
            showFragment(detailsFragment);
        }
    }

    private void showFragment(Fragment fragment) {
        // hides the RunningPlanListFragment
        FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
        fragmentTransaction.hide(this);
        // starts the RunningPlanDetailsFragment
        fragmentTransaction.replace(R.id.fragment_running_plan_container, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    // refresh the cached list of running plans
    private void onListChanged(List<RunningPlan> changedList) {
        runningPlans = changedList;
    }
}