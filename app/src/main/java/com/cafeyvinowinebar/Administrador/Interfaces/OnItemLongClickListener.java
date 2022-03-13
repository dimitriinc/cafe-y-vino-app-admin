package com.cafeyvinowinebar.Administrador.Interfaces;

import android.view.View;

import com.google.firebase.firestore.DocumentSnapshot;

public interface OnItemLongClickListener {
    void onItemLongClick(DocumentSnapshot snapshot, int position, View v);
}
