package com.cafeyvinowinebar.Administrador;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.cafeyvinowinebar.Administrador.Adapters.AdapterCuentas;
import com.cafeyvinowinebar.Administrador.Fragments.DatePicker;
import com.cafeyvinowinebar.Administrador.Fragments.Redact;
import com.cafeyvinowinebar.Administrador.POJOs.Cuenta;
import com.cafeyvinowinebar.Administrador.Runnables.CollectionDeleter;
import com.cafeyvinowinebar.Administrador.Runnables.EliminationApplier;
import com.cafeyvinowinebar.Administrador.Runnables.NewItemCuentaAdder;
import com.cafeyvinowinebar.Administrador.Runnables.UniquePedidoDeleter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Displays a list of not canceled bills for the current date on the main screen
 * On the sliding panel displays a date picker to pass a date value to the activity where admin can check canceled bills on the chosen date
 */
public class CuentasActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private final FirebaseFirestore fStore = FirebaseFirestore.getInstance();

    public String currentDate;
    private RecyclerView recCuentas;
    private AdapterCuentas adapter;
    private CollectionReference collection;
    private ImageView imgHoy, imgSlide, imgCuentaDate;
    private SlidingUpPanelLayout layout;
    public Handler handler;
    public FragmentManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cuentas);

        init();

        // handle the sliding panel
        layout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                imgSlide.setAlpha(1 - slideOffset);
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {

            }
        });
        if (getResources().getConfiguration().smallestScreenWidthDp == 600) {
            layout.setPanelHeight(45);
        } else {
            layout.setPanelHeight(90);
        }

        // sends admin to the canceled bills activity with the current date in intent
        imgHoy.setOnClickListener(v -> startActivity(CuentasCanceladasDelDiaActivity.newIntent(getBaseContext(), currentDate)));

        imgCuentaDate.setOnClickListener(v -> new DatePicker().show(getSupportFragmentManager(), "DATE_PICKER"));

        setupAdapter();
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

    private void init() {
        imgCuentaDate = findViewById(R.id.imgCuentaDate);
        imgHoy = findViewById(R.id.imgHoy);
        currentDate = Utils.getCurrentDate();
        imgSlide = findViewById(R.id.imgSlidingUp);
        layout = findViewById(R.id.slidingUpCuentas);
        recCuentas = findViewById(R.id.recCuentas);
        manager = getSupportFragmentManager();
        collection = fStore.collection(Utils.CUENTAS)
                .document(currentDate)
                .collection("cuentas corrientes");
        handler = new Handler();
    }

    public void setupAdapter() {

        Query query = collection.orderBy(Utils.KEY_MESA);
        FirestoreRecyclerOptions<Cuenta> options = new FirestoreRecyclerOptions.Builder<Cuenta>()
                .setQuery(query, Cuenta.class)
                .build();
        adapter = new AdapterCuentas(options, CuentasActivity.this, this, currentDate, handler, manager);
        recCuentas.setAdapter(adapter);
        recCuentas.setLayoutManager(new LinearLayoutManager(this));

        // swiping a list item will cause the bill to be canceled
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                adapter.cancel(viewHolder.getBindingAdapterPosition());

            }
        }).attachToRecyclerView(recCuentas);

        adapter.setOnItemLongClickListener(((snapshot, position, view) -> {

            // on long click we build an alert dialog before deleting the whole bill
            // dialog prompts admin to type in a reason for deletion
            // then on a background thread we construct a redaction and store it into the Firestore, then delete the bill
            View eliminarView = getLayoutInflater().inflate(R.layout.dialog_eliminacion, null);
            EditText edtEliminar = eliminarView.findViewById(R.id.edtEliminar);
            FloatingActionButton fabEliminar = eliminarView.findViewById(R.id.fabEliminar);

            AlertDialog dialog = new AlertDialog.Builder(CuentasActivity.this)
                    .setView(eliminarView)
                    .create();

            fabEliminar.setOnClickListener(v -> {

                String comment = edtEliminar.getText().toString().trim();
                if (comment.isEmpty()) {
                    comment = "sin comentario";
                }
                App.executor.submit(new EliminationApplier(
                        snapshot.getString(Utils.KEY_NAME),
                        snapshot.getString(Utils.KEY_MESA),
                        comment,
                        snapshot.getReference().getPath() + "/cuenta"));

                snapshot.getReference().delete().addOnSuccessListener(App.executor, unused -> {

                    // if the client doesn't have any pedidos assigned to them, we want to close the session and unblock the table
                    // we do it after the deletion is done, so the runnable doesn't think the client still has a cuenta
                    App.executor.submit(new UniquePedidoDeleter(snapshot));
                });
                dialog.dismiss();


            });

            dialog.show();
        }));

        adapter.setOnAddClickListener(((snapshot, position, view) -> {

            // the on click listener is set to the add new product button
            // we build an alert dialog to create a new product
            // no categories needed in a bill
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_new_item, null);

            EditText nameEt = dialogView.findViewById(R.id.etNewItemCuentaNombre);
            EditText priceEt = dialogView.findViewById(R.id.etNewItemCuentaPrecio);
            EditText countEt = dialogView.findViewById(R.id.etNewItemCuentaCantidad);

            new AlertDialog.Builder(CuentasActivity.this)
                    .setView(dialogView)
                    .setPositiveButton(getString(R.string.agregar), (dialog, which) -> {

                        // get the string from the edit texts
                        String name = nameEt.getText().toString().trim();
                        String priceString = priceEt.getText().toString();
                        String countString = countEt.getText().toString();

                        if (name.isEmpty() || priceString.isEmpty() || countString.isEmpty()) {
                            Toast.makeText(CuentasActivity.this, getString(R.string.llenar_los_campos), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // convert the numeric strings to longs
                        long price = Long.parseLong(priceString);
                        long count = Long.parseLong(countString);

                        // get the path of the cuenta meta doc
                        String path = snapshot.getReference().getPath();

                        App.executor.submit(new NewItemCuentaAdder(name, path, count, price));
                    })
                    .create()
                    .show();

        }));

        adapter.setOnRedactClickListener((snapshot, position, view) -> fStore.collection(snapshot.getReference().getPath() + "/cuenta").get()
                .addOnSuccessListener(App.executor, queryDocumentSnapshots -> new Redact(
                        queryDocumentSnapshots.getDocuments(),
                        snapshot.getString(Utils.KEY_NAME),
                        snapshot.getString(Utils.KEY_MESA),
                        Utils.CUENTAS
                ).show(getSupportFragmentManager(), "redaction")));
    }

    @Override
    @SuppressLint("SimpleDateFormat")
    public void onDateSet(android.widget.DatePicker view, int year, int month, int dayOfMonth) {

        // set the chosen date to the required format and send it to the canceled bills activity via intent
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, dayOfMonth);
        SimpleDateFormat format = new SimpleDateFormat(Utils.DATE_FORMAT);
        String date = format.format(calendar.getTime());

        startActivity(CuentasCanceladasDelDiaActivity.newIntent(getBaseContext(), date));
    }
}