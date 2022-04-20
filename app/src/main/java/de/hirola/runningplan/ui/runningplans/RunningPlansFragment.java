package de.hirola.runningplan.ui.runningplans;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import de.hirola.runningplan.R;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import de.hirola.runningplan.model.RunningPlanViewModel;
import de.hirola.sportsapplications.Global;
import de.hirola.sportsapplications.model.RunningPlan;
import de.hirola.runningplan.util.ModalOptionDialog;
import de.hirola.sportsapplications.model.UUID;
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
 * @since 0.1
 */
public class RunningPlansFragment extends Fragment implements View.OnClickListener {

    private RunningPlanViewModel viewModel;
    private RecyclerView recyclerView; // recycler view list adapter
    private int lastSelectedIndex; // last selected running plan (list index)
    private List<RunningPlan> runningPlans;  // cached list of running plans

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // get the last selected list index (running plan)
        if (savedInstanceState != null) {
            lastSelectedIndex = savedInstanceState.getInt("lastSelectedIndex");
        }
        // load the running plans
        // data
        viewModel = new RunningPlanViewModel(requireActivity().getApplication(), null);
        runningPlans = viewModel.getRunningPlans();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_running_plans, container, false);
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
        recyclerView = view.findViewById(R.id.fgmt_running_plans_recyclerview);
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
                                        getString(R.string.ask_before_delete_running_plan),
                                        null,
                                        null,
                                        button -> {
                                            if (button == ModalOptionDialog.Button.OK) {
                                                // get the running plan
                                                int position1 = viewHolder.getBindingAdapterPosition();
                                                RunningPlan runningPlan1 = runningPlans.get(position1);
                                                // remove running plan
                                                if (viewModel.deleteObject(runningPlan1)) {
                                                    listAdapter.notifyItemRemoved(position1);
                                                } else {
                                                    ModalOptionDialog.showMessageDialog(
                                                            ModalOptionDialog.DialogStyle.CRITICAL,
                                                            requireContext(),
                                                            null, getString(R.string.deleting_failed),
                                                            null);
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
        FloatingActionButton addRunningPlanButton = view.findViewById(R.id.fgmt_running_plans_add_running_plan_button);
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
        // add the selected running plan (list index)
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
            UUID runningPlanUUID = runningPlan.getUUID();
            RunningPlanDetailsFragment detailsFragment = null;
            List<Fragment> fragments = getParentFragmentManager().getFragments();
            for (Fragment fragment : fragments) {
                if (fragment instanceof RunningPlanDetailsFragment) {
                    detailsFragment = (RunningPlanDetailsFragment) fragment;
                    break;
                }
            }
            // if fragment or uuid null or a fragment for another running plan (uuid)
            // then create a new fragment
            if (detailsFragment == null || detailsFragment.getUUID() == null) {
                detailsFragment = RunningPlanDetailsFragment.newInstance(runningPlanUUID);
            } else if (!detailsFragment.getUUID().equals(runningPlanUUID)) {
                detailsFragment = RunningPlanDetailsFragment.newInstance(runningPlanUUID);
            }
            // starts the RunningPlanDetailsFragment
            showFragment(detailsFragment);
        }
    }

    private void showFragment(Fragment fragment) {
        // hides the RunningPlansFragment
        FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
        fragmentTransaction.hide(this);
        // starts the RunningPlanDetailsFragment
        fragmentTransaction.replace(R.id.fragment_running_plan_container, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

}