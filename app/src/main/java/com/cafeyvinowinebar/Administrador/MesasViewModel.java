package com.cafeyvinowinebar.Administrador;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.cafeyvinowinebar.Administrador.POJOs.MesaEntity;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class MesasViewModel extends AndroidViewModel {

    private final MesasRepository repository;
    private final LiveData<List<MesaEntity>> mesas;


    public MesasViewModel(@NonNull Application application) {
        super(application);
        repository = new MesasRepository(application);
        mesas = repository.getAllMesas();
    }

    public void insert(MesaEntity mesa) {
        repository.insert(mesa);
    }

    public void delete(MesaEntity mesa) {
        repository.delete(mesa);
    }

    public void deleteByName(String mesa) {
        repository.deleteByName(mesa);
    }

    public LiveData<List<MesaEntity>> getMesas() {
        return mesas;
    }

    public LiveData<List<MesaEntity>> getPresentMesas() throws ExecutionException, InterruptedException {return repository.getPresentMesas();}

    public MesaEntity getMesaById(int id) throws ExecutionException, InterruptedException {return repository.getMesaByID(id);}
}
