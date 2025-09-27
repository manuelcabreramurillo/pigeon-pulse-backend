package com.pigeonpulse.repository;

import com.pigeonpulse.model.UsuarioPalomar;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Repository
public class UsuarioPalomarRepository extends FirebaseRepository<UsuarioPalomar> {

    @Override
    protected String getCollectionName() {
        return "usuario_palomares";
    }

    @Override
    protected Class<UsuarioPalomar> getEntityClass() {
        return UsuarioPalomar.class;
    }

    public List<UsuarioPalomar> findByUsuarioId(String usuarioId) throws ExecutionException, InterruptedException {
        return findByField("usuario_id", usuarioId);
    }

    public List<UsuarioPalomar> findByPalomarId(String palomarId) throws ExecutionException, InterruptedException {
        return findByField("palomar_id", palomarId);
    }

    public Optional<UsuarioPalomar> findByUsuarioIdAndPalomarId(String usuarioId, String palomarId) throws ExecutionException, InterruptedException {
        System.out.println("UsuarioPalomarRepository: Finding relation for user: " + usuarioId + ", palomar: " + palomarId);
        List<UsuarioPalomar> userRelations = findByUsuarioId(usuarioId);
        System.out.println("UsuarioPalomarRepository: Found " + userRelations.size() + " relations for user");

        for (UsuarioPalomar relation : userRelations) {
            System.out.println("UsuarioPalomarRepository: Relation - usuarioId: " + relation.getUsuarioId() +
                             ", palomarId: " + relation.getPalomarId() + ", rol: " + relation.getRol());
        }

        List<UsuarioPalomar> results = userRelations.stream()
                .filter(up -> palomarId.equals(up.getPalomarId()))
                .toList();
        System.out.println("UsuarioPalomarRepository: Filtered results: " + results.size());
        return results.stream().findFirst();
    }

    public List<UsuarioPalomar> findByUsuarioIdAndRol(String usuarioId, String rol) throws ExecutionException, InterruptedException {
        return findByUsuarioId(usuarioId).stream()
                .filter(up -> rol.equals(up.getRol()))
                .toList();
    }
}