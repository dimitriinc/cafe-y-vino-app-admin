package com.cafeyvinowinebar.Administrador.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cafeyvinowinebar.Administrador.Adapters.AdapterConsumo;
import com.cafeyvinowinebar.Administrador.POJOs.CuentaItem;
import com.cafeyvinowinebar.Administrador.R;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.Query;

/**
 * Displays a RecyclerView of all the served products, ordered by the amount of items of each product served
 * Can be a total Consumo (called from the MainMenuActivity): all the products served
 * Or can be a personal Consumo (called from the HistoryActivity): the products consumed by a specific customer
 * Which collection to display tells us the query object passed as an argument in the constructor
 */
public class Consumo extends DialogFragment {

    private AdapterConsumo adapter;
    private final Query query;

    public Consumo(Query query) {
        this.query = query;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_consumo, container);

        RecyclerView recConsumo = view.findViewById(R.id.recConsumo);

        FirestoreRecyclerOptions<CuentaItem> options = new FirestoreRecyclerOptions.Builder<CuentaItem>()
                .setQuery(query, CuentaItem.class)
                .build();
        adapter = new AdapterConsumo(options);
        recConsumo.setLayoutManager(new LinearLayoutManager(getContext()));
        recConsumo.setAdapter(adapter);


        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}
