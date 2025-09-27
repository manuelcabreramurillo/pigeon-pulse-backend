package com.pigeonpulse.service;

import com.pigeonpulse.model.Palomar;
import com.pigeonpulse.model.UsuarioPalomar;
import com.pigeonpulse.repository.UsuarioPalomarRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Service
public class UsuarioPalomarService {

    @Autowired
    private UsuarioPalomarRepository usuarioPalomarRepository;

    public Optional<UsuarioPalomar> findById(String id) throws ExecutionException, InterruptedException {
        return usuarioPalomarRepository.findById(id);
    }

    public List<UsuarioPalomar> findByUsuarioId(String usuarioId) throws ExecutionException, InterruptedException {
        return usuarioPalomarRepository.findByUsuarioId(usuarioId);
    }

    public List<UsuarioPalomar> findByPalomarId(String palomarId) throws ExecutionException, InterruptedException {
        return usuarioPalomarRepository.findByPalomarId(palomarId);
    }

    public Optional<UsuarioPalomar> findByUsuarioIdAndPalomarId(String usuarioId, String palomarId) throws ExecutionException, InterruptedException {
        return usuarioPalomarRepository.findByUsuarioIdAndPalomarId(usuarioId, palomarId);
    }

    public String save(UsuarioPalomar usuarioPalomar) throws ExecutionException, InterruptedException {
        return usuarioPalomarRepository.save(usuarioPalomar);
    }

    public void update(String id, UsuarioPalomar usuarioPalomar) throws ExecutionException, InterruptedException {
        usuarioPalomarRepository.update(id, usuarioPalomar);
    }

    public void deleteById(String id) throws ExecutionException, InterruptedException {
        usuarioPalomarRepository.deleteById(id);
    }

    // Crear relación propietario al crear palomar
    public UsuarioPalomar createPropietarioRelation(String usuarioId, Palomar palomar) throws ExecutionException, InterruptedException {
        System.out.println("UsuarioPalomarService: Creating propietario relation for user: " + usuarioId + ", palomar: " + palomar.getId());
        UsuarioPalomar relacion = new UsuarioPalomar(usuarioId, palomar.getId(), "PROPIETARIO");
        String id = save(relacion);
        relacion.setId(id);
        System.out.println("UsuarioPalomarService: Created relation with ID: " + id);
        return relacion;
    }

    // Invitar colaborador a un palomar
    public UsuarioPalomar inviteColaborador(String usuarioId, String palomarId) throws ExecutionException, InterruptedException {
        UsuarioPalomar relacion = new UsuarioPalomar(usuarioId, palomarId, "COLABORADOR");
        String id = save(relacion);
        relacion.setId(id);
        return relacion;
    }

    // Verificar si usuario tiene acceso a palomar
    public boolean hasAccessToPalomar(String usuarioId, String palomarId) throws ExecutionException, InterruptedException {
        System.out.println("UsuarioPalomarService: Checking access for user: " + usuarioId + ", palomar: " + palomarId);
        Optional<UsuarioPalomar> relacion = findByUsuarioIdAndPalomarId(usuarioId, palomarId);
        boolean hasAccess = relacion.isPresent();
        System.out.println("UsuarioPalomarService: Has access: " + hasAccess);
        return hasAccess;
    }

    // Obtener rol del usuario en el palomar
    public Optional<String> getRolInPalomar(String usuarioId, String palomarId) throws ExecutionException, InterruptedException {
        Optional<UsuarioPalomar> relacion = findByUsuarioIdAndPalomarId(usuarioId, palomarId);
        return relacion.map(UsuarioPalomar::getRol);
    }

    // Verificar si usuario puede gestionar usuarios en el palomar
    public boolean canManageUsers(String usuarioId, String palomarId) throws ExecutionException, InterruptedException {
        Optional<String> rol = getRolInPalomar(usuarioId, palomarId);
        return rol.isPresent() && "PROPIETARIO".equals(rol.get());
    }
}