package de.hirola.runningplan.ui.info.menu;

import androidx.fragment.app.Fragment;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * A menu item with a string for resources and a fragment to view
 * content of the menu item.
 *
 * @author Michael Schmidt (Hirola)
 * @since 0.1
 */
public class MenuItem {

    private final int imageResourceId;
    private final int textResourceId;
    private final Fragment contentFragment;

    public MenuItem(int imageResourceId, int textResourceId, Fragment contentFragment) {
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

    public Fragment getContentFragment() {
        return contentFragment;
    }

}
