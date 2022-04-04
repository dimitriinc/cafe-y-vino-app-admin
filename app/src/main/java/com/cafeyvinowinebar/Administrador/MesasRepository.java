package com.cafeyvinowinebar.Administrador;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.cafeyvinowinebar.Administrador.Interfaces.MesaDao;
import com.cafeyvinowinebar.Administrador.POJOs.MesaEntity;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class MesasRepository {

    private final MesaDao mesaDao;
    private final LiveData<List<MesaEntity>> mesas;

    public MesasRepository(Application application) {
        MesasDatabase database = MesasDatabase.getInstance(application);
        mesaDao = database.mesaDao();
        mesas = mesaDao.getAllMesas();
    }

    public void insert(MesaEntity  mesa) {
        App.executor.submit(new InsertMesaRunnable(mesaDao, mesa));
    }

    public void delete(MesaEntity mesa) {
        App.executor.submit(new DeleteMesaRunnable(mesaDao, mesa));
    }

    public void deleteByName(String mesa) {
        App.executor.submit(new DeleteByNameRunnable(mesaDao, mesa));
    }

    public LiveData<List<MesaEntity>> getAllMesas() {
        return mesas;
    }

    public LiveData<List<MesaEntity>> getPresentMesas() throws ExecutionException, InterruptedException {
        Future<LiveData<List<MesaEntity>>> future = App.executor.submit(new GetPresentMesasCallable(mesaDao));
        return future.get();
    }

    public MesaEntity getMesaByID(int id) throws ExecutionException, InterruptedException {
        Future<MesaEntity> future = App.executor.submit(new GetMesaCallable(mesaDao, id));
        return future.get();
    }

    public void setPresence(int id, boolean isPresent) {
        App.executor.submit(new PresenceSetter(mesaDao, id, isPresent));
    }

    private static class InsertMesaRunnable implements Runnable {

        private final MesaDao mesaDao;
        private final MesaEntity mesa;
        private InsertMesaRunnable(MesaDao mesaDao, MesaEntity mesa) {
            this.mesaDao = mesaDao;
            this.mesa = mesa;
        }

        @Override
        public void run() {
            mesaDao.insert(mesa);
        }
    }

    private static class DeleteMesaRunnable implements Runnable {

        private final MesaDao mesaDao;
        private final MesaEntity mesa;
        private DeleteMesaRunnable(MesaDao mesaDao, MesaEntity mesa) {
            this.mesaDao = mesaDao;
            this.mesa = mesa;
        }

        @Override
        public void run() {
            mesaDao.delete(mesa);
        }
    }

    private static class DeleteByNameRunnable implements Runnable {

        private final MesaDao mesaDao;
        private final String mesa;

        public DeleteByNameRunnable(MesaDao mesaDao, String mesa) {
            this.mesaDao = mesaDao;
            this.mesa = mesa;
        }

        @Override
        public void run() {
            mesaDao.deleteByName(mesa);
        }
    }

    /**
     * gets all the custom tables that are occupied (have pedidos or cuentas assigned to them)
     */
    private static class GetPresentMesasCallable implements Callable<LiveData<List<MesaEntity>>> {

        private final MesaDao mesaDao;

        public GetPresentMesasCallable(MesaDao mesaDao) {
            this.mesaDao = mesaDao;
        }

        @Override
        public LiveData<List<MesaEntity>> call() throws Exception {
            return mesaDao.getPresentMesas();
        }
    }

    private static class GetMesaCallable implements Callable<MesaEntity> {

        private final MesaDao mesaDao;
        private final int id;

        public GetMesaCallable(MesaDao mesaDao, int id) {
            this.mesaDao = mesaDao;
            this.id = id;
        }

        @Override
        public MesaEntity call() throws Exception {
            return mesaDao.getMesaById(id);
        }
    }

    private static class PresenceSetter implements Runnable {

        private final MesaDao mesaDao;
        private final int id;
        private final boolean isPresent;

        public PresenceSetter(MesaDao mesaDao, int id, boolean isPresent) {
            this.mesaDao = mesaDao;
            this.id = id;
            this.isPresent = isPresent;
        }

        @Override
        public void run() {
            mesaDao.setPresence(id, isPresent);
        }
    }
}
