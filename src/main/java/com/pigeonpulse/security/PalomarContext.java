package com.pigeonpulse.security;

import com.pigeonpulse.model.Palomar;
import com.pigeonpulse.model.Usuario;

public class PalomarContext {
    private final Usuario usuario;
    private final Palomar palomar;
    private final String rol;

    public PalomarContext(Usuario usuario, Palomar palomar, String rol) {
        this.usuario = usuario;
        this.palomar = palomar;
        this.rol = rol;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public Palomar getPalomar() {
        return palomar;
    }

    public String getRol() {
        return rol;
    }

    public String getUsuarioId() {
        return usuario.getId();
    }

    public String getPalomarId() {
        return palomar.getId();
    }

    public boolean isPropietario() {
        return "PROPIETARIO".equals(rol);
    }

    public boolean isColaborador() {
        return "COLABORADOR".equals(rol);
    }

    public boolean puedeEditarPalomas() {
        return isPropietario() || isColaborador();
    }

    public boolean puedeVerReportes() {
        return isPropietario() || isColaborador();
    }

    public boolean puedeGestionarUsuarios() {
        return isPropietario();
    }
}