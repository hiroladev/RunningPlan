package de.hirola.runningplan.util;

import android.app.AlertDialog;
import android.content.Context;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * A dialog for user messages, alerts and to get decisions.
 *
 * @author Michael Schmidt (Hirola)
 * @since 0.1
 */
public final class ModalOptionDialog {

    public static final class DialogStyle {
        public static final int INFORMATION = 0;
        public static final int WARNING = 1;
        public static final int CRITICAL = 2;

    }

    public static final class ButtonStyle {
        public static int OK_CANCEL_OPTION = 0;
        public static int YES_NO_OPTION = 1;
    }

    public static final class Button {
        public static final int OK = 0;
        public static final int CANCEL = 1;
        public static int YES = 2;
        public static int NO = 3;
        public static final int OPTION_1 = 4;
        public static final int OPTION_2 = 5;
        public static final int OPTION_3 = 6;
    }

    public static void showMessageDialog(int dialogStyle, @Nullable Context context,
                                         @Nullable String title, @NotNull String message,
                                         @Nullable String buttonText) {

        boolean isRunningOnAndroid = context != null;
        AlertDialog alert = null;
        if (isRunningOnAndroid) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context)
                    .setCancelable(false);
            alert = builder.create();
            AlertDialog finalAlert = alert;
            if (title != null) {
                alert.setTitle(title);
            }
            alert.setMessage(message);
            if (buttonText == null) {
                buttonText = "OK";
            }
            alert.setButton(AlertDialog.BUTTON_NEUTRAL,
                    buttonText,
                    (dialogInterface, i) -> finalAlert.dismiss());
        } else {
            //TODO: jvm
        }
        switch (dialogStyle) {
            case 0:
                if (isRunningOnAndroid) {
                    alert.setIcon(android.R.drawable.ic_dialog_info);
                    alert.show();
                } else {
                    //TODO: jvm
                }
                break;
            case 1:  case 2:
                if (isRunningOnAndroid) {
                    alert.setIcon(android.R.drawable.ic_dialog_alert);
                    alert.show();
                } else {
                    //TODO: jvm
                }
                break;
        }
    }

    public static void showYesNoDialog(@Nullable Context context,
                                       @Nullable String title, @NotNull String message,
                                       @Nullable String positiveButtonText, @Nullable String negativeButtonText,
                                       @NotNull ModalOptionDialogListener buttonClickListener) {

        boolean isRunningOnAndroid = context != null;
        AlertDialog alert;
        if (isRunningOnAndroid) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context)
                    .setCancelable(false)
                    .setIcon(android.R.drawable.ic_dialog_info);
            alert = builder.create();
            if (title != null) {
                alert.setTitle(title);
            }
            alert.setMessage(message);
            if (positiveButtonText == null) {
                positiveButtonText = "OK";
            }
            alert.setButton(AlertDialog.BUTTON_POSITIVE,
                    positiveButtonText,
                    (dialogInterface, i) -> buttonClickListener.onButtonClicked(Button.OK));
            if (negativeButtonText == null) {
                negativeButtonText = "Cancel";
            }
            alert.setButton(AlertDialog.BUTTON_NEGATIVE,
                    negativeButtonText,
                    (dialogInterface, i) -> buttonClickListener.onButtonClicked(Button.CANCEL));
            alert.show();
        } else {
            //TODO: jvm
        }
    }

    public static void showThreeOptionsDialog(@Nullable Context context,
                                              @Nullable String title, @NotNull String message,
                                              @NotNull String optionOneButtonText,
                                              @NotNull String optionTwoButtonText,
                                              @Nullable String optionThreeButtonText,
                                              @NotNull ModalOptionDialogListener buttonClickListener) {

        boolean isRunningOnAndroid = context != null;
        AlertDialog alert;
        if (isRunningOnAndroid) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context)
                    .setCancelable(false)
                    .setIcon(android.R.drawable.ic_dialog_info);
            alert = builder.create();
            if (title != null) {
                alert.setTitle(title);
            }
            alert.setMessage(message);
            alert.setButton(AlertDialog.BUTTON_POSITIVE,
                    optionOneButtonText,
                    (dialogInterface, i) -> buttonClickListener.onButtonClicked(Button.OPTION_1));
            alert.setButton(AlertDialog.BUTTON_NEGATIVE,
                    optionTwoButtonText,
                    (dialogInterface, i) -> buttonClickListener.onButtonClicked(Button.OPTION_2));
            if (optionThreeButtonText != null) {
                alert.setButton(AlertDialog.BUTTON_NEUTRAL,
                        optionThreeButtonText,
                        (dialogInterface, i) -> buttonClickListener.onButtonClicked(Button.OPTION_3));
            }
            alert.show();
        } else {
            //TODO: jvm
        }
    }

    public interface ModalOptionDialogListener {
        void onButtonClicked(int button);
    }
}
