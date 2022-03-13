package com.cafeyvinowinebar.Administrador.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cafeyvinowinebar.Administrador.POJOs.CuentaItem;
import com.cafeyvinowinebar.Administrador.Interfaces.OnItemClickListener;
import com.cafeyvinowinebar.Administrador.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.card.MaterialCardView;

public class AdapterCuentasNested extends FirestoreRecyclerAdapter<CuentaItem, AdapterCuentasNested.ViewHolder> {

    private OnItemClickListener incrementor;
    private OnItemClickListener decrementor;

    public AdapterCuentasNested(@NonNull FirestoreRecyclerOptions<CuentaItem> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull AdapterCuentasNested.ViewHolder holder, int position, @NonNull CuentaItem model) {
        holder.bind(model);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_cuenta_item, parent, false);
        return new ViewHolder(view);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView cuentaCount, cuentaName, cuentaPrice, cuentaTotal;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            cuentaCount = itemView.findViewById(R.id.cuentaCount);
            cuentaName = itemView.findViewById(R.id.cuentaName);
            cuentaPrice = itemView.findViewById(R.id.cuentaPrice);
            cuentaTotal = itemView.findViewById(R.id.cuentaTotal);

            MaterialCardView cardCount = itemView.findViewById(R.id.cardCount);
            MaterialCardView cardTotal = itemView.findViewById(R.id.cardTotal);

            cardTotal.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION && incrementor != null) {
                    incrementor.onItemClick(getSnapshots().getSnapshot(position), position, v);
                }
            });

            cardCount.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION && decrementor != null) {
                    decrementor.onItemClick(getSnapshots().getSnapshot(position), position, v);
                }
            });

        }

        public void bind(CuentaItem model) {
            cuentaTotal.setText(String.valueOf(model.getTotal()));
            cuentaPrice.setText(String.valueOf(model.getPrice()));
            cuentaCount.setText(String.valueOf(model.getCount()));
            cuentaName.setText(model.getName());
        }
    }

    public void setIncrementor(OnItemClickListener incrementor) {
        this.incrementor = incrementor;
    }

    public void setDecrementor(OnItemClickListener decrementor) {this.decrementor = decrementor;}

}
