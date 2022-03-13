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

import com.cafeyvinowinebar.Administrador.Adapters.AdapterVinos;
import com.cafeyvinowinebar.Administrador.POJOs.MenuCategory;
import com.cafeyvinowinebar.Administrador.ProductsViewModel;
import com.cafeyvinowinebar.Administrador.R;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

/**
 * Displays the wines menu
 */
public class Vinos extends DialogFragment {

    private final FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    private AdapterVinos adapter;
    public ProductsViewModel viewModel;

    public Vinos(ProductsViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_vinos, container, false);

        RecyclerView recVinos = view.findViewById(R.id.recVinos);
        recVinos.setLayoutManager(new LinearLayoutManager(getContext()));

        Query query = fStore.collection("menu/03.vinos/vinos");
        FirestoreRecyclerOptions<MenuCategory> options = new FirestoreRecyclerOptions.Builder<MenuCategory>()
                .setQuery(query, MenuCategory.class)
                .build();

        adapter = new AdapterVinos(options, getContext(), getActivity(), viewModel);

        recVinos.setAdapter(adapter);

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
