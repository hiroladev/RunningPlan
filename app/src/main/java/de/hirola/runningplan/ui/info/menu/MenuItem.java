package de.hirola.runningplan.ui.info.menu;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * A menu item with a string for resources and a fragment to view
 * content of the menu item or only a link.
 * The fragment can be null, then the text to be displayed
 * should contain an Internet link.
 *
 * @author Michael Schmidt (Hirola)
 * @since 0.1
 */
public class MenuItem {

    private final int imageResourceId;
    private final int textResourceId;
    private final Fragment contentFragment;


    public MenuItem(int imageResourceId, int textResourceId, @Nullable Fragment contentFragment) {
        this.imageResourceId = imageResourceId;
        this.textResourceId = textResourceId;
        this.contentFragment = contentFragment;
    }

    public int getImageResourceId() {
        return imageResourceId;
    }

    public int getTextResourceId() {
        return textResourceId;
    }

    @Nullable
    public Fragment getContentFragment() {
        return contentFragment;
    }

}
