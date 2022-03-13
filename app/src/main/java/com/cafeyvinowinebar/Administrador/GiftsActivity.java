package com.cafeyvinowinebar.Administrador;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.cafeyvinowinebar.Administrador.Adapters.AdapterGifts;
import com.cafeyvinowinebar.Administrador.Adapters.AdapterGiftsCanceladas;
import com.cafeyvinowinebar.Administrador.POJOs.Gift;
import com.cafeyvinowinebar.Administrador.Runnables.GiftBackoff;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

/**
 * Displays a list of gifts from the fidelity program
 * The activity can be opened from the PedidosActivity, in which case we display the requests of gifts for the current date
 * And from the CuentasCanceladasDelDiaActivity, in which case we display the gifts already served on the certain date
 * Each case uses its own adapter
 * Which adapter to deploy we decide based on the 'servido' boolean value passed via the intent
 */
public class GiftsActivity extends AppCompatActivity {

    private final FirebaseFirestore fStore = FirebaseFirestore.getInstance();

    private RecyclerView recGifts;
    private String fecha;
    private AdapterGifts adapterRequests;
    private AdapterGiftsCanceladas adapterServed;

    public static Intent newIntent(Context context, boolean servido, String fecha) {
        Intent i = new Intent(context, GiftsActivity.class);
        i.putExtra(Utils.SERVIDO, servido);
        i.putExtra(Utils.KEY_FECHA, fecha);
        return i;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gifts);

        boolean servido = getIntent().getBooleanExtra(Utils.SERVIDO, false);
        fecha = getIntent().getStringExtra(Utils.KEY_FECHA);

        FloatingActionButton fabHome = findViewById(R.id.fabGiftsHome);
        fabHome.setOnClickListener(v -> startActivity(new Intent(GiftsActivity.this, MainActivity.class)));

        recGifts = findViewById(R.id.recGifts);

        if (servido) {
            setupAdapterServed();
        } else {
            setupAdapterRequests();
        }
    }

    private void setupAdapterRequests() {

        Query query = fStore.collection("pedidos").document(fecha).collection("regalos")
                .whereEqualTo(Utils.SERVIDO, false)
                .orderBy(Utils.TIMESTAMP, Query.Direction.ASCENDING);

        FirestoreRecyclerOptions<Gift> options = new FirestoreRecyclerOptions.Builder<Gift>()
                .setQuery(query, Gift.class)
                .build();

        adapterRequests = new AdapterGifts(options, this);
        recGifts.setAdapter(adapterRequests);

        // on long click admin can undo the request and give the customer his bonus points back
        adapterRequests.setOnItemLongClickListener((snapshot, position, v) -> App.executor.submit(new GiftBackoff(snapshot)));

        recGifts.setLayoutManager(new LinearLayoutManager(this));

        // when a product is served admin swipes the item, which triggers the cancel() method defined in the adapter
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                adapterRequests.cancel(viewHolder.getBindingAdapterPosition());

            }
        }).attachToRecyclerView(recGifts);
    }

    private void setupAdapterServed() {

        // the list of served products is just for observation purposes
        Query query = fStore.collection("pedidos").document(fecha).collection("regalos")
                .whereEqualTo(Utils.SERVIDO, true)
                .orderBy(Utils.TIMESTAMP, Query.Direction.ASCENDING);

        FirestoreRecyclerOptions<Gift> options = new FirestoreRecyclerOptions.Builder<Gift>()
                .setQuery(query, Gift.class)
                .build();

        adapterServed = new AdapterGiftsCanceladas(options, GiftsActivity.this);
        recGifts.setAdapter(adapterServed);
        recGifts.setLayoutManager(new LinearLayoutManager(this));
    }


    // since we have two types of adapters, we need to check which one to listen
    @Override
    protected void onStart() {
        super.onStart();
        if (adapterRequests != null) {
            adapterRequests.startListening();
        }
        if (adapterServed != null) {
            adapterServed.startListening();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (adapterRequests != null) {
            adapterRequests.stopListening();
        }
        if (adapterServed != null) {
            adapterServed.stopListening();
        }
    }
}