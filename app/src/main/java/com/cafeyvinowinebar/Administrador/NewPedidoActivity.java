package com.cafeyvinowinebar.Administrador;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.cafeyvinowinebar.Administrador.Adapters.AdapterCanasta;
import com.cafeyvinowinebar.Administrador.Adapters.AdapterMenu;
import com.cafeyvinowinebar.Administrador.POJOs.MenuCategory;
import com.cafeyvinowinebar.Administrador.POJOs.ProductEntity;
import com.cafeyvinowinebar.Administrador.Runnables.PedidoCreator;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.Objects;

/**
 * Has a main screen and a sliding panel
 * On the main screen the menu is displayed, by clicking on one of its products, it gets added to the 'products' table of the SQLiteDB
 * Which is like a canasta for custom orders
 * This canasta is displayed on the sliding panel
 * The canasta is meant to be short-lived: when admin exits the activity, it's emptied all the time
 * Where it by sending the new order, or by leaving it without sending
 * When admin enters the activity anew, the canasta must be empty to start composition of a new order
 */
public class NewPedidoActivity extends AppCompatActivity {

    private final FirebaseFirestore fStore = FirebaseFirestore.getInstance();

    private SlidingUpPanelLayout layout;
    private ImageView imgSlide;
    private RecyclerView recMenu, recCanasta;
    private AdapterMenu adapterMenu;
    private FloatingActionButton fabCanastaOk, fabCanastaNewItem;
    public ProductsViewModel productsViewModel;
    FragmentManager manager;
    String mesa, currentDate;

    public static Intent newIntent(Context context, String mesa) {
        Intent i = new Intent(context, NewPedidoActivity.class);
        i.putExtra(Utils.KEY_MESA, mesa);
        return i;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_pedido);

        mesa = getIntent().getStringExtra(Utils.KEY_MESA);

        init();

        setupAdapterMenu();
        setupAdapterCanasta();

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

        fabCanastaOk.setOnClickListener(v -> {

            // create a new pedido in the FirestoreDB on a background thread
            App.executor.submit(new PedidoCreator(currentDate, mesa, productsViewModel.getProducts().getValue()));

            // clear the 'products' table
            productsViewModel.deleteAllProducts();

            // return to the dialog fragment with the list of tables
            finish();

        });

        fabCanastaNewItem.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_new_item, null);
            EditText nombreEt = dialogView.findViewById(R.id.etNewItemCuentaNombre);
            EditText precioEt = dialogView.findViewById(R.id.etNewItemCuentaPrecio);
            EditText countEt = dialogView.findViewById(R.id.etNewItemCuentaCantidad);
            builder.setView(dialogView);
            builder.setPositiveButton(getString(R.string.btn_cocina), (dialog, which) -> {
                String name = nombreEt.getText().toString().trim();
                String priceString = precioEt.getText().toString().trim();
                String countString = countEt.getText().toString().trim();
                if (name.isEmpty() || priceString.isEmpty() || countString.isEmpty()) {
                    Toast.makeText(this, getString(R.string.llenar_los_campos), Toast.LENGTH_SHORT).show();
                    return;
                }
                long price = Long.parseLong(priceString);
                long count = Long.parseLong(countEt.getText().toString().trim());
                productsViewModel.insert(new ProductEntity(name, Utils.COCINA, count, price));
            });
            builder.setNegativeButton(getString(R.string.btn_barra), (dialog, which) -> {
                String name = nombreEt.getText().toString().trim();
                String priceString = precioEt.getText().toString().trim();
                String countString = countEt.getText().toString().trim();
                if (name.isEmpty() || priceString.isEmpty() || countString.isEmpty()) {
                    Toast.makeText(this, getString(R.string.llenar_los_campos), Toast.LENGTH_SHORT).show();
                    return;
                }
                long price = Long.parseLong(priceString);
                long count = Long.parseLong(countString);
                productsViewModel.insert(new ProductEntity(name, Utils.BARRA, count, price));
            });
            builder.setCancelable(true);
            builder.create().show();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapterMenu.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapterMenu.stopListening();
    }

    private void init() {
        manager = getSupportFragmentManager();
        layout = findViewById(R.id.slidingUpNewPedidos);
        imgSlide = findViewById(R.id.imgMenuSlideUp);
        recMenu = findViewById(R.id.recMenu);
        recCanasta = findViewById(R.id.recCanasta);
        fabCanastaOk = findViewById(R.id.fabCanastaOk);
        fabCanastaNewItem = findViewById(R.id.fabCanastaNewItem);
        productsViewModel = new ViewModelProvider(this).get(ProductsViewModel.class);
        currentDate = Utils.getCurrentDate();
    }

    private void setupAdapterMenu() {
        Query query = fStore.collection("menu");
        FirestoreRecyclerOptions<MenuCategory> options = new FirestoreRecyclerOptions.Builder<MenuCategory>()
                .setQuery(query, MenuCategory.class)
                .build();
        recMenu.setLayoutManager(new LinearLayoutManager(this));
        adapterMenu = new AdapterMenu(options, this, this, manager, productsViewModel);
        recMenu.setAdapter(adapterMenu);
    }

    private void setupAdapterCanasta() {
        recCanasta.setLayoutManager(new GridLayoutManager(this, 2));
        final AdapterCanasta adapterCanasta = new AdapterCanasta(productsViewModel);
        recCanasta.setAdapter(adapterCanasta);
        productsViewModel.getProducts().observe(this, adapterCanasta::submitList);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        productsViewModel.deleteAllProducts();
    }

    @Override
    public void onBackPressed() {
        
        // if the back button was pressed by mistake, we want to warn admin that the products in the canasta will be lost
        // though there is no point in warning if the canasta is empty, so we first check it
        if (!Objects.requireNonNull(productsViewModel.getProducts().getValue()).isEmpty()) {

            // the canasta isn't empty, we show admin an alert dialog
            new AlertDialog.Builder(this)
                    .setTitle("Quieres salir?")
                    .setMessage("La canasta se vaciará")
                    .setPositiveButton("SI", (DialogInterface dialogInterface, int i) -> {

                        // leave the activity
                        finish();
                        dialogInterface.dismiss();
                    })
                    .setNegativeButton("NO", (DialogInterface dialogInterface, int i) -> dialogInterface.dismiss())
                    .show();
        } else {
            super.onBackPressed();
        }
    }
}