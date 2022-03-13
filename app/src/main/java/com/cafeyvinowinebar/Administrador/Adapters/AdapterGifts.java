package com.cafeyvinowinebar.Administrador.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cafeyvinowinebar.Administrador.Interfaces.OnItemLongClickListener;
import com.cafeyvinowinebar.Administrador.POJOs.Gift;
import com.cafeyvinowinebar.Administrador.R;
import com.cafeyvinowinebar.Administrador.Utils;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;

public class AdapterGifts extends FirestoreRecyclerAdapter<Gift, AdapterGifts.ViewHolder> {

    private OnItemLongClickListener longListener;
    private final Context context;

    public AdapterGifts(@NonNull FirestoreRecyclerOptions<Gift> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull Gift model) {
        holder.bind(model);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_pedido, parent, false);
        return new ViewHolder(view);
    }

    /**
     * The method is called when the item is swiped
     * Which marks the document as served
     */
    public void cancel(int position) {
        DocumentSnapshot snapshot = getSnapshots().getSnapshot(position);
        snapshot.getReference().update(Utils.SERVIDO, true);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView txtItem;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtItem = itemView.findViewById(R.id.txtItemName);

            itemView.setOnLongClickListener(v -> {
                int position = getAbsoluteAdapterPosition();
                if (position != RecyclerView.NO_POSITION && longListener != null) {
                    longListener.onItemLongClick(getSnapshots().getSnapshot(position), position, v);
                }
                return true;
            });
        }

        public void bind(Gift model) {
            txtItem.setText(context.getString(R.string.gift_item, model.getNombre(), model.getUser(), model.getMesa()));
        }
    }

    public void setOnItemLongClickListener(OnItemLongClickListener longListener) {
        this.longListener = longListener;
    }
}
