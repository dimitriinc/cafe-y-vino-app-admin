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
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.cafeyvinowinebar.Administrador.POJOs.CuentaCancelada;
import com.cafeyvinowinebar.Administrador.POJOs.CuentaItem;
import com.cafeyvinowinebar.Administrador.R;
import com.cafeyvinowinebar.Administrador.Utils;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

/**
 * Displays a list of all the bills canceled by the user
 * Shows the detailed list of the bill's products on expanding the bill
 */
public class AdapterHistory extends ListAdapter<CuentaCancelada, AdapterHistory.ViewHolder> {

    private final FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    private final Context context;

    public AdapterHistory(Context context) {
        super(DIFF_CALLBACK);
        this.context = context;
    }

    private static final DiffUtil.ItemCallback<CuentaCancelada> DIFF_CALLBACK = new DiffUtil.ItemCallback<CuentaCancelada>() {
        @Override
        public boolean areItemsTheSame(@NonNull CuentaCancelada oldItem, @NonNull CuentaCancelada newItem) {
            return oldItem.getTimestamp() == newItem.getTimestamp();
        }

        @Override
        public boolean areContentsTheSame(@NonNull CuentaCancelada oldItem, @NonNull CuentaCancelada newItem) {
            return oldItem.isExpanded() == newItem.isExpanded() &&
                    oldItem.getMetaDocId().equals(newItem.getMetaDocId()) &&
                    oldItem.getName().equals(newItem.getName()) &&
                    oldItem.getFecha().equals(newItem.getFecha()) &&
                    oldItem.getTotal().equals(newItem.getTotal()) &&
                    oldItem.getUserId().equals(newItem.getUserId());
        }
    };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_cuenta_cancelada, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CuentaCancelada cuenta = getItem(position);
        holder.bind(cuenta);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private final RelativeLayout lay2;
        private final TextView txtCuentaId, txtSum;
        private final ImageView imgExp;
        private final RecyclerView rec2;
        private final CardView parent;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            lay2 = itemView.findViewById(R.id.lay2Cancel);
            txtCuentaId = itemView.findViewById(R.id.txtCuentaIdCancel);
            txtSum = itemView.findViewById(R.id.txtSumCancel);
            imgExp = itemView.findViewById(R.id.imgExpCancel);
            ImageView imgCol = itemView.findViewById(R.id.imgColCancel);
            rec2 = itemView.findViewById(R.id.rec2Cancel);
            parent = itemView.findViewById(R.id.parentCuentaCancel);

            imgExp.setOnClickListener(v -> {
                CuentaCancelada cuenta = getItem(getAbsoluteAdapterPosition());
                cuenta.setExpanded(!cuenta.isExpanded);
                notifyItemChanged(getAbsoluteAdapterPosition());
            });

            imgCol.setOnClickListener(v -> {
                CuentaCancelada cuenta = getItem(getAbsoluteAdapterPosition());
                cuenta.setExpanded(!cuenta.isExpanded);
                notifyItemChanged(getAbsoluteAdapterPosition());
            });

        }

        /**
         * For each bill we want to display the list of its products
         * So we query for that list and when the card gets expanded, we display a nested RecyclerView
         */
        public void bind(CuentaCancelada cuenta) {

            CollectionReference collection = fStore.collection("cuentas")
                    .document(cuenta.getFecha())
                    .collection("cuentas_canceladas")
                    .document(cuenta.getMetaDocId())
                    .collection("cuenta");
            Query query = collection.orderBy(Utils.KEY_NAME);
            FirestoreRecyclerOptions<CuentaItem> options = new FirestoreRecyclerOptions.Builder<CuentaItem>()
                    .setQuery(query, CuentaItem.class)
                    .build();
            AdapterCuentasCanceladasNested adapter = new AdapterCuentasCanceladasNested(options);

            txtSum.setText(cuenta.getTotal());
            txtCuentaId.setText(cuenta.getFecha());

            if (cuenta.isExpanded) {
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
