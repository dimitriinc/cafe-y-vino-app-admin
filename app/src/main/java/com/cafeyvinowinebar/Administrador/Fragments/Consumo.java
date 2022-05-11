package com.cafeyvinowinebar.Administrador.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cafeyvinowinebar.Administrador.Adapters.AdapterConsumo;
import com.cafeyvinowinebar.Administrador.App;
import com.cafeyvinowinebar.Administrador.POJOs.CuentaItem;
import com.cafeyvinowinebar.Administrador.R;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays a RecyclerView of all the served products, ordered by the amount of items of each product served
 * Can be a total Consumo (called from the MainMenuActivity): all the products served
 * Or can be a personal Consumo (called from the HistoryActivity): the products consumed by a specific customer
 * Which collection to display tells us the query object passed as an argument in the constructor
 */
public class Consumo extends DialogFragment {

    private static final String TAG = "Consumo";

    private final Query query;
//    private DocumentSnapshot firstVisible, previousFirstVisible;
    private AdapterConsumo adapter;

    public Consumo(Query query) {
        this.query = query;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_consumo, container);

        FirestoreRecyclerOptions<CuentaItem> options = new FirestoreRecyclerOptions.Builder<CuentaItem>()
                .setQuery(query, CuentaItem.class)
                .build();
        adapter = new AdapterConsumo(options);
        //    private ImageView imgNext, imgPrevious;
        RecyclerView recConsumo = view.findViewById(R.id.recConsumo);
//        imgNext = view.findViewById(R.id.imgConsumoNext);
//        imgPrevious = view.findViewById(R.id.imgConsumoPrevioius);
        recConsumo.setLayoutManager(new LinearLayoutManager(requireActivity()));
//        recConsumo.setHasFixedSize(true);
        recConsumo.setAdapter(adapter);
//        loadItems();
//        imgNext.setOnClickListener(v -> loadItems(firstVisible));
//        imgPrevious.setOnClickListener(v -> loadItems(previousFirstVisible));
        return view;
    }

//    private void loadItems() {
////        if (docToStartAt == null) {
////            query.limit(5);
////
////        } else {
////            query.startAfter(docToStartAt);
////            previousFirstVisible = docToStartAt;
////            imgPrevious.setVisibility(View.VISIBLE);
////
////        }
//        query.get().addOnSuccessListener(App.executor, queryDocumentSnapshots -> {
//            List<CuentaItem> list = new ArrayList<>();
//            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
//                list.add(doc.toObject(CuentaItem.class));
//            }
//            adapter.submitList(list);
////            if (queryDocumentSnapshots.size() < 5) {
////                imgNext.setVisibility(View.GONE);
////            } else {
////                firstVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);
////                imgNext.setVisibility(View.VISIBLE);
////            }
//        });
//
//    }


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
