package com.cafeyvinowinebar.Administrador.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cafeyvinowinebar.Administrador.App;
import com.cafeyvinowinebar.Administrador.NewPedidoActivity;
import com.cafeyvinowinebar.Administrador.POJOs.Mesa;
import com.cafeyvinowinebar.Administrador.R;
import com.cafeyvinowinebar.Administrador.Utils;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Objects;

public class AdapterMesas extends FirestoreRecyclerAdapter<Mesa, AdapterMesas.ViewHolder> {

    private final FirebaseFirestore fStore = FirebaseFirestore.getInstance();

    private final Context context;
    private final String currentDate;

    public AdapterMesas(@NonNull FirestoreRecyclerOptions<Mesa> options, Context context) {
        super(options);
        currentDate = Utils.getCurrentDate();
        this.context = context;
    }


    @Override
    protected void onBindViewHolder(@NonNull AdapterMesas.ViewHolder holder, int position, @NonNull Mesa model) {
        holder.bind(model);
    }

    @NonNull
    @Override
    public AdapterMesas.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_reserva_model, parent, false);
        return new ViewHolder(view);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView txtMesa;
        private final RelativeLayout parent;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtMesa = itemView.findViewById(R.id.txtModel);
            parent = itemView.findViewById(R.id.modelParent);

            itemView.setOnClickListener(view -> {

                // if the table isn't blocked, we go to the NewPedidoActivity to make a new custom order
                Mesa mesa = getSnapshots().getSnapshot(getAbsoluteAdapterPosition()).toObject(Mesa.class);
                assert mesa != null;
                if (mesa.isBlocked()) {

                    // the table is occupied by the app's user
                    Toast.makeText(context, R.string.mesa_blocked, Toast.LENGTH_SHORT).show();
                } else {
                    context.startActivity(NewPedidoActivity.newIntent(context, mesa.getName(),
                            getSnapshots().getSnapshot(getAbsoluteAdapterPosition()).getId()));
                }

            });

            itemView.setOnLongClickListener(view -> {

                // deletes the table
                // but we don't delete the fixed tables (01 - 12)
                // a side note: users' tables that are outside of the set of the fixed ones won't be displayed in the recycler view
                // so there is no way for admins to accidentally delete it

                DocumentSnapshot snapshot = getSnapshots().getSnapshot(getAbsoluteAdapterPosition());
                if (!snapshot.getBoolean("fixed")) {
                    snapshot.getReference().delete();
                }
                return true;
            });
        }

        /**
         * Checks if there is a bill or orders assigned to the table
         * And if those documents belong to a user of the Client App
         */
        public void bind(Mesa mesa) {

            String mesaName = mesa.getName();
            txtMesa.setText(mesaName);

            Query queryPedidos = fStore.collection("pedidos")
                    .document(currentDate)
                    .collection("pedidos enviados")
                    .whereEqualTo(Utils.KEY_MESA, mesaName);

            Query queryCuentas = fStore.collection("cuentas")
                    .document(currentDate)
                    .collection("cuentas corrientes")
                    .whereEqualTo(Utils.KEY_MESA, mesaName);


            // mark the table with color if it's blocked
            if (mesa.isBlocked()) {
                parent.setBackgroundColor(context.getColor(R.color.regal_light));
            }

            // first we check the cuentas
            queryCuentas.get().addOnSuccessListener(App.executor, queryDocumentSnapshots -> {

                if (!queryDocumentSnapshots.isEmpty()) {

                    // if the query is not empty, it means that the table is occupied by someone
                    // we want to know if that someone is a user of the Client App
                    // but first we assume that it's a custom table created by admin and set the greenish color
                    // admin can add custom orders to a custom table
                    parent.setBackgroundColor(context.getColor(R.color.llegado_light));
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {

                        // the name field of a custom cuenta meta doc will always be 'Cliente'
                        // that's how we distinguish custom cuentas from the cuentas of the users of the client app
                        if (!Objects.equals(document.getString(Utils.KEY_NAME), "Cliente")) {

                            // the table belongs to a user, we set the pinkish color and block that table
                            // admin cannot create custom orders for a user of the client app
                            parent.setBackgroundColor(context.getColor(R.color.regal_light));
                            getSnapshots().getSnapshot(getAbsoluteAdapterPosition()).getReference().update("blocked", true);
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

                                // a custom order meta doc will have a field 'user' set to 'Cliente'
                                if (!Objects.equals(snapshot.getString(Utils.KEY_USER), "Cliente")) {

                                    // the order belongs to a user of the client app, we block the table
                                    parent.setBackgroundColor(context.getColor(R.color.regal_light));
                                    getSnapshots().getSnapshot(getAbsoluteAdapterPosition()).getReference().update("blocked", true);
                                }
                            }
                        }
                    });
                }
            });
        }
    }

}
