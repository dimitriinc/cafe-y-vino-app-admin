package com.cafeyvinowinebar.Administrador.Adapters;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cafeyvinowinebar.Administrador.Fragments.PayTypePicker;
import com.cafeyvinowinebar.Administrador.POJOs.Cuenta;
import com.cafeyvinowinebar.Administrador.POJOs.CuentaItem;
import com.cafeyvinowinebar.Administrador.Interfaces.OnItemClickListener;
import com.cafeyvinowinebar.Administrador.Interfaces.OnItemLongClickListener;
import com.cafeyvinowinebar.Administrador.R;
import com.cafeyvinowinebar.Administrador.Utils;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class AdapterCuentas extends FirestoreRecyclerAdapter<Cuenta, AdapterCuentas.ViewHolder> {

    private final FirebaseFirestore fStore = FirebaseFirestore.getInstance();

    public String currentDate;
    private final Context context;
    private final Activity activity;
    public volatile double total;
    private OnItemClickListener addListener, redactListener;
    private OnItemLongClickListener longListener;
    public Handler handler;
    public FragmentManager manager;

    public AdapterCuentas(@NonNull FirestoreRecyclerOptions<Cuenta> options, Context context, Activity activity,
                          String currentDate, Handler handler, FragmentManager manager) {
        super(options);
        this.context = context;
        this.activity = activity;
        this.currentDate = currentDate;
        this.handler = handler;
        this.manager = manager;
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull Cuenta model) {
        holder.bind(model);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_cuenta, parent, false);
        return new ViewHolder(view);
    }

    public void cancel(int position) {

        // the method is called when admin swipes the list item
        // before canceling we must store the data about how the bill was paid for
        DocumentSnapshot snapshot = getSnapshots().getSnapshot(position);
        PayTypePicker fragment = new PayTypePicker(snapshot, currentDate, manager);
        fragment.show(manager, Utils.TAG);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private final RelativeLayout lay2;
        private final TextView txtCuentaId, txtSum;
        private final ImageView imgExp, imgCol;
        private final RecyclerView rec2;
        private final CardView parent;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            lay2 = itemView.findViewById(R.id.lay2);
            txtCuentaId = itemView.findViewById(R.id.txtCuentaId);
            txtSum = itemView.findViewById(R.id.txtSum);
            imgExp = itemView.findViewById(R.id.imgExp);
            imgCol = itemView.findViewById(R.id.imgCol);
            rec2 = itemView.findViewById(R.id.rec2);
            parent = itemView.findViewById(R.id.parentCuenta);
            FloatingActionButton fabAdd = itemView.findViewById(R.id.fabAddCustomItemCuenta);
            FloatingActionButton fabRedact = itemView.findViewById(R.id.fabRedactCuenta);

            itemView.setOnLongClickListener(v -> {
                int position = getAbsoluteAdapterPosition();
                if (position != RecyclerView.NO_POSITION && longListener != null) {
                    longListener.onItemLongClick(getSnapshots().getSnapshot(position), position, v);
                }
                return true;
            });

            fabAdd.setOnClickListener(v -> {
                int position = getAbsoluteAdapterPosition();
                if (position != RecyclerView.NO_POSITION && addListener != null) {
                    addListener.onItemClick(getSnapshots().getSnapshot(position), position, v);
                }
            });

            fabRedact.setOnClickListener(v -> {
                int position = getAbsoluteAdapterPosition();
                if (position != RecyclerView.NO_POSITION && redactListener != null) {
                    redactListener.onItemClick(getSnapshots().getSnapshot(position), position, v);
                }
            });

        }

        public void bind(Cuenta model) {

            // the id of a cuenta meta doc is the Uid of the user to whom this bill belongs
            String userId = getSnapshots().getSnapshot(getAbsoluteAdapterPosition()).getId();

            if (model.getName().equals("Cliente")) {
                // no name on display
                txtCuentaId.setText(context.getString(R.string.custom_user_title, model.getMesa()));
            } else {
                // we want only the first name of the user on display
                String[] userNames = model.getName().split(" ");
                txtCuentaId.setText(context.getString(R.string.pedido_cuenta_title, userNames[0], model.getMesa()));
            }

            // expand the list item
            imgExp.setOnClickListener(v -> {
                model.setExpanded(!model.isExpanded());
                notifyItemChanged(getAbsoluteAdapterPosition());
            });

            // collapse the list item
            imgCol.setOnClickListener(v -> {
                model.setExpanded(!model.isExpanded());
                notifyItemChanged(getAbsoluteAdapterPosition());
            });

            // in the expanded state the list item exposes a nested recycler view that displays the products inside the bill
            Query query = fStore.collection("cuentas")
                    .document(currentDate)
                    .collection("cuentas corrientes")
                    .document(userId)
                    .collection("cuenta");

            FirestoreRecyclerOptions<CuentaItem> options = new FirestoreRecyclerOptions.Builder<CuentaItem>()
                    .setQuery(query, CuentaItem.class)
                    .build();
            AdapterCuentasNested adapter = new AdapterCuentasNested(options);


            if (model.isExpanded()) {

                rec2.setAdapter(adapter);
                rec2.setLayoutManager(new LinearLayoutManager(context));

                // apart from setting up the adapter, we must calculate the total sum of the bill
                // and make it responsive to changes within the bill
                // to do that, we listen to the cuenta collection and iterate through its products an add their total price (count * unit price)
                // to a volatile member var 'total'
                // don't know why exactly, but if i don't synchronize the thing, it doesn't compute correctly
                query.addSnapshotListener(activity, (value, error) -> {
                    if (error != null) {
                        return;
                    }
                    total = 0.00;
                    assert value != null;
                    synchronized (this) {
                        for (QueryDocumentSnapshot doc : value) {
                            long itemTotal = doc.getLong(Utils.TOTAL);
                            total += itemTotal;
                        }
                    }
                    txtSum.setText(String.valueOf(total));
                });
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

    public void setOnAddClickListener(OnItemClickListener addListener) {
        this.addListener = addListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener longListener) {
        this.longListener = longListener;
    }

    public void setOnRedactClickListener(OnItemClickListener redactListener) {this.redactListener = redactListener;}
}
