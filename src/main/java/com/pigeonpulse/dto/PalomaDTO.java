package com.pigeonpulse.dto;

import java.time.LocalDateTime;

public record PalomaDTO(
    String id,
    String anillo,
    Integer año,
    String sexo,
    String color,
    String linea,
    String estado,
    String padre,
    String madre,
    String observaciones,
    String tipoOjo,
    LocalDateTime fechaRegistro,
    String peso,
    String altura,
    String usuarioId,
    String palomarId
) {
}