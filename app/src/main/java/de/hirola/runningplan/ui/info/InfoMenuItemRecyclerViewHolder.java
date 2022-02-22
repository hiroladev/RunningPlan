package de.hirola.runningplan.ui.info;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import de.hirola.runningplan.R;
import org.jetbrains.annotations.NotNull;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * Custom adapter for an info menu item to view in list.
 *
 * @author Michael Schmidt (Hirola)
 * @since 1.1.1
 */
public class InfoMenuItemRecyclerViewHolder extends RecyclerView.ViewHolder {

    ImageView menuItemImageView;
    TextView menuItemTextView;

    public InfoMenuItemRecyclerViewHolder(@NonNull @NotNull View itemView) {
        super(itemView);
        menuItemImageView = itemView.findViewById(R.id.menu_item_imageview);
        menuItemTextView = itemView.findViewById(R.id.menu_item_textview);
    }
}
