package com.cafeyvinowinebar.Administrador.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cafeyvinowinebar.Administrador.Interfaces.OnItemClickListener;
import com.cafeyvinowinebar.Administrador.Interfaces.OnItemClickListenerCanasta;
import com.cafeyvinowinebar.Administrador.POJOs.MenuItem;
import com.cafeyvinowinebar.Administrador.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import java.util.concurrent.ExecutionException;

public class AdapterProducts extends FirestoreRecyclerAdapter<MenuItem, AdapterProducts.ViewHolder> {

    private OnItemClickListenerCanasta listener;

    public AdapterProducts(@NonNull FirestoreRecyclerOptions<MenuItem> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull MenuItem model) {
        holder.bind(model);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_product, parent, false);
        return new ViewHolder(view);
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView txtProduct;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtProduct = itemView.findViewById(R.id.txtProduct);

            itemView.setOnClickListener(view -> {

                int position = getAbsoluteAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    try {
                        listener.onItemClick(getSnapshots().getSnapshot(position), position, view);
                    } catch (ExecutionException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        public void bind(MenuItem model) {
            txtProduct.setText(model.getNombre());
        }
    }

    public void setOnItemClickListener(OnItemClickListenerCanasta listener) {
        this.listener = listener;
    }
}
