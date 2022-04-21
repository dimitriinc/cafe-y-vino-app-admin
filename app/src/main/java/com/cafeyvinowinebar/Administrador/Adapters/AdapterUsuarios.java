package com.cafeyvinowinebar.Administrador.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cafeyvinowinebar.Administrador.Interfaces.OnItemClickListener;
import com.cafeyvinowinebar.Administrador.Interfaces.OnItemLongClickListener;
import com.cafeyvinowinebar.Administrador.POJOs.Usuario;
import com.cafeyvinowinebar.Administrador.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

/**
 * Displays the list of present users in the UsuariosActivity
 */
public class AdapterUsuarios extends FirestoreRecyclerAdapter<Usuario, AdapterUsuarios.ItemViewHolder> {

    private OnItemClickListener listener;
    private OnItemLongClickListener longListener;

    public AdapterUsuarios(@NonNull FirestoreRecyclerOptions<Usuario> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull ItemViewHolder holder, int position, @NonNull Usuario model) {
        holder.bind(model);
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_usario, parent, false);
        return new ItemViewHolder(view);
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {

        private final TextView txtNombre;
        private final TextView txtMesa;

        public ItemViewHolder(@NonNull View itemView) {
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

        public void bind(Usuario usuario) {

            // we want only the first name of the user on display
            String[] userNames = usuario.getNombre().split(" ");
            txtNombre.setText(userNames[0]);
            txtMesa.setText(usuario.getMesa());
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener longListener) {
        this.longListener = longListener;
    }
}
