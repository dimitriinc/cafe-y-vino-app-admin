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
import com.cafeyvinowinebar.Administrador.App;
import com.cafeyvinowinebar.Administrador.POJOs.RedactEntity;
import com.cafeyvinowinebar.Administrador.R;
import com.cafeyvinowinebar.Administrador.RedactViewModel;
import com.cafeyvinowinebar.Administrador.Runnables.RedactionApplier;
import com.cafeyvinowinebar.Administrador.Utils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.List;

/**
 * A terminal to make changes in the pedido
 * Admin can increment or decrement the count value of products
 * After the changes are made, admin can add a comment to these changes
 * Then on a background thread:
 * We compare the count values of the input collection and the output collection, update the pedido collection in the Firestore
 * And store a change object with particular changes in counts of products, and the comment
 */
public class Redact extends DialogFragment {

    private RecyclerView recRedact;
    private EditText edtRedactComment;
    private final List<DocumentSnapshot> products;  // the input collection
    public RedactViewModel viewModel;
    public String userName, mesa, mode;

    public Redact(List<DocumentSnapshot> products, String userName, String mesa, String mode) {
        this.products = products;
        this.userName = userName;
        this.mesa = mesa;
        this.mode = mode;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        // initialize the view model and populate the RedactDB
        // basically create a copy of the input collection, to perform modifications on it and thus create an output collection
        viewModel = new ViewModelProvider(this).get(RedactViewModel.class);

        // the pedido product is slightly different from the cuenta product, so we populate the Room DB in slightly different manner
        if (mode.equals(Utils.PEDIDOS)) {
            for (DocumentSnapshot snapshot : products) {
                viewModel.insert(new RedactEntity(
                        snapshot.getString(Utils.KEY_NAME),
                        snapshot.getString(Utils.KEY_CATEGORY),
                        snapshot.getLong(Utils.PRICE),
                        snapshot.getLong(Utils.KEY_COUNT)
                ));
            }
        } else {
            for (DocumentSnapshot snapshot : products) {
                viewModel.insert(new RedactEntity(
                        snapshot.getString(Utils.KEY_NAME),
                        snapshot.getLong(Utils.PRICE),
                        snapshot.getLong(Utils.KEY_COUNT),
                        snapshot.getLong(Utils.TOTAL)
                ));
            }
        }

        return super.onCreateDialog(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_redact, container);

        recRedact = v.findViewById(R.id.recRedact);
        edtRedactComment = v.findViewById(R.id.edtRedactComment);
        FloatingActionButton fabRedactStore = v.findViewById(R.id.fabRedactStore);

        setupAdapter();

        fabRedactStore.setOnClickListener(view -> {

            // admin has made the changes, we are ready to synchronize the pedido and store the changes
            // first of all, we store the comment, if any
            String comment = edtRedactComment.getText().toString().trim();
            if (comment.isEmpty()) {
                comment = "sin comentario";
            }

            // on a backgroung thread we perform the redaction itself and store the changes in the Firestore
            App.executor.submit(new RedactionApplier(comment, userName, mesa, viewModel, products, mode));

            dismiss();

        });

        return v;
    }

    private void setupAdapter() {

        recRedact.setLayoutManager(new LinearLayoutManager(getContext()));
        final AdapterRedact adapter = new AdapterRedact(viewModel);
        recRedact.setAdapter(adapter);
        viewModel.getAllProducts().observe(this, adapter::submitList);

    }
}
