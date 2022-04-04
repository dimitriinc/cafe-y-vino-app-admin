package com.cafeyvinowinebar.Administrador.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.cafeyvinowinebar.Administrador.App;
import com.cafeyvinowinebar.Administrador.MesasViewModel;
import com.cafeyvinowinebar.Administrador.POJOs.MesaEntity;
import com.cafeyvinowinebar.Administrador.R;
import com.cafeyvinowinebar.Administrador.Runnables.MesaInCuentaChanger;
import com.cafeyvinowinebar.Administrador.Utils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.concurrent.ExecutionException;

/**
 * Displays the list of custom created tables in the 'present' state, i.e. there are pedidos or cuentas assigned to them
 */
public class AdapterCustomUsuarios extends ListAdapter<MesaEntity, AdapterCustomUsuarios.ViewHolder> {

    private final Context context;
    private final MesasViewModel viewModel;

    public AdapterCustomUsuarios(Context context, MesasViewModel viewModel) {
        super(DIFF_CALLBACK);
        this.context = context;
        this.viewModel = viewModel;
    }

    private static final DiffUtil.ItemCallback<MesaEntity> DIFF_CALLBACK = new DiffUtil.ItemCallback<MesaEntity>() {
        @Override
        public boolean areItemsTheSame(@NonNull MesaEntity oldItem, @NonNull MesaEntity newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull MesaEntity oldItem, @NonNull MesaEntity newItem) {
            return oldItem.getMesa().equals(newItem.getMesa()) &&
                    oldItem.isBlocked() == newItem.isBlocked() &&
                    oldItem.isPresent() == newItem.isPresent();
        }
    };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_usario, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView txtNombre;
        private final TextView txtMesa;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtMesa = itemView.findViewById(R.id.txtUsarioMesa);
            txtNombre = itemView.findViewById(R.id.txtUsarioNombre);

            // on click admin can assign a new name to the table
            itemView.setOnClickListener(v -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                View view = LayoutInflater.from(context).inflate(R.layout.dialog_custom_mesa, null);
                EditText edtNewMesa = view.findViewById(R.id.edtNewMesa);
                FloatingActionButton fabOkNewMesa = view.findViewById(R.id.fabOkNewMesa);
                builder.setView(view);
                fabOkNewMesa.setOnClickListener(v2 -> {
                    String newMesa = edtNewMesa.getText().toString().trim();
                    if (newMesa.isEmpty()) {
                        Toast.makeText(context, R.string.llenar_los_campos, Toast.LENGTH_SHORT).show();
                    } else {

                        // change the mesa value in the pedidos and cuenta in the fStore
                        App.executor.submit(new MesaInCuentaChanger(
                                Utils.getCurrentDate(),
                                getItem(getAbsoluteAdapterPosition()).getMesa(),
                                newMesa));

                        // change the mesa value of the entity in the SQLite DB
                        getItem(getAbsoluteAdapterPosition()).setMesa(newMesa);


                    }
                });
                builder.create().show();
            });

            itemView.setOnLongClickListener(v -> {
                viewModel.setPresence(getItem(getAbsoluteAdapterPosition()).getId(), false);
                return true;
            });
        }

        private void bind(MesaEntity mesa) {
            txtMesa.setText(mesa.getMesa());
            txtNombre.setText(R.string.cliente);
        }
    }
}
