package com.pigeonpulse.repository;

import com.pigeonpulse.model.Paloma;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Repository
public class PalomaRepository extends FirebaseRepository<Paloma> {

    @Override
    protected String getCollectionName() {
        return "palomas_v2";
    }

    @Override
    protected Class<Paloma> getEntityClass() {
        return Paloma.class;
    }

    public Optional<Paloma> findByAnillo(String anillo) throws ExecutionException, InterruptedException {
        return findByField("anillo", anillo).stream().findFirst();
    }

    public List<Paloma> findByPalomarId(String palomarId) throws ExecutionException, InterruptedException {
        return findByField("palomarId", palomarId);
    }

    public List<Paloma> findByEstado(String estado) throws ExecutionException, InterruptedException {
        return findByField("estado", estado);
    }

    public List<Paloma> findBySexo(String sexo) throws ExecutionException, InterruptedException {
        return findByField("sexo", sexo);
    }

    public List<Paloma> findByLinea(String linea) throws ExecutionException, InterruptedException {
        return findByField("linea", linea);
    }

    public List<Paloma> findByPadre(String padreAnillo) throws ExecutionException, InterruptedException {
        return findByField("padre", padreAnillo);
    }

    public List<Paloma> findByMadre(String madreAnillo) throws ExecutionException, InterruptedException {
        return findByField("madre", madreAnillo);
    }
}