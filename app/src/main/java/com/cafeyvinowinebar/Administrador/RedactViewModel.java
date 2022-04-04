package com.cafeyvinowinebar.Administrador;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.cafeyvinowinebar.Administrador.POJOs.ProductEntity;
import com.cafeyvinowinebar.Administrador.POJOs.RedactEntity;

import java.util.List;

public class RedactViewModel extends AndroidViewModel {

    private final RedactRepo repo;
    private final LiveData<List<RedactEntity>> products;

    public RedactViewModel(@NonNull Application application) {
        super(application);
        repo = new RedactRepo(application);
        products = repo.getAllProducts();
    }

    public LiveData<List<RedactEntity>> getAllProducts() {
        return products;
    }

    public void redact(String name, long count) {repo.redact(name, count);}

    public void insert(RedactEntity product) {
        repo.insert(product);
    }

    public void deleteAll() {repo.deleteAll();}
}
