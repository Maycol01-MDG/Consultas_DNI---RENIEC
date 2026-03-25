package com.consulta_dni.dni_api.controlador;

import com.consulta_dni.dni_api.dto.PersonaRespuestaDTO;
import com.consulta_dni.dni_api.servicio.PersonaServicio;
import org.springframework.beans.factory.annotation.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dni")
public class PersonaControlador {
    @Autowired
    private PersonaServicio personaServicio;

    @GetMapping("/{dni}")
    public PersonaRespuestaDTO obtenerPersona(@PathVariable String dni) {
        return personaServicio.obtenerPorDni(dni);
    }
}
