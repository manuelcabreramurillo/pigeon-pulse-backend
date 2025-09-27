package com.pigeonpulse.model;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.firestore.annotation.Exclude;
import com.google.cloud.firestore.annotation.PropertyName;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public class UsuarioPalomar {

    @DocumentId
    private String id;

    @PropertyName("usuario_id")
    @NotBlank(message = "El ID de usuario es obligatorio")
    public String usuarioId;

    @PropertyName("palomar_id")
    @NotBlank(message = "El ID del palomar es obligatorio")
    public String palomarId;

    @NotBlank(message = "El rol es obligatorio")
    public String rol; // "PROPIETARIO", "COLABORADOR"

    private String permisos; // JSON con permisos específicos (futuro)

    @PropertyName("fecha_autorizacion_timestamp")
    private Timestamp fechaAutorizacion;

    // Constructors
    public UsuarioPalomar() {
        this.fechaAutorizacion = Timestamp.now();
    }

    public UsuarioPalomar(String usuarioId, String palomarId, String rol) {
        this.usuarioId = usuarioId;
        this.palomarId = palomarId;
        this.rol = rol;
        this.fechaAutorizacion = Timestamp.now();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsuarioId() { return usuarioId; }
    public void setUsuarioId(String usuarioId) { this.usuarioId = usuarioId; }

    public String getPalomarId() { return palomarId; }
    public void setPalomarId(String palomarId) { this.palomarId = palomarId; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public String getPermisos() { return permisos; }
    public void setPermisos(String permisos) { this.permisos = permisos; }

    @Exclude
    public LocalDateTime getFechaAutorizacion() {
        if (fechaAutorizacion != null) {
            return LocalDateTime.ofInstant(fechaAutorizacion.toDate().toInstant(),
                java.time.ZoneId.systemDefault());
        }
        return LocalDateTime.now();
    }

    public void setFechaAutorizacion(LocalDateTime fechaAutorizacion) {
        this.fechaAutorizacion = fechaAutorizacion != null ?
            Timestamp.of(java.util.Date.from(fechaAutorizacion.atZone(java.time.ZoneId.systemDefault()).toInstant())) : null;
    }

    // Helper methods
    public boolean isPropietario() {
        return "PROPIETARIO".equals(rol);
    }

    public boolean isColaborador() {
        return "COLABORADOR".equals(rol);
    }

    public boolean puedeGestionarUsuarios() {
        return isPropietario();
    }

    public boolean puedeEditarPalomas() {
        return isPropietario() || isColaborador();
    }

    public boolean puedeVerReportes() {
        return isPropietario() || isColaborador();
    }
}