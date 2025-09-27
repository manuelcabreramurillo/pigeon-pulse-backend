package com.pigeonpulse.controller;

import com.pigeonpulse.dto.PalomarDTO;
import com.pigeonpulse.dto.UsuarioPalomarDTO;
import com.pigeonpulse.model.Palomar;
import com.pigeonpulse.model.Usuario;
import com.pigeonpulse.model.UsuarioPalomar;
import com.pigeonpulse.security.PalomarContext;
import com.pigeonpulse.service.PalomarService;
import com.pigeonpulse.service.UsuarioPalomarService;
import com.pigeonpulse.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/palomares")
@Tag(name = "Palomares", description = "Gestión de palomares y workspaces")
public class PalomarController {

    @Autowired
    private PalomarService palomarService;

    @Autowired
    private UsuarioPalomarService usuarioPalomarService;

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    @Operation(summary = "Obtener palomares del usuario")
    public ResponseEntity<List<PalomarDTO>> getUserPalomares() throws ExecutionException, InterruptedException {
        PalomarContext palomarContext = (PalomarContext) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Usuario usuario = palomarContext.getUsuario();

        System.out.println("PalomarController: Getting palomares for user: " + usuario.getId() + " (" + usuario.getEmail() + ")");

        // Get user's palomares (owned and shared)
        List<UsuarioPalomar> userPalomares = usuarioPalomarService.findByUsuarioId(usuario.getId());
        System.out.println("PalomarController: Found " + userPalomares.size() + " palomar relationships");

        List<PalomarDTO> palomaresDTO = userPalomares.stream()
                .map(userPalomar -> {
                    try {
                        Palomar palomar = palomarService.findById(userPalomar.getPalomarId()).orElse(null);
                        if (palomar != null) {
                            return new PalomarDTO(
                                palomar.getId(),
                                palomar.getNombre(),
                                palomar.getAlias(),
                                palomar.getPropietarioId(),
                                userPalomar.getRol(),
                                palomar.getFechaCreacion()
                            );
                        }
                    } catch (ExecutionException | InterruptedException e) {
                        // Log error but continue
                    }
                    return null;
                })
                .filter(dto -> dto != null)
                .collect(Collectors.toList());

        return ResponseEntity.ok(palomaresDTO);
    }

    @PostMapping
    @Operation(summary = "Crear nuevo palomar")
    public ResponseEntity<PalomarDTO> createPalomar(@RequestBody CreatePalomarRequest request) throws ExecutionException, InterruptedException {
        PalomarContext palomarContext = (PalomarContext) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Usuario usuario = palomarContext.getUsuario();

        Palomar palomar = new Palomar(request.nombre(), usuario.getId());
        if (request.alias() != null && !request.alias().trim().isEmpty()) {
            palomar.setAlias(request.alias());
        }

        String palomarId = palomarService.save(palomar);
        palomar.setId(palomarId);

        // Create PROPIETARIO relationship
        usuarioPalomarService.createPropietarioRelation(usuario.getId(), palomar);

        PalomarDTO palomarDTO = new PalomarDTO(
            palomar.getId(),
            palomar.getNombre(),
            palomar.getAlias(),
            palomar.getPropietarioId(),
            "PROPIETARIO",
            palomar.getFechaCreacion()
        );

        return ResponseEntity.ok(palomarDTO);
    }

    @GetMapping("/{id}/accesos")
    @Operation(summary = "Obtener usuarios con acceso al palomar")
    public ResponseEntity<List<UsuarioPalomarDTO>> getPalomarAccesos(@PathVariable String id) throws ExecutionException, InterruptedException {
        PalomarContext palomarContext = (PalomarContext) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Usuario usuario = palomarContext.getUsuario();

        // Verify user has access to this palomar
        boolean hasAccess = usuarioPalomarService.hasAccessToPalomar(usuario.getId(), id);
        if (!hasAccess) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Get all users with access to this palomar
        List<UsuarioPalomar> accesos = usuarioPalomarService.findByPalomarId(id);

        List<UsuarioPalomarDTO> accesosDTO = accesos.stream()
                .map(acceso -> {
                    try {
                        // Get user information
                        Optional<Usuario> usuarioOpt = usuarioService.findById(acceso.getUsuarioId());
                        String usuarioNombre = usuarioOpt.map(Usuario::getNombre).orElse("Usuario desconocido");
                        String usuarioEmail = usuarioOpt.map(Usuario::getEmail).orElse(acceso.getUsuarioId());

                        return new UsuarioPalomarDTO(
                            acceso.getId(),
                            acceso.getUsuarioId(),
                            usuarioNombre,
                            usuarioEmail,
                            acceso.getPalomarId(),
                            acceso.getRol(),
                            acceso.getFechaAutorizacion()
                        );
                    } catch (ExecutionException | InterruptedException e) {
                        // Fallback if user lookup fails
                        return new UsuarioPalomarDTO(
                            acceso.getId(),
                            acceso.getUsuarioId(),
                            "Usuario desconocido",
                            acceso.getUsuarioId(),
                            acceso.getPalomarId(),
                            acceso.getRol(),
                            acceso.getFechaAutorizacion()
                        );
                    }
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(accesosDTO);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar información del palomar")
    public ResponseEntity<PalomarDTO> updatePalomar(@PathVariable String id, @RequestBody UpdatePalomarRequest request) throws ExecutionException, InterruptedException {
        PalomarContext palomarContext = (PalomarContext) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Usuario usuario = palomarContext.getUsuario();

        // Verify user is PROPIETARIO of this palomar
        Optional<String> userRole = usuarioPalomarService.getRolInPalomar(usuario.getId(), id);
        if (userRole.isEmpty() || !"PROPIETARIO".equals(userRole.get())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Get current palomar
        Optional<Palomar> palomarOpt = palomarService.findById(id);
        if (palomarOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Palomar palomar = palomarOpt.get();

        // Update alias if provided
        if (request.alias() != null) {
            palomar.setAlias(request.alias().trim().isEmpty() ? null : request.alias().trim());
        }

        // Update existing palomar
        palomarService.update(id, palomar);

        PalomarDTO palomarDTO = new PalomarDTO(
            palomar.getId(),
            palomar.getNombre(),
            palomar.getAlias(),
            palomar.getPropietarioId(),
            userRole.get(), // Use the actual role from verification
            palomar.getFechaCreacion()
        );

        return ResponseEntity.ok(palomarDTO);
    }

    @PostMapping("/{id}/accesos")
    @Operation(summary = "Dar acceso a un colaborador")
    public ResponseEntity<Void> grantAccess(@PathVariable String id, @RequestBody GrantAccessRequest request) throws ExecutionException, InterruptedException {
        PalomarContext palomarContext = (PalomarContext) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Usuario usuario = palomarContext.getUsuario();

        // Verify user is PROPIETARIO of this palomar
        Optional<String> userRole = usuarioPalomarService.getRolInPalomar(usuario.getId(), id);
        if (userRole.isEmpty() || !"PROPIETARIO".equals(userRole.get())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Grant COLABORADOR access
        usuarioPalomarService.inviteColaborador(request.usuarioId(), id);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/accesos/{usuarioId}")
    @Operation(summary = "Revocar acceso a un colaborador")
    public ResponseEntity<Void> revokeAccess(@PathVariable String id, @PathVariable String usuarioId) throws ExecutionException, InterruptedException {
        PalomarContext palomarContext = (PalomarContext) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Usuario usuario = palomarContext.getUsuario();

        // Verify user is PROPIETARIO of this palomar
        Optional<String> userRole = usuarioPalomarService.getRolInPalomar(usuario.getId(), id);
        if (userRole.isEmpty() || !"PROPIETARIO".equals(userRole.get())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Find and delete the relationship
        Optional<UsuarioPalomar> relacion = usuarioPalomarService.findByUsuarioIdAndPalomarId(usuarioId, id);
        if (relacion.isPresent()) {
            usuarioPalomarService.deleteById(relacion.get().getId());
        }

        return ResponseEntity.ok().build();
    }

    // Request DTOs
    public record CreatePalomarRequest(String nombre, String alias) {}
    public record UpdatePalomarRequest(String alias) {}
    public record GrantAccessRequest(String usuarioId) {}
}