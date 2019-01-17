package com.sannsyn.dca.vaadin.component.custom;

import com.vaadin.ui.UI;
import org.vaadin.dialogs.ConfirmDialog;

/**
 * Created by mashiur on 6/2/17.
 */
public class DCAConfirmDialog extends ConfirmDialog {
    public static ConfirmDialog show(final UI ui, final String message,
                                     final Listener listener) {
        ConfirmDialog confirmDialog = show(ui, null, message, null, null, listener);
        confirmDialog.getOkButton().setStyleName("btn-primary");
        confirmDialog.getCancelButton().setStyleName("btn-primary");
        confirmDialog.setCaption("");
        return confirmDialog;
    }
}
