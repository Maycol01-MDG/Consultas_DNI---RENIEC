package com.consulta_dni.dni_api.servicio;

import com.consulta_dni.dni_api.dto.PersonaRespuestaDTO;
public interface PersonaServicio {

    PersonaRespuestaDTO obtenerPorDni(String dni);
}