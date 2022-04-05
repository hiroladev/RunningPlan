package de.hirola.runningplan.ui.info.menu;

import android.content.Context;
import android.text.Html;
import android.text.Spannable;
import android.text.TextPaint;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;
import androidx.annotation.NonNull;
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

    private final Context context;
    private final int imageResourceId;
    private final int textResourceId;
    private final String urlString;
    private final Fragment contentFragment;

    public MenuItem(@NonNull Context context, int imageResourceId, int textResourceId,
                    @Nullable String urlString, @Nullable Fragment contentFragment) {
        this.context = context;
        this.imageResourceId = imageResourceId;
        this.textResourceId = textResourceId;
        this.urlString = urlString;
        this.contentFragment = contentFragment;
    }

    public int getImageResourceId() {
        return imageResourceId;
    }

    public String getMenuItemText() {
        return context.getString(textResourceId);
    }

    /**
     * Get a menu item text with a link and without underline.
     *
     * @return A text for the menu item with a link
     */
    public Spannable getMenuItemLinkText() {
        // build the menu item text with given url
        String menuItemText = "<a href=\"" +
                urlString +
                "\">" +
                context.getString(textResourceId) +
                "</a>";
        Spannable spannableString = (Spannable) Html.fromHtml(menuItemText, Html.FROM_HTML_MODE_LEGACY);
        for (URLSpan u: spannableString.getSpans(0, spannableString.length(), URLSpan.class)) {
            spannableString.setSpan(new UnderlineSpan() {
                public void updateDrawState(TextPaint textPaint) {
                    textPaint.setUnderlineText(false);
                }
            }, spannableString.getSpanStart(u), spannableString.getSpanEnd(u), 0);
        }
        return spannableString;
    }

    @Nullable
    public Fragment getContentFragment() {
        return contentFragment;
    }

}
