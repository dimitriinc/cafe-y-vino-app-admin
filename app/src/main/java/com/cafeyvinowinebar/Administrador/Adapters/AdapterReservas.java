package com.cafeyvinowinebar.Administrador.Adapters;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cafeyvinowinebar.Administrador.App;
import com.cafeyvinowinebar.Administrador.Interfaces.OnItemClickListener;
import com.cafeyvinowinebar.Administrador.Interfaces.OnItemLongClickListener;
import com.cafeyvinowinebar.Administrador.R;
import com.cafeyvinowinebar.Administrador.POJOs.Reserva;
import com.cafeyvinowinebar.Administrador.Utils;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdapterReservas extends FirestoreRecyclerAdapter<Reserva, AdapterReservas.ViewHolder> {

    private final FirebaseFirestore fStore = FirebaseFirestore.getInstance();

    private final String date;
    private final String part;
    private final Context context;
    private OnItemClickListener listener;
    private OnItemLongClickListener longListener;
    final Handler mainHandler;

    public AdapterReservas(@NonNull FirestoreRecyclerOptions<Reserva> options, String date, String part, Context context, Handler mainHandler) {
        super(options);
        this.date = date;
        this.part = part;
        this.mainHandler = mainHandler;
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull AdapterReservas.ViewHolder holder, int position, @NonNull Reserva model) {
        String mesa = getSnapshots().getSnapshot(position).getId();
        holder.bind(mesa);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_reserva_model, parent, false);
        return new ViewHolder(view);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView txtModel;
        private final RelativeLayout parent;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtModel = itemView.findViewById(R.id.txtModel);
            parent = itemView.findViewById(R.id.modelParent);

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

        public void bind(String mesa) {

            txtModel.setText(mesa);

            // we need to set a background color of the view depending on the state of reservation
            // for that we need to access the document with the id of the table in question in the reservas collection
            fStore.collection("reservas")
                    .document(date)
                    .collection(part)
                    .document(mesa)
                    .get()
                    .addOnSuccessListener(App.executor, snapshot -> {

                        if (snapshot.exists()) {

                            // first we check the 'arrived' state, if its true we set a greenish color
                            if (snapshot.getBoolean(Utils.KEY_LLEGADO)) {
                                mainHandler.post(() -> parent.setBackgroundColor(context.getColor(R.color.llegado_light)));
                            }

                            // if it's not 'arrived', it's either 'confirmed' or not
                            else if (snapshot.getBoolean(Utils.KEY_CONFIRMADO)) {
                                // if it's confirmed we set a pinkish color
                                mainHandler.post(() -> parent.setBackgroundColor(context.getColor(R.color.regal_light)));
                            } else {
                                // if it's a request we set a beige color
                                mainHandler.post(() -> parent.setBackgroundColor(context.getColor(R.color.beige)));
                            }
                        }
                    });
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener longListener) {
        this.longListener = longListener;
    }
}
