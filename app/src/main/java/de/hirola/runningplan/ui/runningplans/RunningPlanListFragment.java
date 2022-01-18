package de.hirola.runningplan.ui.runningplans;

import android.widget.ListView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import de.hirola.runningplan.R;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.ListFragment;
import de.hirola.runningplan.model.MutableListLiveData;
import de.hirola.runningplan.model.RunningPlanViewModel;
import de.hirola.sportslibrary.Global;
import de.hirola.sportslibrary.SportsLibraryException;
import de.hirola.sportslibrary.model.RunningPlan;
import de.hirola.sportslibrary.ui.ModalOptionDialog;
import de.hirola.sportslibrary.ui.ModalOptionDialogListener;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * List-Fragment für die Darstellung aller vorhandener Laufpläne (Überblick).
 *

 * @author Michael Schmidt (Hirola)
 * @since 1.1.1
 */
public class RunningPlanListFragment extends Fragment {

    // view model
    private RunningPlanViewModel viewModel;
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
        // visualize the list of running plans
        RunningPlanRecyclerView listAdapter = new RunningPlanRecyclerView(requireContext(), runningPlans);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView_running_plans);
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
                // warning before remove
                ModalOptionDialog.showOptionDialog(requireContext(),
                        null,
                        getString(R.string.ask_before_remove_running_plan),
                        null,
                        null,
                        new ModalOptionDialogListener() {
                            @Override
                            public void onButtonClicked(int button) {
                                if (button == ModalOptionDialog.Button.OK) {
                                    // get the running plan
                                    int position = viewHolder.getBindingAdapterPosition();
                                    if (runningPlans.size() > position) {
                                        RunningPlan runningPlan = runningPlans.get(position);
                                        // remove running plan
                                        try {
                                            viewModel.remove(runningPlan);
                                            runningPlans.remove(position);
                                        } catch (SportsLibraryException exception) {
                                            ModalOptionDialog.showMessageDialog(
                                                    ModalOptionDialog.DialogStyle.CRITICAL,
                                                    requireContext(),
                                                    null, getString(R.string.remove_active_runningplan),
                                                    null);
                                            if (Global.DEBUG) {
                                                //TODO: Logging
                                            }
                                        }
                                    }
                                }
                                listAdapter.notifyDataSetChanged();
                            }
                        });
            }
        });

        itemTouchHelper.attachToRecyclerView(recyclerView);
        recyclerView.setAdapter(listAdapter);
        // button to add (import) new templates
        FloatingActionButton addRunningPlanButton = view.findViewById(R.id.button_add_runningplan);
        addRunningPlanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
            }
        });
        return view;
    }

    @Override
    public void onSaveInstanceState(@NonNull @NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // save the selected running plan (list index)
        outState.putInt("lastSelectedIndex", lastSelectedIndex);
    }

    public void onListItemClick(@NonNull ListView l, @NonNull View v, int position, long id) {
        showDetailsForRunningPlanAtIndex(position);
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