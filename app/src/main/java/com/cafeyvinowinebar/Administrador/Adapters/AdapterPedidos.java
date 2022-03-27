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

import com.cafeyvinowinebar.Administrador.App;
import com.cafeyvinowinebar.Administrador.Interfaces.OnItemClickListener;
import com.cafeyvinowinebar.Administrador.POJOs.ItemShort;
import com.cafeyvinowinebar.Administrador.POJOs.Mesa;
import com.cafeyvinowinebar.Administrador.Interfaces.OnItemLongClickListener;
import com.cafeyvinowinebar.Administrador.R;
import com.cafeyvinowinebar.Administrador.Runnables.ItemPedidoDecrementor;
import com.cafeyvinowinebar.Administrador.Runnables.PedidoToCuentaMover;
import com.cafeyvinowinebar.Administrador.Utils;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class AdapterPedidos extends FirestoreRecyclerAdapter<Mesa, AdapterPedidos.PedidoOneViewHolder> {

    private final FirebaseFirestore fStore = FirebaseFirestore.getInstance();

    private final Context context;
    public String currentDate;
    private OnItemLongClickListener longListener;
    private OnItemClickListener listener, redactListener;
    public String mode;

    public AdapterPedidos(@NonNull FirestoreRecyclerOptions<Mesa> options, Context context,
                          String currentDate, String mode) {
        super(options);
        this.context = context;
        this.currentDate = currentDate;
        this.mode = mode;

    }

    @Override
    protected void onBindViewHolder(@NonNull PedidoOneViewHolder holder, int position, @NonNull Mesa model) {
        holder.bind(model, position);
    }

    @NonNull
    @Override
    public PedidoOneViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_pedidos, parent, false);
        return new PedidoOneViewHolder(view);
    }

    /**
     * Moves the products in the query to a cuenta collection with a background task
     */
    public void moveToCuenta(int position) {
        DocumentSnapshot snapshot = getSnapshots().getSnapshot(position);
        App.executor.submit(new PedidoToCuentaMover(snapshot, currentDate, mode));
    }

    class PedidoOneViewHolder extends RecyclerView.ViewHolder {

        private final RelativeLayout layDown;
        private final TextView txtPedidoId;
        private final ImageView imgOne, imgTwo;
        private final CardView parent;
        private final RecyclerView downRecView;

        public PedidoOneViewHolder(@NonNull View itemView) {
            super(itemView);
            layDown = itemView.findViewById(R.id.layDown);
            txtPedidoId = itemView.findViewById(R.id.txtPedidoId);
            imgOne = itemView.findViewById(R.id.imgOne);
            imgTwo = itemView.findViewById(R.id.imgTwo);
            parent = itemView.findViewById(R.id.parentPedidos);
            downRecView = itemView.findViewById(R.id.downRecView);
            FloatingActionButton fabAdd = itemView.findViewById(R.id.fabAddCustomItem);
            FloatingActionButton fabRedact = itemView.findViewById(R.id.fabRedactPedido);

            itemView.setOnLongClickListener(v -> {
                int position = getAbsoluteAdapterPosition();
                if (position != RecyclerView.NO_POSITION && longListener != null) {
                    longListener.onItemLongClick(getSnapshots().getSnapshot(position), position, v);
                }
                return true;
            });

            fabAdd.setOnClickListener(v -> {
                int position = getAbsoluteAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(getSnapshots().getSnapshot(position), position, v);
                }
            });

            fabRedact.setOnClickListener(v -> {
                int position = getAbsoluteAdapterPosition();
                if (position != RecyclerView.NO_POSITION && redactListener != null) {
                    redactListener.onItemClick(getSnapshots().getSnapshot(position), position, v);
                }
            });
        }

        /**
         * In the expanded mode the list item displays a nested recycler view containing the pedido collection
         */
        public void bind(Mesa model, int position) {
            Query query;

            txtPedidoId.setText(context.getString(R.string.pedido_title, model.getUser(), model.getMesa()));

            imgOne.setOnClickListener(v -> {
                model.setExpanded(!model.isExpanded());
                notifyItemChanged(getAbsoluteAdapterPosition());
            });
            imgTwo.setOnClickListener(v -> {
                model.setExpanded(!model.isExpanded());
                notifyItemChanged(getAbsoluteAdapterPosition());
            });


            // get the collection reference by getting the path of the meta doc and concatenating the string to it
            CollectionReference collection = fStore.collection(getSnapshots().getSnapshot(position).getReference().getPath() + "/pedido");

            // depending on the 'mode' value we display different sets of products in the collection
            switch (mode) {
                case Utils.TODO:

                    // display all the collection
                    query = collection.orderBy(Utils.KEY_NAME);
                    break;
                case Utils.BARRA:
                    // display only the bar products of the collection
                    query = collection
                            .whereEqualTo(Utils.KEY_CATEGORY, Utils.BARRA)
                            .orderBy(Utils.KEY_NAME);
                    break;
                case Utils.COCINA:

                    // display only the kitchen products of the collection
                    query = collection
                            .whereEqualTo(Utils.KEY_CATEGORY, Utils.COCINA)
                            .orderBy(Utils.KEY_NAME);
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + mode);
            }

            FirestoreRecyclerOptions<ItemShort> options = new FirestoreRecyclerOptions.Builder<ItemShort>()
                    .setQuery(query, ItemShort.class)
                    .build();

            AdapterPedidoNested adapter = new AdapterPedidoNested(options);


            // when admin expands the list item we set adapter on the recycler view, and start listening
            // we stop listening when admin collapses the list item
            if (model.isExpanded()) {
                downRecView.setAdapter(adapter);
                downRecView.setLayoutManager(new LinearLayoutManager(context));

                // with a click on a count card, admin can increment the count value of the product
                adapter.setIncrementor((snapshot, position1, view) -> snapshot.getReference().update(Utils.KEY_COUNT, FieldValue.increment(1)));

                // with a click on a name card, admin decrements the count value of the product
                adapter.setDecremenator(((snapshot, position1, view) -> App.executor.submit(new ItemPedidoDecrementor(snapshot))));

                adapter.startListening();
                TransitionManager.beginDelayedTransition(parent);
                layDown.setVisibility(View.VISIBLE);
                imgOne.setVisibility(View.GONE);
            } else {
                adapter.stopListening();
                TransitionManager.beginDelayedTransition(parent);
                layDown.setVisibility(View.GONE);
                imgOne.setVisibility(View.VISIBLE);
            }
        }
    }

    public void setOnItemLongClickListener(OnItemLongClickListener longListener) {
        this.longListener = longListener;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setOnRedactClickListener(OnItemClickListener redactListener) {
        this.redactListener = redactListener;
    }
}
