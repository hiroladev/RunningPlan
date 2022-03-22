package de.hirola.runningplan.ui.info;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hirola.runningplan.R;
import de.hirola.runningplan.ui.info.log.InfoLogsFragment;
import de.hirola.runningplan.ui.info.menu.InfoMenuItemRecyclerView;
import de.hirola.runningplan.ui.info.menu.MenuItem;
import de.hirola.runningplan.ui.info.tracks.InfoTracksFragment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * The content fragment for infos.
 *
 * @author Michael Schmidt (Hirola)
 * @since 0.1
 */
public class InfoContentFragment extends Fragment implements View.OnClickListener {

    private RecyclerView recyclerView; // recycler view list adapter
    private Map<Integer, MenuItem> menuItemMap; // menu items

    // Required empty public constructor
    public InfoContentFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View infoView = inflater.inflate(R.layout.fragment_info_content, container, false);
        // build the menu item map
        menuItemMap = new HashMap<>(5);
        menuItemMap.putIfAbsent(0, new MenuItem(R.drawable.baseline_info_black_36, R.string.menu_item_info, new InfoAboutFragment()));
        menuItemMap.putIfAbsent(1, new MenuItem(R.drawable.baseline_list_black_36, R.string.menu_item_license, new InfoLicenseFragment()));
        menuItemMap.putIfAbsent(2, new MenuItem(R.drawable.baseline_help_black_36, R.string.menu_item_help, new InfoHelpFragment()));
        menuItemMap.putIfAbsent(3, new MenuItem(R.drawable.baseline_route_black_24, R.string.menu_item_track, new InfoTracksFragment()));
        menuItemMap.putIfAbsent(4, new MenuItem(R.drawable.outline_adb_black_36, R.string.menu_item_logs, new InfoLogsFragment()));
        InfoMenuItemRecyclerView listAdapter = new InfoMenuItemRecyclerView(requireContext(), menuItemMap);
        // menu fragments on click
        listAdapter.setOnClickListener(this);
        recyclerView = infoView.findViewById(R.id.fgmt_info_content_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(listAdapter);
        return infoView;
    }

    @Override
    public void onClick(View v) {
        int itemPosition = recyclerView.getChildLayoutPosition(v);
        showFragmentForMenuItemAtIndex(itemPosition);
    }

    private void showFragmentForMenuItemAtIndex(int index) {
        if (menuItemMap.size() > index) {
            MenuItem menuItem = menuItemMap.get(index);
            if (menuItem != null) {
                Fragment menuItemFragment = menuItem.getContentFragment();
                if (menuItemFragment != null) {
                    List<Fragment> fragments = getParentFragmentManager().getFragments();
                    for (Fragment fragment : fragments) {
                        if (fragment.getClass().equals(menuItemFragment.getClass())) {
                            menuItemFragment = fragment;
                            break;
                        }
                    }
                    // starts the RunningPlanDetailsFragment
                    showFragment(menuItemFragment);
                }
            }
        }
    }

    private void showFragment(Fragment fragment) {
        // hides the info fragment
        FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
        fragmentTransaction.hide(this);
        // starts the menu item fragment
        fragmentTransaction.replace(R.id.fragment_info_container, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
}