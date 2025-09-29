package com.pigeonpulse.controller;

import com.pigeonpulse.dto.PalomaDTO;
import com.pigeonpulse.model.Paloma;
import com.pigeonpulse.model.Usuario;
import com.pigeonpulse.security.PalomarContext;
import com.pigeonpulse.service.PalomaService;
import com.pigeonpulse.service.UsuarioPalomarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/palomas")
@Tag(name = "Palomas", description = "API para gestión de palomas")
public class PalomaController {

    @Autowired
    private PalomaService palomaService;

    @Autowired
    private UsuarioPalomarService usuarioPalomarService;

    @GetMapping
    @Operation(summary = "Listar palomas", description = "Obtiene la lista de palomas con filtros opcionales")
    public ResponseEntity<List<PalomaDTO>> getPalomas(
            @RequestParam(required = false) String palomarId,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String sexo,
            @RequestParam(required = false) String linea,
            @RequestParam(required = false) String search) throws ExecutionException, InterruptedException {

        PalomarContext palomarContext = (PalomarContext) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Usuario usuario = palomarContext.getUsuario();

        // Use provided palomarId or default to JWT context
        String targetPalomarId = palomarId != null ? palomarId : palomarContext.getPalomarId();

        // Verify user has access to the target palomar
        boolean hasAccess = usuarioPalomarService.hasAccessToPalomar(usuario.getId(), targetPalomarId);
        if (!hasAccess) {
            return ResponseEntity.status(403).build();
        }

        List<Paloma> palomas = palomaService.findByPalomarId(targetPalomarId);

        // Apply filters
        if (estado != null && !estado.isEmpty()) {
            palomas = palomas.stream()
                    .filter(p -> estado.equals(p.getEstado()))
                    .collect(Collectors.toList());
        }
        if (sexo != null && !sexo.isEmpty()) {
            palomas = palomas.stream()
                    .filter(p -> sexo.equals(p.getSexo()))
                    .collect(Collectors.toList());
        }
        if (linea != null && !linea.isEmpty()) {
            palomas = palomas.stream()
                    .filter(p -> linea.equals(p.getLinea()))
                    .collect(Collectors.toList());
        }
        if (search != null && !search.isEmpty()) {
            String searchLower = search.toLowerCase();
            palomas = palomas.stream()
                    .filter(p -> p.getAnillo().toLowerCase().contains(searchLower) ||
                               p.getColor().toLowerCase().contains(searchLower) ||
                               p.getLinea().toLowerCase().contains(searchLower))
                    .collect(Collectors.toList());
        }

        List<PalomaDTO> palomasDTO = palomas.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(palomasDTO);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener paloma por ID")
    public ResponseEntity<PalomaDTO> getPalomaById(@PathVariable String id) throws ExecutionException, InterruptedException {
        return palomaService.findById(id)
                .map(paloma -> ResponseEntity.ok(convertToDTO(paloma)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Crear nueva paloma")
    public ResponseEntity<PalomaDTO> createPaloma(
            @RequestParam(required = false) String palomarId,
            @RequestBody PalomaDTO palomaDTO) throws ExecutionException, InterruptedException {

        PalomarContext palomarContext = (PalomarContext) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Usuario usuario = palomarContext.getUsuario();

        // Use provided palomarId or default to JWT context
        String targetPalomarId = palomarId != null ? palomarId : palomarContext.getPalomarId();

        // Verify user has access to the target palomar
        boolean hasAccess = usuarioPalomarService.hasAccessToPalomar(usuario.getId(), targetPalomarId);
        if (!hasAccess) {
            return ResponseEntity.status(403).build();
        }

        Paloma paloma = convertToEntity(palomaDTO);
        paloma.setPalomarId(targetPalomarId);
        paloma.setUsuarioId(usuario.getId()); // Set the creator

        String savedId = palomaService.save(paloma);
        paloma.setId(savedId);
        return ResponseEntity.ok(convertToDTO(paloma));
    }

    @PostMapping("/{id}")
    @Operation(summary = "Actualizar paloma")
    public ResponseEntity<PalomaDTO> updatePaloma(@PathVariable String id, @RequestParam(required = false) String palomarId, @RequestBody PalomaDTO palomaDTO) throws ExecutionException, InterruptedException {
        PalomarContext palomarContext = (PalomarContext) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Usuario usuario = palomarContext.getUsuario();

        System.out.println("🔄 UPDATE PALOMA - ID: " + id + ", User: " + usuario.getId() + ", JWT PalomarId: " + palomarContext.getPalomarId() + ", Provided PalomarId: " + palomarId);

        // Get the paloma to check its palomarId
        var palomaOpt = palomaService.findById(id);
        if (!palomaOpt.isPresent()) {
            System.out.println("❌ Paloma not found: " + id);
            return ResponseEntity.notFound().build();
        }

        Paloma existingPaloma = palomaOpt.get();
        System.out.println("📋 Existing paloma palomarId: " + existingPaloma.getPalomarId());

        // Use provided palomarId or default to the paloma's existing palomarId
        String targetPalomarId = palomarId != null ? palomarId : existingPaloma.getPalomarId();
        System.out.println("🎯 Target palomarId: " + targetPalomarId);

        // Verify user has access to the target palomar
        boolean hasAccess = usuarioPalomarService.hasAccessToPalomar(usuario.getId(), targetPalomarId);
        System.out.println("🔐 User has access to palomar: " + hasAccess);

        if (!hasAccess) {
            System.out.println("🚫 Access denied for user " + usuario.getId() + " to palomar " + targetPalomarId);
            return ResponseEntity.status(403).build();
        }

        Paloma paloma = convertToEntity(palomaDTO);
        paloma.setId(id);
        paloma.setPalomarId(targetPalomarId); // Ensure the paloma stays in the correct palomar
        palomaService.update(id, paloma);

        System.out.println("✅ Paloma updated successfully: " + id);
        return ResponseEntity.ok(convertToDTO(paloma));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar paloma")
    public ResponseEntity<Void> deletePaloma(@PathVariable String id) throws ExecutionException, InterruptedException {
        PalomarContext palomarContext = (PalomarContext) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Usuario usuario = palomarContext.getUsuario();

        // Get the paloma to check its palomarId
        var palomaOpt = palomaService.findById(id);
        if (!palomaOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Paloma paloma = palomaOpt.get();

        // Verify user has access to the palomar where this paloma belongs
        boolean hasAccess = usuarioPalomarService.hasAccessToPalomar(usuario.getId(), paloma.getPalomarId());
        if (!hasAccess) {
            return ResponseEntity.status(403).build();
        }

        palomaService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/ancestros")
    @Operation(summary = "Obtener ancestros de la paloma")
    public ResponseEntity<List<PalomaDTO>> getAncestors(@PathVariable String id) throws ExecutionException, InterruptedException {
        List<Paloma> ancestors = palomaService.getAncestors(id);
        List<PalomaDTO> ancestorsDTO = ancestors.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ancestorsDTO);
    }

    @GetMapping("/{id}/descendientes")
    @Operation(summary = "Obtener descendientes de la paloma")
    public ResponseEntity<List<PalomaDTO>> getDescendants(@PathVariable String id) throws ExecutionException, InterruptedException {
        List<Paloma> descendants = palomaService.getDescendants(id);
        List<PalomaDTO> descendantsDTO = descendants.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(descendantsDTO);
    }

    private PalomaDTO convertToDTO(Paloma paloma) {
        return new PalomaDTO(
            paloma.getId(),
            paloma.getAnillo(),
            paloma.getAño(),
            paloma.getSexo(),
            paloma.getColor(),
            paloma.getLinea(),
            paloma.getEstado(),
            paloma.getPadre(),
            paloma.getMadre(),
            paloma.getObservaciones(),
            paloma.getFechaRegistro(),
            paloma.getPeso(),
            paloma.getAltura(),
            paloma.getUsuarioId(),
            paloma.getPalomarId()
        );
    }

    private Paloma convertToEntity(PalomaDTO palomaDTO) {
        Paloma paloma = new Paloma();
        paloma.setId(palomaDTO.id());
        paloma.setAnillo(palomaDTO.anillo());
        paloma.setAño(palomaDTO.año());
        paloma.setSexo(palomaDTO.sexo());
        paloma.setColor(palomaDTO.color());
        paloma.setLinea(palomaDTO.linea());
        paloma.setEstado(palomaDTO.estado());
        paloma.setPadre(palomaDTO.padre());
        paloma.setMadre(palomaDTO.madre());
        paloma.setObservaciones(palomaDTO.observaciones());
        paloma.setFechaRegistro(palomaDTO.fechaRegistro());
        paloma.setPeso(palomaDTO.peso());
        paloma.setAltura(palomaDTO.altura());
        paloma.setUsuarioId(palomaDTO.usuarioId());
        paloma.setPalomarId(palomaDTO.palomarId());
        return paloma;
    }
}