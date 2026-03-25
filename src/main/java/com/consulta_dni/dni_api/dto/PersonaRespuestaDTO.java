package com.consulta_dni.dni_api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PersonaRespuestaDTO {

    private String dni;
    private String nombreCompleto;
}