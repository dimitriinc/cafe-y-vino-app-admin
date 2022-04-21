package com.cafeyvinowinebar.Administrador.Adapters;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cafeyvinowinebar.Administrador.Interfaces.OnItemClickListener;
import com.cafeyvinowinebar.Administrador.Interfaces.OnItemLongClickListener;
import com.cafeyvinowinebar.Administrador.POJOs.Mesa;
import com.cafeyvinowinebar.Administrador.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Displays the list of custom created tables in the 'present' state, i.e. there are pedidos or cuentas assigned to them
 */
public class AdapterCustomUsuarios extends FirestoreRecyclerAdapter<Mesa, AdapterCustomUsuarios.ViewHolder> {

    private final FirebaseFirestore fStore = FirebaseFirestore.getInstance();

    private final Context context;
    private final Handler mainHandler;

    private OnItemClickListener listener;
    private OnItemLongClickListener longListener;

    public AdapterCustomUsuarios(@NonNull FirestoreRecyclerOptions<Mesa> options, Context context, Handler mainHandler) {
        super(options);
        this.context = context;
        this.mainHandler = mainHandler;
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull Mesa model) {
        holder.bind(model);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_usario, parent, false);
        return new ViewHolder(view);
    }


    class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView txtNombre;
        private final TextView txtMesa;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtMesa = itemView.findViewById(R.id.txtUsarioMesa);
            txtNombre = itemView.findViewById(R.id.txtUsarioNombre);

            itemView.setOnClickListener(v -> {
                int position = getAbsoluteAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(getSnapshots().getSnapshot(position), position, v);
                }
            });

            itemView.setOnLongClickListener(v -> {
                int position = getAbsoluteAdapterPosition();
                if (position != RecyclerView.NO_POSITION && longListener != null) {
                    longListener.onItemLongClick(getSnapshots().getSnapshot(position), position, v);
                }
                return true;
            });
        }

        private void bind(Mesa mesa) {
            txtMesa.setText(mesa.getName());
            txtNombre.setText(R.string.cliente);
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener longListener) {
        this.longListener = longListener;
    }
}
