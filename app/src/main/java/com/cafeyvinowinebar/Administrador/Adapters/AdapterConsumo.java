package com.cafeyvinowinebar.Administrador.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.cafeyvinowinebar.Administrador.POJOs.CuentaItem;
import com.cafeyvinowinebar.Administrador.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class AdapterConsumo extends FirestoreRecyclerAdapter<CuentaItem, AdapterConsumo.ViewHolder> {

    public AdapterConsumo(@NonNull FirestoreRecyclerOptions<CuentaItem> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull CuentaItem model) {
        holder.bind(model);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_consumo, parent, false);
        return new ViewHolder(view);
    }


//    private static final DiffUtil.ItemCallback<CuentaItem> DIFF_CALLBACK = new DiffUtil.ItemCallback<CuentaItem>() {
//        @Override
//        public boolean areItemsTheSame(@NonNull CuentaItem oldItem, @NonNull CuentaItem newItem) {
//            return oldItem.getName().equals(newItem.getName());
//        }
//
//        @Override
//        public boolean areContentsTheSame(@NonNull CuentaItem oldItem, @NonNull CuentaItem newItem) {
//            return oldItem.getCount() == newItem.getCount();
//        }
//    };

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtName, txtCount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtName = itemView.findViewById(R.id.txtConsumoName);
            txtCount = itemView.findViewById(R.id.txtConsumoCount);
        }

        public void bind(CuentaItem item) {
            txtName.setText(item.getName());
            txtCount.setText(String.valueOf(item.getCount()));
        }
    }
}
