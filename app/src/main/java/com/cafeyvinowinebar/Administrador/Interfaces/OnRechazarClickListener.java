package com.cafeyvinowinebar.Administrador.Interfaces;

import com.google.firebase.firestore.DocumentSnapshot;

public interface OnRechazarClickListener {
    void onRechazarClick(DocumentSnapshot snapshot, int position);
}
