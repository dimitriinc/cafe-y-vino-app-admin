package com.cafeyvinowinebar.Administrador.Interfaces;

import com.google.firebase.firestore.DocumentSnapshot;

public interface OnSwitchClickListener {
    void onSwitchClick(DocumentSnapshot snapshot, boolean isChecked);
}
