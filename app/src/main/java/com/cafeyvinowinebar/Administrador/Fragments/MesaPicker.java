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
import com.cafeyvinowinebar.Administrador.MesasViewModel;
import com.cafeyvinowinebar.Administrador.NewPedidoActivity;
import com.cafeyvinowinebar.Administrador.POJOs.MesaEntity;
import com.cafeyvinowinebar.Administrador.R;
import com.cafeyvinowinebar.Administrador.Utils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * Displays the list of tables as a recycler view
 * The purpose is to assign a table value to the new custom order
 * The list of tables is stored in the SQLiteDB, in the 'mesas' table
 * The list start with the set of fixed tables
 * But admin can add custom ones if needed by presing the FAB
 * The custom tables get deleted when the bill assigned to it is canceled
 */
public class MesaPicker extends DialogFragment {

    private MesasViewModel mesasViewModel;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        // get the view model for the fragment
        mesasViewModel = new ViewModelProvider(this).get(MesasViewModel.class);
        return super.onCreateDialog(savedInstanceState);

    }

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
                String newMesa = edtNewMesa.getText().toString().trim();
                if (newMesa.isEmpty()) {
                    Toast.makeText(getContext(), R.string.llenar_los_campos, Toast.LENGTH_SHORT).show();
                } else {

                    // create a new mesa entity and insert it into the mesas table
                    MesaEntity mesa = new MesaEntity(newMesa, false);
                    int mesaId = mesa.getId();
                    mesasViewModel.insert(mesa);

                    // dismiss the alert dialog and the dialog fragment
                    dialog.dismiss();
                    dismiss();

                    // go to the NewPedidoActivity with the name of the new table
                    startActivity(NewPedidoActivity.newIntent(getContext(), newMesa));
                }
            });
            dialog.show();
        });

        recMesas.setLayoutManager(new GridLayoutManager(getContext(), 3));
        final AdapterMesas adapter = new AdapterMesas(getContext(), mesasViewModel);
        recMesas.setAdapter(adapter);

        // start observing the live data
        mesasViewModel.getMesas().observe(this, adapter::submitList);

        return view;
    }
}
