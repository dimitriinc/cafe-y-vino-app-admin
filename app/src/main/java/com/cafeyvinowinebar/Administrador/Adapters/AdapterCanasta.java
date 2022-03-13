package com.cafeyvinowinebar.Administrador.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.cafeyvinowinebar.Administrador.POJOs.ProductEntity;
import com.cafeyvinowinebar.Administrador.ProductsViewModel;
import com.cafeyvinowinebar.Administrador.R;

import java.util.ArrayList;
import java.util.List;

public class AdapterCanasta extends ListAdapter<ProductEntity, AdapterCanasta.ViewHolder> {

    private final ProductsViewModel viewModel;

    public AdapterCanasta(ProductsViewModel viewModel) {
        super(DIFF_CALLBACK);
        this.viewModel = viewModel;
    }

    private static final DiffUtil.ItemCallback<ProductEntity> DIFF_CALLBACK = new DiffUtil.ItemCallback<ProductEntity>() {
        @Override
        public boolean areItemsTheSame(@NonNull ProductEntity oldItem, @NonNull ProductEntity newItem) {
            return oldItem.getName().equals(newItem.getName());
        }

        @Override
        public boolean areContentsTheSame(@NonNull ProductEntity oldItem, @NonNull ProductEntity newItem) {
            return oldItem.getCategory().equals(newItem.getCategory()) &&
                    oldItem.getCount() == newItem.getCount() &&
                    oldItem.getPrice() == newItem.getPrice();
        }
    };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_canasta, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ProductEntity product = getItem(position);
        holder.bind(product);
    }


    class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView txtCanastaItem, txtCanastaCount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtCanastaCount = itemView.findViewById(R.id.txtCanastaCount);
            txtCanastaItem = itemView.findViewById(R.id.txtCanastaItem);

            itemView.setOnClickListener(view -> {

                // on click we increment the existing product
                ProductEntity product = getItem(getAbsoluteAdapterPosition());
                long count = product.getCount();
                count++;
                viewModel.increment(product.getName(), count);
            });

            itemView.setOnLongClickListener(view -> {
                // on long click we decrement it
                // TODO: get rid of this one (?)
                try {
                    decrement();
                } catch (ArrayIndexOutOfBoundsException ignored) {}
                return true;
            });

            txtCanastaCount.setOnClickListener(view -> {
                // when clicking on the view that displays the count, we decrement the product
                // it's more usable then the long click
                try {
                    decrement();
                } catch (ArrayIndexOutOfBoundsException ignored) {}
            });
        }

        private void decrement() {
            ProductEntity product = getItem(getAbsoluteAdapterPosition());
            long count = product.getCount();
            if (count != 1) {
                count--;
                viewModel.increment(product.getName(), count);
            } else {

                // instead of decrementing to zero, we just delete the product
                viewModel.delete(product);
            }
        }

        public void bind(ProductEntity product) {
            txtCanastaItem.setText(product.getName());
            txtCanastaCount.setText(String.valueOf(product.getCount()));
        }

    }
}
