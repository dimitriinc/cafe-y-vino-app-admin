package com.cafeyvinowinebar.Administrador.Fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cafeyvinowinebar.Administrador.Adapters.AdapterRedact;
import com.cafeyvinowinebar.Administrador.POJOs.RedactEntity;
import com.cafeyvinowinebar.Administrador.R;
import com.cafeyvinowinebar.Administrador.RedactViewModel;
import com.cafeyvinowinebar.Administrador.Utils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Redact extends DialogFragment {

    private static final String TAG = "Redact";

    private RecyclerView recRedact;
    private EditText edtRedactComment;
    private FloatingActionButton fabRedactStore;
    private List<DocumentSnapshot> products;
    private Map<String, Long> inputCollection;
    private Map<String, Long> outputCollection;
    public RedactViewModel viewModel;

    public Redact(List<DocumentSnapshot> products) {
        this.products = products;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(RedactViewModel.class);
        return super.onCreateDialog(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_redact, container);

        recRedact = v.findViewById(R.id.recRedact);
        edtRedactComment = v.findViewById(R.id.edtRedactComment);
        fabRedactStore = v.findViewById(R.id.fabRedactStore);

        populateTheRoom();

        setupAdapter();

        fabRedactStore.setOnClickListener(view -> {
            outputCollection = new HashMap<>();
            String comment = edtRedactComment.getText().toString().trim();
            if (comment.isEmpty()) {
                comment = "sin comentario";
            }
            List<RedactEntity> productsFromRoom = viewModel.getProducts().getValue();
            for (RedactEntity product : productsFromRoom) {
                outputCollection.put(product.getName(), product.getCount());
            }
        });

        return v;
    }


    private void populateTheRoom() {

        inputCollection = new HashMap<>();
        for (DocumentSnapshot snapshot : products) {
            viewModel.insert(snapshot.toObject(RedactEntity.class));
            inputCollection.put(snapshot.getId(), snapshot.getLong(Utils.KEY_COUNT));
        }

    }

    private void setupAdapter() {
        recRedact.setLayoutManager(new LinearLayoutManager(getContext()));
        final AdapterRedact adapter = new AdapterRedact(viewModel);
        recRedact.setAdapter(adapter);
        viewModel.getProducts().observe(requireActivity(), adapter::submitList);
    }
}
