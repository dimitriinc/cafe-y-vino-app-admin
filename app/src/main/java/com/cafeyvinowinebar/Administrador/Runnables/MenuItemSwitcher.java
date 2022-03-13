package com.cafeyvinowinebar.Administrador.Runnables;

import com.cafeyvinowinebar.Administrador.Utils;
import com.google.firebase.firestore.DocumentSnapshot;

/**
 * Based on the position of the SwitchButton, changes the presence status of a product
 */
public class MenuItemSwitcher implements Runnable {

    private final DocumentSnapshot snapshot;
    private final boolean isChecked;

    public MenuItemSwitcher(DocumentSnapshot snapshot, boolean isChecked) {
        this.snapshot = snapshot;
        this.isChecked = isChecked;
    }

    @Override
    public void run() {
        snapshot.getReference().update(Utils.IS_PRESENT, isChecked);
    }
}
