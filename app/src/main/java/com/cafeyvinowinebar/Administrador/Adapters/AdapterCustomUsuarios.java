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
import com.cafeyvinowinebar.Administrador.POJOs.Mesa;
import com.cafeyvinowinebar.Administrador.POJOs.MesaEntity;
import com.cafeyvinowinebar.Administrador.R;
import com.cafeyvinowinebar.Administrador.Runnables.MesaInCuentaChanger;
import com.cafeyvinowinebar.Administrador.Utils;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Objects;
import java.util.concurrent.ExecutionException;

/**
 * Displays the list of custom created tables in the 'present' state, i.e. there are pedidos or cuentas assigned to them
 */
public class AdapterCustomUsuarios extends FirestoreRecyclerAdapter<Mesa, AdapterCustomUsuarios.ViewHolder> {

    private final FirebaseFirestore fStore = FirebaseFirestore.getInstance();

    private final Context context;

    public AdapterCustomUsuarios(@NonNull FirestoreRecyclerOptions<Mesa> options, Context context) {
        super(options);
        this.context = context;
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

            // on click admin can assign a new name to the table
            itemView.setOnClickListener(v -> {

                // get the snapshot of the clicked document
                DocumentSnapshot clickedDoc = getSnapshots().getSnapshot(getAbsoluteAdapterPosition());

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                View view = LayoutInflater.from(context).inflate(R.layout.dialog_custom_mesa, null);
                EditText edtNewMesa = view.findViewById(R.id.edtNewMesa);
                FloatingActionButton fabOkNewMesa = view.findViewById(R.id.fabOkNewMesa);
                builder.setView(view);
                AlertDialog dialog = builder.create();

                fabOkNewMesa.setOnClickListener(v2 -> {

                    String newMesa = edtNewMesa.getText().toString().trim();
                    if (newMesa.isEmpty()) {
                        Toast.makeText(context, R.string.llenar_los_campos, Toast.LENGTH_SHORT).show();
                    } else {

                        // change the mesa value in the pedidos and cuenta in the fStore
                        App.executor.submit(new MesaInCuentaChanger(
                                Utils.getCurrentDate(),
                                getSnapshots().getSnapshot(getAbsoluteAdapterPosition()).getString(Utils.KEY_NAME),
                                newMesa));

                        // check if the new mesa name is already in the mesas collection at the Firestore
                        fStore.collection("mesas").get()
                                .addOnSuccessListener(App.executor, queryDocumentSnapshots -> {

                                    boolean isFixed = false;
                                    for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                                        
                                        if (Objects.equals(snapshot.getString(Utils.KEY_NAME), newMesa)) {

                                            // the table of the new name is in the collection, most probably one of the fixed ones
                                            snapshot.getReference().update("present", true);
                                            isFixed = true;

                                            if (clickedDoc.getBoolean("fixed")) {
                                                // if the table we are changing is one of the fixed ones, we change its presence status
                                                clickedDoc.getReference().update("present", false);
                                            } else {
                                                // the table we are changing was a customized one, we delete it from the collection
                                                clickedDoc.getReference().delete();
                                            }
                                        }
                                    }

                                    
                                    if (!isFixed) {

                                        // we are here because iteratation through the mesas collection didn't find the matching name
                                        // it means that administrator wants to assign a new customized table
                                        // so we add a new one to the collection
                                        fStore.collection("mesas").add(new Mesa(false, false, true, newMesa));
                                        
                                        if (clickedDoc.getBoolean("fixed")) {
                                            
                                            // the old one is one of the fixed, we change its presence status
                                            clickedDoc.getReference().update("present", false);
                                        } else {
                                            // the old one is customized, we can delete it
                                            clickedDoc.getReference().delete();
                                        }
                                    }
                                });

                        dialog.dismiss();
                    }
                });
                dialog.show();
            });

            itemView.setOnLongClickListener(v -> {
                getSnapshots().getSnapshot(getAbsoluteAdapterPosition()).getReference().update("present", false);
                return true;
            });
        }

        private void bind(Mesa mesa) {
            txtMesa.setText(mesa.getName());
            txtNombre.setText(R.string.cliente);
        }
    }
}
