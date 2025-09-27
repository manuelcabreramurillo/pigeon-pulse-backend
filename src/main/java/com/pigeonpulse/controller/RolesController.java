package com.pigeonpulse.controller;

import com.pigeonpulse.dto.RolPalomarDTO;
import com.pigeonpulse.model.Usuario;
import com.pigeonpulse.model.UsuarioPalomar;
import com.pigeonpulse.security.PalomarContext;
import com.pigeonpulse.service.UsuarioPalomarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/roles")
@Tag(name = "Roles", description = "Gestión de roles y permisos")
public class RolesController {

    @Autowired
    private UsuarioPalomarService usuarioPalomarService;

    @GetMapping
    @Operation(summary = "Obtener roles del usuario en todos sus palomares")
    public ResponseEntity<List<RolPalomarDTO>> getUserRoles() throws ExecutionException, InterruptedException {
        PalomarContext palomarContext = (PalomarContext) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Usuario usuario = palomarContext.getUsuario();

        // Get all user's palomar relationships
        List<UsuarioPalomar> userPalomares = usuarioPalomarService.findByUsuarioId(usuario.getId());

        List<RolPalomarDTO> rolesDTO = userPalomares.stream()
                .map(up -> new RolPalomarDTO(
                    up.getPalomarId(),
                    up.getRol()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(rolesDTO);
    }
}