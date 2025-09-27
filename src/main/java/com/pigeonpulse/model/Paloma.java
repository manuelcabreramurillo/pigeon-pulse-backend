package com.pigeonpulse.model;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.firestore.annotation.Exclude;
import com.google.cloud.firestore.annotation.PropertyName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class Paloma {

    @DocumentId
    private String id;

    @NotBlank(message = "El anillo es obligatorio")
    private String anillo;

    @NotNull(message = "El año es obligatorio")
    private Integer año;

    private String sexo; // Macho, Hembra, Indefinido

    private String color;

    private String linea;

    private String estado; // Activa en carrera, Reproductora, Otra

    private String padre; // anillo del padre

    private String madre; // anillo de la madre

    private String observaciones;

    private Timestamp fechaRegistro;

    private String peso;

    private String altura;

    private String usuarioId; // ID del usuario propietario (legacy)

    @PropertyName("palomar_id")
    private String palomarId; // ID del palomar al que pertenece

    // Constructors
    public Paloma() {
        this.fechaRegistro = Timestamp.now();
    }

    public Paloma(String anillo, Integer año, String usuarioId) {
        this.anillo = anillo;
        this.año = año;
        this.usuarioId = usuarioId;
        this.fechaRegistro = Timestamp.now();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getAnillo() { return anillo; }
    public void setAnillo(String anillo) { this.anillo = anillo; }

    public Integer getAño() { return año; }
    public void setAño(Integer año) { this.año = año; }

    public String getSexo() { return sexo; }
    public void setSexo(String sexo) { this.sexo = sexo; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getLinea() { return linea; }
    public void setLinea(String linea) { this.linea = linea; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getPadre() { return padre; }
    public void setPadre(String padre) { this.padre = padre; }

    public String getMadre() { return madre; }
    public void setMadre(String madre) { this.madre = madre; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    @Exclude
    public LocalDateTime getFechaRegistro() {
        if (fechaRegistro != null) {
            return LocalDateTime.ofInstant(fechaRegistro.toDate().toInstant(),
                java.time.ZoneId.systemDefault());
        }
        return LocalDateTime.now();
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro != null ?
            Timestamp.of(java.util.Date.from(fechaRegistro.atZone(java.time.ZoneId.systemDefault()).toInstant())) : null;
    }

    public String getPeso() { return peso; }
    public void setPeso(String peso) { this.peso = peso; }

    public String getAltura() { return altura; }
    public void setAltura(String altura) { this.altura = altura; }

    public String getUsuarioId() { return usuarioId; }
    public void setUsuarioId(String usuarioId) { this.usuarioId = usuarioId; }

    public String getPalomarId() { return palomarId; }
    public void setPalomarId(String palomarId) { this.palomarId = palomarId; }
}