package com.pigeonpulse.dto;

import java.time.LocalDateTime;

public record PalomarDTO(
    String id,
    String nombre,
    String alias,
    String propietarioId,
    String rolUsuario,
    LocalDateTime fechaCreacion
) {
}