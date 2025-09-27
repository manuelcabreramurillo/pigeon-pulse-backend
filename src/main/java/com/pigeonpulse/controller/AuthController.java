package com.pigeonpulse.controller;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.pigeonpulse.dto.AuthResponse;
import com.pigeonpulse.dto.UsuarioDTO;
import com.pigeonpulse.model.Palomar;
import com.pigeonpulse.model.Usuario;
import com.pigeonpulse.model.UsuarioPalomar;
import com.pigeonpulse.security.JwtUtil;
import com.pigeonpulse.security.PalomarContext;
import com.pigeonpulse.service.PalomarService;
import com.pigeonpulse.service.UsuarioPalomarService;
import com.pigeonpulse.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private PalomarService palomarService;

    @Autowired
    private UsuarioPalomarService usuarioPalomarService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody FirebaseLoginRequest request) throws ExecutionException, InterruptedException {
        try {
            // Verify Firebase ID token (works for both Firebase Auth tokens and Google tokens)
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(request.token());

            // Extract user information from verified token
            String firebaseUid = decodedToken.getUid();
            String email = decodedToken.getEmail();
            String nombre = decodedToken.getName();

            // Check if email is verified (optional but recommended)
            if (!Boolean.TRUE.equals(decodedToken.isEmailVerified())) {
                throw new RuntimeException("Email not verified");
            }

            // Create or update user in database using Firebase UID as googleId
            Usuario usuario = usuarioService.createOrUpdateFromGoogle(firebaseUid, email, nombre);

            // Ensure user has their own palomar (PROPIETARIO relationship)
            System.out.println("AuthController: Checking if user has their own palomar...");
            var ownedPalomares = palomarService.findByPropietarioId(usuario.getId());

            Palomar userOwnPalomar;
            if (ownedPalomares.isEmpty()) {
                System.out.println("AuthController: User doesn't have their own palomar, creating default one");
                // Create default palomar for user
                userOwnPalomar = palomarService.createDefaultPalomar(usuario);
                System.out.println("AuthController: Created user's own palomar with ID: " + userOwnPalomar.getId());

                // Create PROPIETARIO relationship
                usuarioPalomarService.createPropietarioRelation(usuario.getId(), userOwnPalomar);
                System.out.println("AuthController: Created propietario relationship for user's own palomar");
            } else {
                userOwnPalomar = ownedPalomares.get(0); // Use first owned palomar
                System.out.println("AuthController: User already has their own palomar: " + userOwnPalomar.getId());
            }

            // For JWT token, use user's own palomar with PROPIETARIO role
            // This ensures the user always has access to their own workspace
            Palomar palomar = userOwnPalomar;
            String rol = "PROPIETARIO";
            System.out.println("AuthController: Using user's own palomar for JWT: " + palomar.getId() + " with role: " + rol);

            // Generate JWT token with palomar context
            String jwtToken = jwtUtil.generateToken(usuario.getId(), usuario.getEmail(), palomar.getId(), rol);

            UsuarioDTO usuarioDTO = new UsuarioDTO(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.getTelefono(),
                usuario.getDomicilio()
            );

            AuthResponse response = new AuthResponse(jwtToken, usuarioDTO);
            return ResponseEntity.ok(response);

        } catch (FirebaseAuthException e) {
            // Handle Firebase authentication errors
            String errorCode = e.getErrorCode().toString();
            switch (errorCode) {
                case "EXPIRED_ID_TOKEN":
                    throw new RuntimeException("Firebase ID token has expired");
                case "REVOKED_ID_TOKEN":
                    throw new RuntimeException("Firebase ID token has been revoked");
                case "INVALID_ID_TOKEN":
                    throw new RuntimeException("Invalid Firebase ID token");
                default:
                    throw new RuntimeException("Firebase authentication failed: " + e.getMessage());
            }
        } catch (Exception e) {
            throw new RuntimeException("Authentication failed: " + e.getMessage());
        }
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioDTO> getCurrentUser() {
        PalomarContext palomarContext = (PalomarContext) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Usuario usuario = palomarContext.getUsuario();
        UsuarioDTO usuarioDTO = new UsuarioDTO(
            usuario.getId(),
            usuario.getNombre(),
            usuario.getEmail(),
            usuario.getTelefono(),
            usuario.getDomicilio()
        );
        return ResponseEntity.ok(usuarioDTO);
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout del usuario")
    public ResponseEntity<Void> logout() {
        // Clear the security context on the server side
        SecurityContextHolder.clearContext();
        // Client-side token removal is handled by the frontend
        return ResponseEntity.ok().build();
    }

    @GetMapping("/users/search")
    @Operation(summary = "Buscar usuario por email - permite invitar a cualquier email válido")
    public ResponseEntity<UsuarioDTO> searchUserByEmail(@RequestParam String email) {
        try {
            // Validate email format
            if (email == null || email.trim().isEmpty() || !email.contains("@")) {
                return ResponseEntity.badRequest().build();
            }

            // First, check if user exists in our database
            Optional<Usuario> usuarioOpt = usuarioService.findByEmail(email.trim());
            if (usuarioOpt.isPresent()) {
                Usuario usuario = usuarioOpt.get();
                UsuarioDTO usuarioDTO = new UsuarioDTO(
                    usuario.getId(),
                    usuario.getNombre(),
                    usuario.getEmail(),
                    usuario.getTelefono(),
                    usuario.getDomicilio()
                );
                return ResponseEntity.ok(usuarioDTO);
            }

            // User not found in database - create basic user record for invitation
            // This allows inviting any valid email address, even if they haven't registered yet
            Usuario newUsuario = new Usuario(email.trim(), email.trim(), null); // null googleId for invited users
            String userId = usuarioService.save(newUsuario);
            newUsuario.setId(userId);

            UsuarioDTO usuarioDTO = new UsuarioDTO(
                newUsuario.getId(),
                newUsuario.getNombre(),
                newUsuario.getEmail(),
                newUsuario.getTelefono(),
                newUsuario.getDomicilio()
            );
            return ResponseEntity.ok(usuarioDTO);

        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/cleanup-fecha-creacion")
    @Operation(summary = "Endpoint temporal para limpiar campos fechaCreacion residuales")
    public ResponseEntity<String> cleanupFechaCreacion() {
        try {
            usuarioService.cleanupFechaCreacionField();
            return ResponseEntity.ok("Campo fechaCreacion eliminado de todos los documentos de usuarios");
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.internalServerError()
                .body("Error al limpiar campo fechaCreacion: " + e.getMessage());
        }
    }

    // Record for Firebase login request
    public record FirebaseLoginRequest(String token, String provider) {}
}