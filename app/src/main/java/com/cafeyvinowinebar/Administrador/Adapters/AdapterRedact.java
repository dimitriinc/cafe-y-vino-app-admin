package com.cafeyvinowinebar.Administrador.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.cafeyvinowinebar.Administrador.Fragments.Redact;
import com.cafeyvinowinebar.Administrador.POJOs.ItemShort;
import com.cafeyvinowinebar.Administrador.POJOs.RedactEntity;
import com.cafeyvinowinebar.Administrador.R;
import com.cafeyvinowinebar.Administrador.RedactViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class AdapterRedact extends ListAdapter<RedactEntity, AdapterRedact.ViewHolder> {

    private final RedactViewModel viewModel;

    public AdapterRedact(RedactViewModel viewModel) {
        super(DIFF_CALLBACK);
        this.viewModel = viewModel;
    }

    private static final DiffUtil.ItemCallback<RedactEntity> DIFF_CALLBACK = new DiffUtil.ItemCallback<RedactEntity>() {
        @Override
        public boolean areItemsTheSame(@NonNull RedactEntity oldItem, @NonNull RedactEntity newItem) {
            return oldItem.getName().equals(newItem.getName());
        }

        @Override
        public boolean areContentsTheSame(@NonNull RedactEntity oldItem, @NonNull RedactEntity newItem) {
            return oldItem.getCategory().equals(newItem.getCategory()) &&
                    oldItem.getPrice() == newItem.getPrice() &&
                    oldItem.getCount() == newItem.getCount() &&
                    oldItem.getTotal() == newItem.getTotal();
        }
    };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_redact, parent, false);
        return new AdapterRedact.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView txtCount, txtName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtCount = itemView.findViewById(R.id.txtRedactCount);
            txtName = itemView.findViewById(R.id.txtRedactName);
            FloatingActionButton fabIncrement = itemView.findViewById(R.id.fabRedactIncrement);
            FloatingActionButton fabDecrement = itemView.findViewById(R.id.fabRedactDecrement);

            fabIncrement.setOnClickListener(v -> {
                RedactEntity product = getItem(getAbsoluteAdapterPosition());
                viewModel.redact(product.getName(), product.getCount() + 1);
            });

            fabDecrement.setOnClickListener(v -> {
                RedactEntity product = getItem(getAbsoluteAdapterPosition());
                if (product.getCount() > 0) {
                    viewModel.redact(product.getName(), product.getCount() - 1);
                }
            });
        }



        public void bind(RedactEntity product) {
            txtCount.setText(String.valueOf(product.getCount()));
            txtName.setText(product.getName());
        }
    }
}
