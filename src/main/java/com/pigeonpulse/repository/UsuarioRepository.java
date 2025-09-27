package com.pigeonpulse.repository;

import com.pigeonpulse.model.Usuario;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Repository
public class UsuarioRepository extends FirebaseRepository<Usuario> {

    @Override
    protected String getCollectionName() {
        return "usuarios_v2";
    }

    @Override
    protected Class<Usuario> getEntityClass() {
        return Usuario.class;
    }

    public Optional<Usuario> findByEmail(String email) throws ExecutionException, InterruptedException {
        return findByField("email", email).stream().findFirst();
    }

    public Optional<Usuario> findByGoogleId(String googleId) throws ExecutionException, InterruptedException {
        return findByField("googleId", googleId).stream().findFirst();
    }
}