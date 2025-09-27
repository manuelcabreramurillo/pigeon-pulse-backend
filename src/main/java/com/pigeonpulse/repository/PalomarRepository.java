package com.pigeonpulse.repository;

import com.pigeonpulse.model.Palomar;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Repository
public class PalomarRepository extends FirebaseRepository<Palomar> {

    @Override
    protected String getCollectionName() {
        return "palomares";
    }

    @Override
    protected Class<Palomar> getEntityClass() {
        return Palomar.class;
    }

    public Optional<Palomar> findByPropietarioId(String propietarioId) throws ExecutionException, InterruptedException {
        return findByField("propietario_id", propietarioId).stream().findFirst();
    }

    public List<Palomar> findByPropietarioIdList(String propietarioId) throws ExecutionException, InterruptedException {
        return findByField("propietario_id", propietarioId);
    }

    public List<Palomar> findByNombre(String nombre) throws ExecutionException, InterruptedException {
        return findByField("nombre", nombre);
    }
}