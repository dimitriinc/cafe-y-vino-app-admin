package com.cafeyvinowinebar.Administrador;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.cafeyvinowinebar.Administrador.Interfaces.ProductDao;
import com.cafeyvinowinebar.Administrador.POJOs.ProductEntity;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class ProductsViewModel extends AndroidViewModel {

    private final ProductsRepository repo;
    private final LiveData<List<ProductEntity>> products;

    public ProductsViewModel(@NonNull Application application) {
        super(application);
        repo = new ProductsRepository(application);
        products = repo.getAllProducts();
    }

    public LiveData<List<ProductEntity>> getProducts() {
        return products;
    }

    public void insert(ProductEntity product) {
        repo.insert(product);
    }

    public void update(ProductEntity product) {
        repo.update(product);
    }

    public void delete(ProductEntity product) {
        repo.delete(product);
    }

    public void increment(String name, long count) {
        repo.increment(name, count);
    }

    public void deleteAllProducts() {
        repo.deleteAll();
    }

    public ProductEntity getProduct (String name) throws ExecutionException, InterruptedException {
        return repo.getProduct(name);
    }
}
