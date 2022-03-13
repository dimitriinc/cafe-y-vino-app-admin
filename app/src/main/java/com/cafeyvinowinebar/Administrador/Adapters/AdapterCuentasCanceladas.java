package com.cafeyvinowinebar.Administrador.Adapters;

import android.content.Context;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cafeyvinowinebar.Administrador.POJOs.CuentaCancelada;
import com.cafeyvinowinebar.Administrador.POJOs.CuentaItem;
import com.cafeyvinowinebar.Administrador.R;
import com.cafeyvinowinebar.Administrador.Utils;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class AdapterCuentasCanceladas extends FirestoreRecyclerAdapter<CuentaCancelada, AdapterCuentasCanceladas.ViewHolder> {

    private final FirebaseFirestore fStore = FirebaseFirestore.getInstance();

    private final Context context;

    public AdapterCuentasCanceladas(@NonNull FirestoreRecyclerOptions<CuentaCancelada> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull AdapterCuentasCanceladas.ViewHolder holder, int position, @NonNull CuentaCancelada model) {
        holder.bind(model);
    }

    @NonNull
    @Override
    public AdapterCuentasCanceladas.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_cuenta_cancelada, parent, false);
        return new ViewHolder(view);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private final RelativeLayout lay2;
        private final TextView txtCuentaId, txtSum;
        private final ImageView imgExp, imgCol;
        private final RecyclerView rec2;
        private final CardView parent;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            lay2 = itemView.findViewById(R.id.lay2Cancel);
            txtCuentaId = itemView.findViewById(R.id.txtCuentaIdCancel);
            txtSum = itemView.findViewById(R.id.txtSumCancel);
            imgExp = itemView.findViewById(R.id.imgExpCancel);
            imgCol = itemView.findViewById(R.id.imgColCancel);
            rec2 = itemView.findViewById(R.id.rec2Cancel);
            parent = itemView.findViewById(R.id.parentCuentaCancel);
        }

        public void bind(CuentaCancelada model) {

            txtSum.setText(model.getTotal());
            txtCuentaId.setText(model.getName());

            imgExp.setOnClickListener(v -> {
                model.setExpanded(!model.isExpanded());
                notifyItemChanged(getAbsoluteAdapterPosition());
            });
            imgCol.setOnClickListener(v -> {
                model.setExpanded(!model.isExpanded());
                notifyItemChanged(getAbsoluteAdapterPosition());
            });

            CollectionReference collection = fStore.collection(getSnapshots()
                    .getSnapshot(getAbsoluteAdapterPosition())
                    .getReference()
                    .getPath() + "/cuenta");

            Query query = collection.orderBy(Utils.KEY_NAME);
            FirestoreRecyclerOptions<CuentaItem> options = new FirestoreRecyclerOptions.Builder<CuentaItem>()
                    .setQuery(query, CuentaItem.class)
                    .build();
            AdapterCuentasCanceladasNested adapter = new AdapterCuentasCanceladasNested(options);

            if (model.isExpanded()) {
                rec2.setAdapter(adapter);
                rec2.setLayoutManager(new LinearLayoutManager(context));
                adapter.startListening();
                TransitionManager.beginDelayedTransition(parent);
                lay2.setVisibility(View.VISIBLE);
                imgExp.setVisibility(View.GONE);
            } else {
                adapter.stopListening();
                TransitionManager.beginDelayedTransition(parent);
                lay2.setVisibility(View.GONE);
                imgExp.setVisibility(View.VISIBLE);
            }
        }
    }

}
