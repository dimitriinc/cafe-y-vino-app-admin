package com.cafeyvinowinebar.Administrador;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cafeyvinowinebar.Administrador.POJOs.CuentaCancelada;
import com.cafeyvinowinebar.Administrador.POJOs.RedactChange;
import com.cafeyvinowinebar.Administrador.POJOs.Redaction;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Displays a list of redaction made on the chosen date
 * And for each redaction there is a nested list of concrete changes for each product
 * So the class also contains two adapters with their respective view holders for each recycler view
 */
public class RedactHIstoryActivity extends AppCompatActivity {

    private final FirebaseFirestore fStore = FirebaseFirestore.getInstance();

    private String date;
    private RecyclerView recRedactHistory;
    private RedactHistoryAdapter adapter;

    public static Intent newIntent(Context context, String date) {
        Intent i = new Intent(context, RedactHIstoryActivity.class);
        i.putExtra(Utils.KEY_DATE, date);
        return i;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_redact_history);

        date = getIntent().getStringExtra(Utils.KEY_DATE);
        FloatingActionButton fabHome = findViewById(R.id.fabRedactHome);
        recRedactHistory = findViewById(R.id.recRedactHistory);

        fabHome.setOnClickListener(v -> startActivity(new Intent(getBaseContext(), MainActivity.class)));

        setupRedactAdapter();


    }

    private void setupRedactAdapter() {

        Query query = fStore.collection("cambios").document(date).collection("cambios")
                .orderBy(Utils.TIMESTAMP);

        FirestoreRecyclerOptions<Redaction> options = new FirestoreRecyclerOptions.Builder<Redaction>()
                .setQuery(query, Redaction.class)
                .build();

        adapter = new RedactHistoryAdapter(options);
        recRedactHistory.setLayoutManager(new LinearLayoutManager(this));
        recRedactHistory.setAdapter(adapter);
    }

    /**
     * Adapter for the main list of redactions
     * Displays the name of client, their table, a nested recycler view of changes, and a comment
     */
    private class RedactHistoryAdapter extends FirestoreRecyclerAdapter<Redaction, ViewHolder> {

        public RedactHistoryAdapter(@NonNull FirestoreRecyclerOptions<Redaction> options) {
            super(options);
        }

        @Override
        protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull Redaction model) {
            holder.bind(model);
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_redact_history, parent, false);
            return new ViewHolder(view);
        }
    }

    private class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView txtTitle, txtComment;
        private final RecyclerView recChanges;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.txtRedactHistoryTitle);
            txtComment = itemView.findViewById(R.id.txtRedactComment);
            recChanges = itemView.findViewById(R.id.recRedactChanges);
        }

        private void bind(Redaction model) {
            txtTitle.setText(getString(R.string.redaction_title, model.getUserName(), model.getMesa()));
            txtComment.setText(model.getComment());

            // the details of the redaction are stored as a map, with each key-value pair representing one change
            // so to display those changes as a recycler view, we need to convert the map to a list
            Map<String, String> modelChanges = model.getChanges();
            List<RedactChange> changes = modelChanges.entrySet().stream().map(e -> new RedactChange(e.getKey(), e.getValue()))
                    .collect(Collectors.toList());

            RedactChangesAdapter changesAdapter = new RedactChangesAdapter();
            changesAdapter.setItems(changes);
            recChanges.setLayoutManager(new LinearLayoutManager(getBaseContext()));
            recChanges.setAdapter(changesAdapter);
            recChanges.setHasFixedSize(true);

        }

        /**
         * Adapter for the nested recycler view, displaying a list of changes in their redaction
         * Since it's a read-only recycler view, we only need a standard adapter, we don't extend the ListAdapter
         */
        private class RedactChangesAdapter extends RecyclerView.Adapter<RedactChangesAdapter.ChangesViewHolder> {

            List<RedactChange> changes = new ArrayList<>();

            @NonNull
            @Override
            public ChangesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_consumo, parent, false);
                return new ChangesViewHolder(view);
            }

            @Override
            public void onBindViewHolder(@NonNull ChangesViewHolder holder, int position) {
                holder.bind(changes.get(position));
            }

            @Override
            public int getItemCount() {
                return changes.size();
            }

            @SuppressLint("NotifyDataSetChanged")
            public void setItems(List<RedactChange> changes) {
                this.changes = changes;
                notifyDataSetChanged();
            }

            private class ChangesViewHolder extends RecyclerView.ViewHolder {

                private final TextView txtName, txtCount;

                public ChangesViewHolder(@NonNull View itemView) {
                    super(itemView);
                    txtName = itemView.findViewById(R.id.txtConsumoName);
                    txtCount = itemView.findViewById(R.id.txtConsumoCount);
                }

                private void bind(RedactChange model) {
                    txtName.setText(model.getName());
                    txtCount.setText(model.getCount());
                }
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}