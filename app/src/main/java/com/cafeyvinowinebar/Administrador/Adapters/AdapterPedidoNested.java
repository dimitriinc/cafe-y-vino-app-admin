package com.cafeyvinowinebar.Administrador.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cafeyvinowinebar.Administrador.POJOs.ItemShort;
import com.cafeyvinowinebar.Administrador.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class AdapterPedidoNested extends FirestoreRecyclerAdapter<ItemShort, AdapterPedidoNested.PedidoTwoViewHolder> {

    public AdapterPedidoNested(@NonNull FirestoreRecyclerOptions<ItemShort> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull PedidoTwoViewHolder holder, int position, @NonNull ItemShort model) {
        holder.bind(model);
    }

    @NonNull
    @Override
    public PedidoTwoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_consumo, parent, false);
        return new PedidoTwoViewHolder(view);
    }

    static class PedidoTwoViewHolder extends RecyclerView.ViewHolder {

        private final TextView txtItemName, txtItemCount;

        public PedidoTwoViewHolder(@NonNull View itemView) {
            super(itemView);

            txtItemName = itemView.findViewById(R.id.txtConsumoName);
            txtItemCount = itemView.findViewById(R.id.txtConsumoCount);

        }

        public void bind(ItemShort model) {
            txtItemName.setText(model.getName());
            txtItemCount.setText(String.valueOf(model.getCount()));
        }
    }
}
