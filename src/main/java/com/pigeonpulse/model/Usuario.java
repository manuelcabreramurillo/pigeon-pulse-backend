package com.pigeonpulse.model;

import com.google.cloud.firestore.annotation.DocumentId;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class Usuario {

    @DocumentId
    private String id;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @Email(message = "Email inválido")
    private String email;

    private String telefono;

    private String domicilio;

    private String googleId;



    // Constructors
    public Usuario() {

    }

    public Usuario(String nombre, String email, String googleId) {
        this.nombre = nombre;
        this.email = email;
        this.googleId = googleId;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getDomicilio() { return domicilio; }
    public void setDomicilio(String domicilio) { this.domicilio = domicilio; }

    public String getGoogleId() { return googleId; }
    public void setGoogleId(String googleId) { this.googleId = googleId; }




}