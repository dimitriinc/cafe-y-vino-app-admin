package com.cafeyvinowinebar.Administrador.Fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cafeyvinowinebar.Administrador.Adapters.AdapterMesas;
import com.cafeyvinowinebar.Administrador.App;
import com.cafeyvinowinebar.Administrador.MesasViewModel;
import com.cafeyvinowinebar.Administrador.NewPedidoActivity;
import com.cafeyvinowinebar.Administrador.POJOs.Mesa;
import com.cafeyvinowinebar.Administrador.POJOs.MesaEntity;
import com.cafeyvinowinebar.Administrador.R;
import com.cafeyvinowinebar.Administrador.Utils;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

/**
 * Displays the list of tables as a recycler view
 * The purpose is to assign a table value to the new custom order
 * The list of tables is stored in the SQLiteDB, in the 'mesas' table
 * The list start with the set of fixed tables
 * But admin can add custom ones if needed by presing the FAB
 * The custom tables get deleted when the bill assigned to it is canceled
 */
public class MesaPicker extends DialogFragment {

    private final FirebaseFirestore fStore = FirebaseFirestore.getInstance();

    private AdapterMesas adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_mesa_picker, container, false);

        RecyclerView recMesas = view.findViewById(R.id.recMesas);
        FloatingActionButton fabAddMesa = view.findViewById(R.id.fabAddMesa);

        fabAddMesa.setOnClickListener(view1 -> {

            // we build an alert dialog for admin to create a custom table
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            View viewDialog = getLayoutInflater().inflate(R.layout.dialog_custom_mesa, container);
            EditText edtNewMesa = viewDialog.findViewById(R.id.edtNewMesa);
            FloatingActionButton fabOkNewMesa = viewDialog.findViewById(R.id.fabOkNewMesa);
            builder.setView(viewDialog);
            AlertDialog dialog = builder.create();

            fabOkNewMesa.setOnClickListener(view11 -> {

                // get the name of the new table
                String newMesaName = edtNewMesa.getText().toString().trim();
                if (newMesaName.isEmpty()) {
                    Toast.makeText(getContext(), R.string.llenar_los_campos, Toast.LENGTH_SHORT).show();
                } else {

                    // add a new mesa to the Firestore collection
                    // when it's added, we can go construct a new pedido
                    // we wait for success of the write to know the id of the new mesa
                    fStore.collection("mesas").add(new Mesa(false, false, false, newMesaName))
                            .addOnSuccessListener(App.executor, documentReference -> {

                                // go to the NewPedidoActivity with the name of the new table, and its id (to change the presence status of the mesa later)
                                startActivity(NewPedidoActivity.newIntent(getContext(), newMesaName, documentReference.getId()));

                                // dismiss the alert dialog and the dialog fragment
                                dialog.dismiss();
                                dismiss();
                            });
                }
            });
            dialog.show();
        });

        Query query = fStore.collection("mesas")
                .orderBy(Utils.KEY_NAME);
        FirestoreRecyclerOptions<Mesa> options = new FirestoreRecyclerOptions.Builder<Mesa>()
                .setQuery(query, Mesa.class)
                .build();

        adapter = new AdapterMesas(options, getContext());
        recMesas.setLayoutManager(new GridLayoutManager(getContext(), 3));
        recMesas.setAdapter(adapter);


        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onPause() {
        super.onPause();
        dismiss();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}
