package com.cafeyvinowinebar.Administrador.Interfaces;

import android.view.View;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.concurrent.ExecutionException;

public interface OnItemClickListener {
    void onItemClick(DocumentSnapshot snapshot, int position, View view);
}
