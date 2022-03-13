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

public class AdapterNotiPedido extends FirestoreRecyclerAdapter<ItemShort, AdapterNotiPedido.ViewHolder> {

    public AdapterNotiPedido(@NonNull FirestoreRecyclerOptions<ItemShort> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull ItemShort model) {

        holder.txtName.setText(model.getName());
        holder.txtCount.setText(String.valueOf(model.getCount()));

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_noti_pedido, parent, false);
        return new ViewHolder(view);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtName, txtCount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtName = itemView.findViewById(R.id.txtNotiItemName);
            txtCount = itemView.findViewById(R.id.txtNotiItemCount);

        }
    }
}
