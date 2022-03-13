package com.cafeyvinowinebar.Administrador.Adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cafeyvinowinebar.Administrador.Fragments.Vinos;
import com.cafeyvinowinebar.Administrador.POJOs.MenuCategory;
import com.cafeyvinowinebar.Administrador.POJOs.MenuItem;
import com.cafeyvinowinebar.Administrador.POJOs.ProductEntity;
import com.cafeyvinowinebar.Administrador.ProductsViewModel;
import com.cafeyvinowinebar.Administrador.R;
import com.cafeyvinowinebar.Administrador.Utils;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Objects;

public class AdapterMenu extends FirestoreRecyclerAdapter<MenuCategory, RecyclerView.ViewHolder> {

    // two view types
    public static final int TYPE_NORMAL = 0;
    public static final int TYPE_VINOS = 1;

    private final FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    private final Context context;
    private final Activity activity;
    private final FragmentManager manager;
    private final ProductsViewModel viewModel;

    public AdapterMenu(@NonNull FirestoreRecyclerOptions<MenuCategory> options, Context context,
                       Activity activity, FragmentManager manager, ProductsViewModel viewModel) {
        super(options);
        this.context = context;
        this.activity = activity;
        this.manager = manager;
        this.viewModel = viewModel;
    }

    @Override
    public int getItemViewType(int position) {

        // return a special view type for the wine category
        String catName = getSnapshots().getSnapshot(position).getString(Utils.KEY_NAME);
        assert catName != null;
        if (catName.equals("Vinos")) {
            return TYPE_VINOS;
        } else {
            return TYPE_NORMAL;
        }
    }

    @Override
    protected void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull MenuCategory model) {

        // call a bind() according to the view type
        if (getItemViewType(position) == TYPE_NORMAL) {
            ((ViewHolderNormal) holder).bind(model);
        } else {
            ((ViewHolderVinos) holder).bind(model);
        }

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        // return a view holder according to the view type
        if (viewType == TYPE_NORMAL) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_menu_cat, parent, false);
            return new ViewHolderNormal(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_menu_cat_vinos, parent, false);
            return new ViewHolderVinos(view);
        }

    }

    private class ViewHolderNormal extends RecyclerView.ViewHolder {

        private final TextView catTitle;
        private final RecyclerView recMenuItems;

        public ViewHolderNormal(@NonNull View itemView) {
            super(itemView);

            catTitle = itemView.findViewById(R.id.txtMenuCat);
            recMenuItems = itemView.findViewById(R.id.recMenuItems);
        }

        public void bind(MenuCategory model) {
            catTitle.setText(model.getName());

            // all the categories apart from wines have a nested recycler view to display its products
            Query query = fStore.collection(model.getCatPath()).whereEqualTo(Utils.IS_PRESENT, true);

            FirestoreRecyclerOptions<MenuItem> options = new FirestoreRecyclerOptions.Builder<MenuItem>()
                    .setQuery(query, MenuItem.class)
                    .build();

            recMenuItems.setLayoutManager(new GridLayoutManager(context, 3));
            AdapterProducts adapter = new AdapterProducts(options);
            recMenuItems.setAdapter(adapter);
            adapter.startListening();
            adapter.setOnItemClickListener((snapshot, position1, view) -> {

                // on click we add one item of the product to the canasta
                // the same product may already be in the canasta, we check it
                ProductEntity product = viewModel.getProduct(snapshot.getString(Utils.KEY_NOMBRE));
                if (product == null) {

                    // there is no such product in the table, so we insert one
                    viewModel.insert(new ProductEntity(snapshot.getString(Utils.KEY_NOMBRE), snapshot.getString("categoria"),
                            1, Long.parseLong(Objects.requireNonNull(snapshot.getString(Utils.KEY_PRECIO)))));
                } else {

                    // there is, so we increment its count
                    long count = product.getCount();
                    count++;
                    viewModel.increment(product.getName(), count);
                }

            });

            // we also need a moment to stop listening to the nested adapter
            // the best i could think about is when the activity is destroyed
            if (activity.isDestroyed()) {
                adapter.stopListening();
            }

        }

    }

    /**
     * A special view holder for the wine category
     * Unlike other categories, it has nested categories itself
     * We show them in a dialog fragment
     */
    private class ViewHolderVinos extends RecyclerView.ViewHolder {

        private final TextView title;

        public ViewHolderVinos(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.txtMenuCatVinos);
            itemView.setOnClickListener(view -> new Vinos(viewModel).show(manager, "VINOS"));
        }

        public void bind(MenuCategory model) {
            title.setText(model.getName());
        }


    }
}
