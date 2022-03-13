package com.cafeyvinowinebar.Administrador.Interfaces;

import com.google.firebase.firestore.DocumentSnapshot;

public interface OnAceptarCllickListener {
    void onAceptarClick(DocumentSnapshot snapshot, int position);
}
