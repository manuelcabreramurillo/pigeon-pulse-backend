package com.pigeonpulse.service;

import com.pigeonpulse.model.Palomar;
import com.pigeonpulse.model.Usuario;
import com.pigeonpulse.model.UsuarioPalomar;
import com.pigeonpulse.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PalomarService palomarService;

    @Autowired
    private UsuarioPalomarService usuarioPalomarService;

    public Optional<Usuario> findById(String id) throws ExecutionException, InterruptedException {
        return usuarioRepository.findById(id);
    }

    public Optional<Usuario> findByEmail(String email) throws ExecutionException, InterruptedException {
        return usuarioRepository.findByEmail(email);
    }

    public Optional<Usuario> findByGoogleId(String googleId) throws ExecutionException, InterruptedException {
        return usuarioRepository.findByGoogleId(googleId);
    }

    public String save(Usuario usuario) throws ExecutionException, InterruptedException {
        return usuarioRepository.save(usuario);
    }

    public void update(String id, Usuario usuario) throws ExecutionException, InterruptedException {
        usuarioRepository.update(id, usuario);
    }

    public void deleteById(String id) throws ExecutionException, InterruptedException {
        usuarioRepository.deleteById(id);
    }

    public Usuario createOrUpdateFromGoogle(String googleId, String email, String nombre) throws ExecutionException, InterruptedException {
        // First, try to find by Google ID
        Optional<Usuario> existingUser = findByGoogleId(googleId);
        if (existingUser.isPresent()) {
            Usuario user = existingUser.get();
            user.setEmail(email);
            user.setNombre(nombre);
            update(user.getId(), user);
            return user;
        }

        // If not found by Google ID, try to find by email (for invited users)
        Optional<Usuario> invitedUser = findByEmail(email);
        if (invitedUser.isPresent()) {
            Usuario user = invitedUser.get();
            // Update the invited user with Google data
            user.setNombre(nombre);
            user.setGoogleId(googleId);
            update(user.getId(), user);
            return user;
        }

        // User not found, create new one
        Usuario newUser = new Usuario(nombre, email, googleId);
        String id = save(newUser);
        newUser.setId(id);

        // Crear palomar por defecto
        Palomar defaultPalomar = palomarService.createDefaultPalomar(newUser);

        // Crear relación propietario
        usuarioPalomarService.createPropietarioRelation(newUser.getId(), defaultPalomar);

        return newUser;
    }

    public void cleanupFechaCreacionField() throws ExecutionException, InterruptedException {
        usuarioRepository.removeFieldFromAllDocuments("fechaCreacion");
    }
}