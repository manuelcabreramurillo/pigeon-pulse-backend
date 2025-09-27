package com.pigeonpulse.service;

import com.pigeonpulse.model.Palomar;
import com.pigeonpulse.model.Usuario;
import com.pigeonpulse.repository.PalomarRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Service
public class PalomarService {

    @Autowired
    private PalomarRepository palomarRepository;

    public Optional<Palomar> findById(String id) throws ExecutionException, InterruptedException {
        return palomarRepository.findById(id);
    }

    public List<Palomar> findByPropietarioId(String propietarioId) throws ExecutionException, InterruptedException {
        return palomarRepository.findByPropietarioIdList(propietarioId);
    }

    public String save(Palomar palomar) throws ExecutionException, InterruptedException {
        return palomarRepository.save(palomar);
    }

    public void update(String id, Palomar palomar) throws ExecutionException, InterruptedException {
        palomarRepository.update(id, palomar);
    }

    public void deleteById(String id) throws ExecutionException, InterruptedException {
        palomarRepository.deleteById(id);
    }

    // Crear palomar por defecto para un usuario
    public Palomar createDefaultPalomar(Usuario usuario) throws ExecutionException, InterruptedException {
        Palomar palomar = new Palomar("Mi Palomar", usuario.getId());
        String id = save(palomar);
        palomar.setId(id);
        return palomar;
    }

    // Verificar si el usuario es propietario del palomar
    public boolean isPropietario(String palomarId, String usuarioId) throws ExecutionException, InterruptedException {
        Optional<Palomar> palomarOpt = findById(palomarId);
        return palomarOpt.isPresent() && palomarOpt.get().isPropietario(usuarioId);
    }
}