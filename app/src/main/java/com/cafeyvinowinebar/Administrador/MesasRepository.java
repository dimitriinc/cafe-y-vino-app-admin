package com.cafeyvinowinebar.Administrador;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import com.cafeyvinowinebar.Administrador.Interfaces.MesaDao;
import com.cafeyvinowinebar.Administrador.POJOs.MesaEntity;

import java.util.List;

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
}
