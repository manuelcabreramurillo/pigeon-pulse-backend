package com.pigeonpulse.service;

import com.pigeonpulse.model.Paloma;
import com.pigeonpulse.repository.PalomaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Service
public class PalomaService {

    @Autowired
    private PalomaRepository palomaRepository;

    public Optional<Paloma> findById(String id) throws ExecutionException, InterruptedException {
        return palomaRepository.findById(id);
    }

    public Optional<Paloma> findByAnillo(String anillo) throws ExecutionException, InterruptedException {
        return palomaRepository.findByAnillo(anillo);
    }

    public List<Paloma> findByPalomarId(String palomarId) throws ExecutionException, InterruptedException {
        return palomaRepository.findByPalomarId(palomarId);
    }

    public List<Paloma> findByEstado(String estado) throws ExecutionException, InterruptedException {
        return palomaRepository.findByEstado(estado);
    }

    public List<Paloma> findBySexo(String sexo) throws ExecutionException, InterruptedException {
        return palomaRepository.findBySexo(sexo);
    }

    public List<Paloma> findByLinea(String linea) throws ExecutionException, InterruptedException {
        return palomaRepository.findByLinea(linea);
    }

    public List<Paloma> findByPadre(String padreAnillo) throws ExecutionException, InterruptedException {
        return palomaRepository.findByPadre(padreAnillo);
    }

    public List<Paloma> findByMadre(String madreAnillo) throws ExecutionException, InterruptedException {
        return palomaRepository.findByMadre(madreAnillo);
    }

    public String save(Paloma paloma) throws ExecutionException, InterruptedException {
        return palomaRepository.save(paloma);
    }

    public void update(String id, Paloma paloma) throws ExecutionException, InterruptedException {
        palomaRepository.update(id, paloma);
    }

    public void deleteById(String id) throws ExecutionException, InterruptedException {
        palomaRepository.deleteById(id);
    }

    // Genealogy methods
    public List<Paloma> getAncestors(String palomaId) throws ExecutionException, InterruptedException {
        Optional<Paloma> palomaOpt = findById(palomaId);
        if (palomaOpt.isEmpty()) {
            return List.of();
        }

        Paloma paloma = palomaOpt.get();
        List<Paloma> ancestors = new java.util.ArrayList<>();

        // Add father if exists
        if (paloma.getPadre() != null) {
            findByAnillo(paloma.getPadre()).ifPresent(ancestors::add);
        }

        // Add mother if exists
        if (paloma.getMadre() != null) {
            findByAnillo(paloma.getMadre()).ifPresent(ancestors::add);
        }

        return ancestors;
    }

    public List<Paloma> getDescendants(String palomaId) throws ExecutionException, InterruptedException {
        Optional<Paloma> palomaOpt = findById(palomaId);
        if (palomaOpt.isEmpty()) {
            return List.of();
        }

        Paloma paloma = palomaOpt.get();
        String anillo = paloma.getAnillo();

        List<Paloma> descendants = new java.util.ArrayList<>();
        descendants.addAll(findByPadre(anillo));
        descendants.addAll(findByMadre(anillo));

        return descendants;
    }
}