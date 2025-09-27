package com.pigeonpulse.model;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.firestore.annotation.Exclude;
import com.google.cloud.firestore.annotation.PropertyName;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public class Palomar {

    @DocumentId
    private String id;

    @NotBlank(message = "El nombre del palomar es obligatorio")
    private String nombre;

    private String alias; // Alias opcional configurable

    @PropertyName("propietario_id")
    public String propietarioId; // Usuario propietario

    @PropertyName("fecha_creacion_timestamp")
    private Timestamp fechaCreacion;

    // Constructors
    public Palomar() {
        this.fechaCreacion = Timestamp.now();
    }

    public Palomar(String nombre, String propietarioId) {
        this.nombre = nombre;
        this.propietarioId = propietarioId;
        this.fechaCreacion = Timestamp.now();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getAlias() { return alias; }
    public void setAlias(String alias) { this.alias = alias; }

    public String getPropietarioId() { return propietarioId; }
    public void setPropietarioId(String propietarioId) { this.propietarioId = propietarioId; }

    @Exclude
    public LocalDateTime getFechaCreacion() {
        if (fechaCreacion != null) {
            return LocalDateTime.ofInstant(fechaCreacion.toDate().toInstant(),
                java.time.ZoneId.systemDefault());
        }
        return LocalDateTime.now();
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion != null ?
            Timestamp.of(java.util.Date.from(fechaCreacion.atZone(java.time.ZoneId.systemDefault()).toInstant())) : null;
    }

    // Helper methods
    public String getDisplayName() {
        return alias != null && !alias.trim().isEmpty() ? alias : nombre;
    }

    public boolean isPropietario(String usuarioId) {
        return propietarioId != null && propietarioId.equals(usuarioId);
    }
}