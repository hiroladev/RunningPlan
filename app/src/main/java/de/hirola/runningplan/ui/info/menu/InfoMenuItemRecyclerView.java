package de.hirola.runningplan.ui.info.menu;

import android.content.Context;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import de.hirola.runningplan.R;
import de.hirola.runningplan.RunningPlanApplication;
import de.hirola.sportslibrary.SportsLibrary;
import org.tinylog.Logger;

import java.util.*;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * Custom adapter for an info menu item to view in list.
 *
 * @author Michael Schmidt (Hirola)
 * @since 0.1
 */
public class InfoMenuItemRecyclerView extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final SportsLibrary sportsLibrary;
    private View.OnClickListener onClickListener;
    private final Map<Integer, MenuItem> menuItemMap;

    public InfoMenuItemRecyclerView(@NonNull Context context, @NonNull Map<Integer, MenuItem> menuItemMap) {
        this.menuItemMap = menuItemMap;
        sportsLibrary = ((RunningPlanApplication) context.getApplicationContext()).getSportsLibrary();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.menu_item_row, parent, false);
        v.setOnClickListener(onClickListener);
        return new InfoMenuItemRecyclerViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof InfoMenuItemRecyclerViewHolder) {
            InfoMenuItemRecyclerViewHolder viewHolder = (InfoMenuItemRecyclerViewHolder) holder;
            // view a menu item with an image and text
            // on click is can open a fragment fror the menu
            MenuItem menuItem = menuItemMap.get(position);
            if (menuItem != null) {
                try {
                    // load menu image resource
                    viewHolder.menuItemImageView.setImageResource(menuItem.getImageResourceId());
                    // menu item text
                    // if fragment is null, the text view should contain a link
                    if (menuItem.getContentFragment() != null) {
                        viewHolder.menuItemTextView.setText(menuItem.getMenuItemText());
                    } else {
                        viewHolder.menuItemTextView.setClickable(true);
                        viewHolder.menuItemTextView.setText(menuItem.getMenuItemLinkText());
                        viewHolder.menuItemTextView.setMovementMethod(LinkMovementMethod.getInstance());
                    }
                } catch (Exception exception) {
                    if (sportsLibrary.isDebugMode()) {
                        Logger.debug(exception);
                    }
                }
            }

        }
    }

    @Override
    public int getItemCount() {
        return menuItemMap.size();
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }
}
