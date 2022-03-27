package com.cafeyvinowinebar.Administrador;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.cafeyvinowinebar.Administrador.Interfaces.RedactDao;
import com.cafeyvinowinebar.Administrador.POJOs.RedactEntity;

import java.util.List;

public class RedactRepo {

    private final RedactDao redactDao;
    private final LiveData<List<RedactEntity>> products;

    public RedactRepo(Application application) {
        RedactDatabase database = RedactDatabase.getInstance(application);
        redactDao = database.redactDao();
        products = redactDao.getAllProducts();
    }

    public LiveData<List<RedactEntity>> getAllProducts() {
        return products;
    }

    public void redact(String name, long count) {
        App.executor.submit(new RedactRunnable(redactDao, name, count));
    }

    public void insert(RedactEntity product) {
        App.executor.submit(new InsertRunnable(redactDao, product));
    }

    public void deleteAll() {
        App.executor.submit(new DeleteAllRunnable(redactDao));
    }

    private static class RedactRunnable implements Runnable {

        RedactDao redactDao;
        String name;
        long count;

        public RedactRunnable(RedactDao redactDao, String name, long count) {
            this.redactDao = redactDao;
            this.name = name;
            this.count = count;
        }

        @Override
        public void run() {
            redactDao.redact(name, count);
        }
    }

    private static class InsertRunnable implements Runnable {
        RedactDao redactDao;
        RedactEntity product;

        public InsertRunnable(RedactDao redactDao, RedactEntity product) {
            this.redactDao = redactDao;
            this.product = product;
        }

        @Override
        public void run() {
            redactDao.insert(product);
        }
    }

    private static class DeleteAllRunnable implements Runnable {
        RedactDao redactDao;

        public DeleteAllRunnable(RedactDao redactDao) {
            this.redactDao = redactDao;
        }

        @Override
        public void run() {
            redactDao.deleteAll();
        }
    }
}
