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
        java.util.Set<String> visited = new java.util.HashSet<>();

        // Recursive function to collect all ancestors
        collectAncestors(paloma, ancestors, visited);

        return ancestors;
    }

    private void collectAncestors(Paloma paloma, List<Paloma> ancestors, java.util.Set<String> visited) throws ExecutionException, InterruptedException {
        // Add father if exists and not already visited
        if (paloma.getPadre() != null && !visited.contains(paloma.getPadre())) {
            visited.add(paloma.getPadre());
            Optional<Paloma> fatherOpt = findByAnillo(paloma.getPadre());
            if (fatherOpt.isPresent()) {
                Paloma father = fatherOpt.get();
                ancestors.add(father);
                // Recursively collect father's ancestors
                collectAncestors(father, ancestors, visited);
            }
        }

        // Add mother if exists and not already visited
        if (paloma.getMadre() != null && !visited.contains(paloma.getMadre())) {
            visited.add(paloma.getMadre());
            Optional<Paloma> motherOpt = findByAnillo(paloma.getMadre());
            if (motherOpt.isPresent()) {
                Paloma mother = motherOpt.get();
                ancestors.add(mother);
                // Recursively collect mother's ancestors
                collectAncestors(mother, ancestors, visited);
            }
        }
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