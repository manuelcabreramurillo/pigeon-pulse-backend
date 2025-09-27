package com.pigeonpulse.dto;


public record UsuarioDTO(
    String id,
    String nombre,
    String email,
    String telefono,
    String domicilio
    //LocalDateTime fechaCreacion
) {
}