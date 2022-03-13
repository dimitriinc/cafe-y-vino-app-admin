package com.cafeyvinowinebar.Administrador.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cafeyvinowinebar.Administrador.POJOs.MenuItem;
import com.cafeyvinowinebar.Administrador.Interfaces.OnSwitchClickListener;
import com.cafeyvinowinebar.Administrador.R;
import com.cafeyvinowinebar.Administrador.Utils;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class AdapterCarta extends FirestoreRecyclerAdapter<MenuItem, AdapterCarta.ItemViewHolder> {

    OnSwitchClickListener listener;

    public AdapterCarta(@NonNull FirestoreRecyclerOptions<MenuItem> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull AdapterCarta.ItemViewHolder holder, int position, @NonNull MenuItem model) {
        holder.bind(model, position);
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_menu_item, parent, false);
        return new ItemViewHolder(view);
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {

        private final TextView txtItem;
        private final SwitchMaterial swBtn;

        public ItemViewHolder(@NonNull View itemView) {

            super(itemView);
            txtItem = itemView.findViewById(R.id.txtItem);
            swBtn = itemView.findViewById(R.id.swBtn);

            swBtn.setOnClickListener(v -> {
                int position = getAbsoluteAdapterPosition();
                boolean isChecked = swBtn.isChecked();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onSwitchClick(getSnapshots().getSnapshot(position), isChecked);
                }
            });
        }

        public void bind(MenuItem item, int position) {
            txtItem.setText(item.getNombre());
            Boolean isPresent = getSnapshots().getSnapshot(position).getBoolean(Utils.IS_PRESENT);
            if (isPresent != null) {
                swBtn.setChecked(isPresent);
            }

        }
    }

    public void setOnSwitchClickListener(OnSwitchClickListener listener) {
        this.listener = listener;
    }
}
