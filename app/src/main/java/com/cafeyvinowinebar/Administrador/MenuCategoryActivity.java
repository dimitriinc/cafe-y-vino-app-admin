package com.cafeyvinowinebar.Administrador;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.cafeyvinowinebar.Administrador.Adapters.AdapterCarta;
import com.cafeyvinowinebar.Administrador.POJOs.MenuItem;
import com.cafeyvinowinebar.Administrador.Runnables.MenuItemSwitcher;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

/**
 * Show the product of the category, passed via the intent
 * Admin can manage the presence status of each product with a switch button
 */

public class MenuCategoryActivity extends AppCompatActivity {

    public static final String COL_PATH = "collection path";

    private final FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    private AdapterCarta adapter;

    public static Intent newIntent(Context context, String collectionPath) {
        Intent intent = new Intent(context, MenuCategoryActivity.class);
        intent.putExtra(COL_PATH, collectionPath);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_category);

        RecyclerView recView = findViewById(R.id.recView);
        FloatingActionButton fabCartaHome = findViewById(R.id.fabCartaHome);

        Query query = fStore.collection(getIntent().getStringExtra(COL_PATH));

        fabCartaHome.setOnClickListener(v -> startActivity(new Intent(this, MainActivity.class)));

        FirestoreRecyclerOptions<MenuItem> options = new FirestoreRecyclerOptions.Builder<MenuItem>()
                .setQuery(query, MenuItem.class)
                .build();
        adapter = new AdapterCarta(options);
        recView.setLayoutManager(new LinearLayoutManager(this));
        recView.setAdapter(adapter);

        // set the presence status of the product based on position of the switch
        adapter.setOnSwitchClickListener((snapshot, isChecked) -> App.executor.submit(new MenuItemSwitcher(snapshot, isChecked)));

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
}