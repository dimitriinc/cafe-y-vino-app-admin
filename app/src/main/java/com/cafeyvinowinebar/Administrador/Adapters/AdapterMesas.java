package com.cafeyvinowinebar.Administrador.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.cafeyvinowinebar.Administrador.App;
import com.cafeyvinowinebar.Administrador.MesasViewModel;
import com.cafeyvinowinebar.Administrador.NewPedidoActivity;
import com.cafeyvinowinebar.Administrador.POJOs.Mesa;
import com.cafeyvinowinebar.Administrador.POJOs.MesaEntity;
import com.cafeyvinowinebar.Administrador.R;
import com.cafeyvinowinebar.Administrador.Utils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;

public class AdapterMesas extends ListAdapter<MesaEntity, AdapterMesas.ViewHolder> {

    private final FirebaseFirestore fStore = FirebaseFirestore.getInstance();

    private final Context context;
    private final MesasViewModel viewModel;
    private final String currentDate;

    public AdapterMesas(Context context, MesasViewModel viewModel) {
        super(DIFF_CALLBACK);
        this.context = context;
        this.viewModel = viewModel;
        currentDate = Utils.getCurrentDate();
    }

    private static final DiffUtil.ItemCallback<MesaEntity> DIFF_CALLBACK = new DiffUtil.ItemCallback<MesaEntity>() {
        @Override
        public boolean areItemsTheSame(@NonNull MesaEntity oldItem, @NonNull MesaEntity newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull MesaEntity oldItem, @NonNull MesaEntity newItem) {
            return oldItem.getMesa().equals(newItem.getMesa()) &&
                    oldItem.isBlocked() == newItem.isBlocked();
        }
    };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_reserva_model, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MesaEntity mesa = getItem(position);
        holder.bind(mesa);

    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView txtMesa;
        private final RelativeLayout parent;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtMesa = itemView.findViewById(R.id.txtModel);
            parent = itemView.findViewById(R.id.modelParent);

            itemView.setOnClickListener(view -> {

                // if the table isn't blocked, we go to the NewPedidoActivity to make a new custom order
                MesaEntity mesa = getItem(getAbsoluteAdapterPosition());
                if (mesa.isBlocked()) {

                    // the table is occupied by the app's user
                    Toast.makeText(context, R.string.mesa_blocked, Toast.LENGTH_SHORT).show();
                } else {
                    context.startActivity(NewPedidoActivity.newIntent(context, mesa.getMesa()));

                }
            });

            itemView.setOnLongClickListener(view -> {

                // deletes a table
                // but we don't delete the fixed tables (01 - 12)
                // a side note: users' tables that are outside of the set of the fixed ones won't be displayed in the recycler view
                // so there is no way for admins to accidentally delete it
                String mesa = getItem(getAbsoluteAdapterPosition()).getMesa();
                boolean mesaIsFixed = false;

                // using the static list of strings stored in the Utils class we check if the table is one of the fixed ones
                for (String table : Utils.FIXED_MESAS) {
                    if (mesa.equals(table)) {

                        // table is fixed, nothing happens
                        mesaIsFixed = true;
                        break;
                    }
                }
                if (!mesaIsFixed) {

                    // permission is granted, we delete the entity from the mesas table
                    viewModel.delete(getItem(getAbsoluteAdapterPosition()));
                }
                return true;
            });
        }

        /**
         * Checks if there is a bill or orders assigned to the table
         * And if those documents belong to a user of the Client App
         */
        public void bind(MesaEntity mesa) {

            String mesaName = mesa.getMesa();
            txtMesa.setText(mesaName);

            Query queryPedidos = fStore.collection("pedidos")
                    .document(currentDate)
                    .collection("pedidos enviados")
                    .whereEqualTo(Utils.KEY_MESA, mesaName)
                    .whereEqualTo(Utils.SERVIDO, false);

            Query queryCuentas = fStore.collection("cuentas")
                    .document(currentDate)
                    .collection("cuentas corrientes")
                    .whereEqualTo(Utils.KEY_MESA, mesaName);


            // first we check the cuentas
            queryCuentas.get().addOnSuccessListener(App.executor, queryDocumentSnapshots -> {

                if (!queryDocumentSnapshots.isEmpty()) {

                    // if the query is not empty, it means that the table is occupied by someone
                    // we want to know if that someone is a user of the Client App
                    // but first we assume that it's a custom table created by admin and set the greenish color
                    // admin can add custom orders to a custom table
                    parent.setBackgroundColor(context.getColor(R.color.llegado_light));
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {

                        // the cuenta meta doc of a custom table will have the id that corresponds to the name of the table
                        // the cuenta meta doc id of a table belonging to a user is this user's Uid
                        if (!document.getId().equals(mesa.getMesa())) {

                            // the table belongs to a user, we set the pinkish color and block that table
                            // admin cannot create custom orders for a user of the app
                            parent.setBackgroundColor(context.getColor(R.color.regal_light));
                            mesa.setBlocked(true);
                        }
                    }

                } else {

                    // there is no cuenta assigned to this table
                    // but there could be pending orders assigned to the user, which would also mean that the table is occupied
                    queryPedidos.get().addOnSuccessListener(App.executor, queryDocumentSnapshots1 -> {

                        if (!queryDocumentSnapshots1.isEmpty()) {

                            // there is at least one order assigned to the table
                            // we assume it's custom
                            parent.setBackgroundColor(context.getColor(R.color.llegado_light));
                            for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots1) {

                                // a custom order meta doc will have a field userId that will be the same as the name of the table
                                if (!Objects.equals(snapshot.getString(Utils.KEY_USER_ID), mesa.getMesa())) {

                                    // the order belongs to a user, we block it
                                    parent.setBackgroundColor(context.getColor(R.color.regal_light));
                                    mesa.setBlocked(true);
                                }
                            }
                        }
                    });
                }
            });
        }
    }
}
