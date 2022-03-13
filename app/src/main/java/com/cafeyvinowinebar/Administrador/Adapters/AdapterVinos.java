package com.cafeyvinowinebar.Administrador.Adapters;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cafeyvinowinebar.Administrador.Interfaces.OnItemClickListener;
import com.cafeyvinowinebar.Administrador.POJOs.MenuCategory;
import com.cafeyvinowinebar.Administrador.POJOs.MenuItem;
import com.cafeyvinowinebar.Administrador.POJOs.ProductEntity;
import com.cafeyvinowinebar.Administrador.ProductsViewModel;
import com.cafeyvinowinebar.Administrador.R;
import com.cafeyvinowinebar.Administrador.Utils;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class AdapterVinos extends FirestoreRecyclerAdapter<MenuCategory, AdapterVinos.ViewHolder> {

    private final FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    private final Context context;
    private final Activity activity;
    private final ProductsViewModel viewModel;

    public AdapterVinos(@NonNull FirestoreRecyclerOptions<MenuCategory> options, Context context,
                       Activity activity, ProductsViewModel viewModel) {
        super(options);
        this.context = context;
        this.activity = activity;
        this.viewModel = viewModel;
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull MenuCategory model) {
        holder.bind(model);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_menu_cat, parent, false);
        return new AdapterVinos.ViewHolder(view);
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView catTitle;
        private final RecyclerView recMenuItems;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            catTitle = itemView.findViewById(R.id.txtMenuCat);
            recMenuItems = itemView.findViewById(R.id.recMenuItems);
        }

        public void bind(MenuCategory model) {

            catTitle.setText(getSnapshots().getSnapshot(getAbsoluteAdapterPosition()).getId());

            // a nested recycler view that displays wines depending on the category
            Query query = fStore.collection(model.getCatPath()).whereEqualTo(Utils.IS_PRESENT, true);
            FirestoreRecyclerOptions<MenuItem> options = new FirestoreRecyclerOptions.Builder<MenuItem>()
                    .setQuery(query, MenuItem.class)
                    .build();
            recMenuItems.setLayoutManager(new GridLayoutManager(context, 3));
            AdapterProducts adapter = new AdapterProducts(options);
            recMenuItems.setAdapter(adapter);
            adapter.startListening();
            adapter.setOnItemClickListener((snapshot, position1, view) -> {

                // on click we insert a new product to the products table if there is no such product yet
                // increment the count, if there is
                ProductEntity product = viewModel.getProduct(snapshot.getString(Utils.KEY_NOMBRE));
                if (product == null) {
                    viewModel.insert(new ProductEntity(snapshot.getString(Utils.KEY_NOMBRE), Utils.BARRA,
                            1, Long.parseLong(snapshot.getString(Utils.KEY_PRECIO))));
                } else {
                    long count = product.getCount();
                    count++;
                    viewModel.increment(product.getName(), count);
                }

            });
            if (activity.isDestroyed()) {
                adapter.stopListening();
            }
        }
    }
}
