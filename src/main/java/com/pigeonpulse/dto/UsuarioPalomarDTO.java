package com.pigeonpulse.dto;

import java.time.LocalDateTime;

public record UsuarioPalomarDTO(
    String id,
    String usuarioId,
    String usuarioNombre,
    String usuarioEmail,
    String palomarId,
    String rol,
    LocalDateTime fechaAutorizacion
) {
}