package com.cafeyvinowinebar.Administrador;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.cafeyvinowinebar.Administrador.Interfaces.ProductDao;
import com.cafeyvinowinebar.Administrador.POJOs.ProductEntity;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class ProductsRepository {

    private ProductDao productDao;
    private LiveData<List<ProductEntity>> products;
    ProductEntity product;

    public ProductsRepository(Application application) {
        ProductsDatabase database = ProductsDatabase.getInstance(application);
        productDao = database.productDao();
        products = productDao.getAllProducts();
    }

    public void insert(ProductEntity product) {
        App.executor.submit(new InsertProductRunnable(productDao, product));
    }

    public void update(ProductEntity product) {
        App.executor.submit(new UpdateProductRunnable(productDao, product));
    }

    public void delete(ProductEntity product) {
        App.executor.submit(new DeleteProductRunnable(productDao, product));
    }

    public void increment(String name, long count) {
        App.executor.submit(new IncrementProduct(productDao, name, count));
    }

    public void deleteAll() {
        App.executor.submit(new DeleteAllProductsRunnable(productDao));
    }

    public LiveData<List<ProductEntity>> getAllProducts() {
        return products;
    }

    public ProductEntity getProduct(String name) throws ExecutionException, InterruptedException {
        Future<ProductEntity> future = App.executor.submit(new GetProductCallable(productDao, name));
        return future.get();
    }

    private static class InsertProductRunnable implements Runnable {

        private ProductDao productDao;
        private ProductEntity product;
        private InsertProductRunnable(ProductDao productDao, ProductEntity product) {
            this.productDao = productDao;
            this.product = product;
        }

        @Override
        public void run() {
            productDao.insert(product);
        }
    }

    private static class UpdateProductRunnable implements Runnable {

        private ProductDao productDao;
        private ProductEntity product;
        private UpdateProductRunnable(ProductDao productDao, ProductEntity product) {
            this.productDao = productDao;
            this.product = product;
        }

        @Override
        public void run() {
            productDao.update(product);
        }
    }

    private static class DeleteProductRunnable implements Runnable {

        private ProductDao productDao;
        private ProductEntity product;
        private DeleteProductRunnable(ProductDao productDao, ProductEntity product) {
            this.productDao = productDao;
            this.product = product;
        }

        @Override
        public void run() {
            productDao.delete(product);
        }
    }

    private static class DeleteAllProductsRunnable implements Runnable {

        private ProductDao productDao;
        private DeleteAllProductsRunnable(ProductDao productDao) {
            this.productDao = productDao;
        }

        @Override
        public void run() {
            productDao.deleteAll();
        }
    }

    private class GetProductCallable implements Callable<ProductEntity> {

        private ProductDao productDao;
        private String name;

        public GetProductCallable(ProductDao productDao, String name) {
            this.productDao = productDao;
            this.name = name;
        }

        @Override
        public ProductEntity call() throws Exception {
            return productDao.getProduct(name);
        }
    }

    private class IncrementProduct implements Runnable {

        ProductDao productDao;
        String name;
        long count;

        public IncrementProduct(ProductDao productDao, String name, long count) {
            this.productDao = productDao;
            this.name = name;
            this.count = count;
        }

        @Override
        public void run() {
            productDao.increment(name, count);
        }
    }
}
