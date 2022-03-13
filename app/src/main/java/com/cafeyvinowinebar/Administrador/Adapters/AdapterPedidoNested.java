package com.cafeyvinowinebar.Administrador.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cafeyvinowinebar.Administrador.Interfaces.OnItemClickListener;
import com.cafeyvinowinebar.Administrador.POJOs.ItemShort;
import com.cafeyvinowinebar.Administrador.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.card.MaterialCardView;

public class AdapterPedidoNested extends FirestoreRecyclerAdapter<ItemShort, AdapterPedidoNested.PedidoTwoViewHolder> {

    private OnItemClickListener incrementor;
    private OnItemClickListener decremenator;

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

    class PedidoTwoViewHolder extends RecyclerView.ViewHolder {

        private final TextView txtItemName, txtItemCount;

        public PedidoTwoViewHolder(@NonNull View itemView) {
            super(itemView);

            txtItemName = itemView.findViewById(R.id.txtConsumoName);
            txtItemCount = itemView.findViewById(R.id.txtConsumoCount);
            MaterialCardView cardName = itemView.findViewById(R.id.cardConsumoName);
            MaterialCardView cardCount = itemView.findViewById(R.id.cardConsumoCount);

            cardName.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION && decremenator != null) {
                    decremenator.onItemClick(getSnapshots().getSnapshot(position), position, v);
                }
            });

            cardCount.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION && incrementor != null) {
                    incrementor.onItemClick(getSnapshots().getSnapshot(position), position, v);
                }
            });
        }

        public void bind(ItemShort model) {
            txtItemName.setText(model.getName());
            txtItemCount.setText(String.valueOf(model.getCount()));
        }
    }

    public void setDecremenator(OnItemClickListener decremenator) {
        this.decremenator = decremenator;
    }

    public void setIncrementor(OnItemClickListener incrementor) {
        this.incrementor = incrementor;
    }
}
