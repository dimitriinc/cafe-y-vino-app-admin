package com.cafeyvinowinebar.Administrador;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.cafeyvinowinebar.Administrador.Adapters.AdapterPedidos;
import com.cafeyvinowinebar.Administrador.Fragments.Redact;
import com.cafeyvinowinebar.Administrador.POJOs.Pedido;
import com.cafeyvinowinebar.Administrador.Runnables.EliminationApplier;
import com.cafeyvinowinebar.Administrador.Runnables.NewItemPedidoAdder;
import com.cafeyvinowinebar.Administrador.Runnables.CollectionDeleter;
import com.cafeyvinowinebar.Administrador.Runnables.PedidoOrCuentaForceDeleter;
import com.cafeyvinowinebar.Administrador.Runnables.UniquePedidoDeleter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

/**
 * Displays the pending orders of the current date
 * The way it displays depends on the string 'mode' that arrives with the intent
 * It may display orders with only kitchen products in them, only bar products in them, or all the products in them
 * All the modes share the same adapter and the same collection to query
 * The difference is the filters we put on the query
 * And the implementation of the on click listeners
 */
public class PedidosTodoActivity extends AppCompatActivity {

    private final FirebaseFirestore fStore = FirebaseFirestore.getInstance();

    private RecyclerView upRecView;
    private AdapterPedidos adapter;
    private String currentDate, mode;
    private CollectionReference collection;
    private Query query;

    public static Intent newIntent(Context context, String mode) {
        Intent i = new Intent(context, PedidosTodoActivity.class);
        i.putExtra(Utils.KEY_MODE, mode);
        return i;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pedidos_todo);

        init();

        setupAdapter();

    }

    private void setupAdapter() {

        // we decide how to filter the query based on the 'mode' value
        switch (mode) {
            case Utils.TODO:
                query = collection
                        .orderBy(Utils.TIMESTAMP, Query.Direction.ASCENDING);
                break;
            case Utils.BARRA:
                query = collection
                        .whereEqualTo(Utils.SERVIDO_BARRA, false)
                        .orderBy(Utils.TIMESTAMP, Query.Direction.ASCENDING);

                break;
            case Utils.COCINA:
                query = collection
                        .whereEqualTo(Utils.SERVIDO_COCINA, false)
                        .orderBy(Utils.TIMESTAMP, Query.Direction.ASCENDING);
                break;

        }

        FirestoreRecyclerOptions<Pedido> options = new FirestoreRecyclerOptions.Builder<Pedido>()
                .setQuery(query, Pedido.class)
                .build();

        adapter = new AdapterPedidos(options, getBaseContext(), currentDate, mode);
        upRecView.setHasFixedSize(true);
        upRecView.setAdapter(adapter);
        upRecView.setLayoutManager(new LinearLayoutManager(getBaseContext()));

        // when the order is served on the table, admin swipes the item, and all the products are moved from the pedido collection
        // to the cuenta collection
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                adapter.moveToCuenta(viewHolder.getBindingAdapterPosition());

            }
        }).attachToRecyclerView(upRecView);

        // the click listener is set on the 'add custom item' button, which appears in the expanded version of the list item
        // it varies slightly on the 'mode' value
        adapter.setOnAddClickListener((snapshot, position, view) -> showAddProductDialog(mode, snapshot));

        // we also listen to the 'redact order' button, which opens a dialog fragment for the redaction
        // we pass a list of products (doc snapshots) to the fragment
        adapter.setOnRedactClickListener((snapshot, position, view) -> {
            fStore.collection(snapshot.getReference().getPath() + "/pedido").get()
                    .addOnSuccessListener(App.executor, queryDocumentSnapshots -> new Redact(
                            queryDocumentSnapshots.getDocuments(),
                            snapshot.getString(Utils.KEY_USER),
                            snapshot.getString(Utils.KEY_MESA),
                            Utils.PEDIDOS
                    ).show(getSupportFragmentManager(), "redaction"));
        });

        // the long click listener is set only in the 'everything' mode, it deletes the order completely
        if (mode.equals(Utils.TODO)) {
            adapter.setOnItemLongClickListener((snapshot, position, v) -> {

                // on long click we build an alert dialog before deleting the whole order
                // dialog prompts admin to type in a reason for deletion
                // then on a background thread we construct a redaction and store it into the Firestore, then delete the order
                View eliminarView = getLayoutInflater().inflate(R.layout.dialog_eliminacion, null);
                EditText edtEliminar = eliminarView.findViewById(R.id.edtEliminar);
                FloatingActionButton fabEliminar = eliminarView.findViewById(R.id.fabEliminar);

                AlertDialog dialog = new AlertDialog.Builder(PedidosTodoActivity.this)
                        .setView(eliminarView)
                        .create();

                fabEliminar.setOnClickListener(view -> {

                    String comment = edtEliminar.getText().toString().trim();
                    if (comment.isEmpty()) {
                        comment = "sin comentario";
                    }

                    // handle the deletion on a background thread
                    App.executor.submit(new PedidoOrCuentaForceDeleter(snapshot, comment, Utils.PEDIDO));

                    dialog.dismiss();

                });

                dialog.show();
            });
        }
    }

    private void init() {

        mode = getIntent().getStringExtra(Utils.KEY_MODE);

        FloatingActionButton fabTodoHome = findViewById(R.id.fabTodoHome);
        fabTodoHome.setOnClickListener(v -> startActivity(new Intent(getBaseContext(), MainActivity.class)));

        currentDate = Utils.getCurrentDate();
        upRecView = findViewById(R.id.upRecView);

        collection = fStore.collection(Utils.PEDIDOS)
                .document(currentDate)
                .collection("pedidos enviados");

    }


    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    /**
     * Shows a dialog to create a new custom product within the order
     */
    private void showAddProductDialog(String mode, DocumentSnapshot documentSnapshot) {

        AlertDialog.Builder builder = new AlertDialog.Builder(PedidosTodoActivity.this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_new_item, null);
        EditText nombreEt = dialogView.findViewById(R.id.etNewItemCuentaNombre);
        EditText precioEt = dialogView.findViewById(R.id.etNewItemCuentaPrecio);
        EditText countEt = dialogView.findViewById(R.id.etNewItemCuentaCantidad);
        builder.setView(dialogView);

        if (mode.equals(Utils.TODO)) {

            // admin can choose to what category the custom item belongs
            // on positive button it will be stored as a kitchen product
            builder.setPositiveButton(R.string.btn_cocina, (dialog, which) -> storeCustomProduct(
                    nombreEt, precioEt, countEt, documentSnapshot, Utils.COCINA
            ));

            // on negative button it will be stored as a bar product
            builder.setNegativeButton(R.string.btn_barra, (dialog, which) -> storeCustomProduct(
                    nombreEt, precioEt, countEt, documentSnapshot, Utils.BARRA
            ));

        } else {

            // otherwise, the product will be stored with the category corresponding to the 'mode' value
            builder.setPositiveButton(R.string.agregar, (dialog, which) -> storeCustomProduct(
                    nombreEt, precioEt, countEt, documentSnapshot, mode
            ));
        }

        builder.create().show();
    }

    /**
     * Handles the buttons of the createCustomProduct dialog
     */
    private void storeCustomProduct(EditText nombreEt, EditText precioEt, EditText countEt, DocumentSnapshot snapshot, String mode) {

        String name = nombreEt.getText().toString().trim();
        String priceString = precioEt.getText().toString().trim();
        String countString = countEt.getText().toString().trim();
        if (name.isEmpty() || priceString.isEmpty() || countString.isEmpty()) {
            Toast.makeText(PedidosTodoActivity.this, R.string.llenar_los_campos, Toast.LENGTH_SHORT).show();
            return;
        }
        long price = Long.parseLong(priceString);
        long count = Long.parseLong(countString);
        String path = snapshot.getReference().getPath();
        App.executor.submit(new NewItemPedidoAdder(name, path, price, mode, count));

    }

}