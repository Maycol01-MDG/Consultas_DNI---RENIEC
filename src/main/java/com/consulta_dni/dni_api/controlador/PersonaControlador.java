package com.consulta_dni.dni_api.controlador;

import com.consulta_dni.dni_api.dto.PersonaRespuestaDTO;
import com.consulta_dni.dni_api.servicio.PersonaServicio;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dni")
public class PersonaControlador {

    private final PersonaServicio personaServicio;

    // Inyección por constructor: más seguro y facilita las pruebas unitarias
    public PersonaControlador(PersonaServicio personaServicio) {
        this.personaServicio = personaServicio;
    }

    @GetMapping("/{dni}")
    public PersonaRespuestaDTO obtenerPersona(@PathVariable String dni) {
        return personaServicio.obtenerPorDni(dni);
    }
}