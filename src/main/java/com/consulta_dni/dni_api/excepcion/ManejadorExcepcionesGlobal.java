package com.consulta_dni.dni_api.excepcion;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ManejadorExcepcionesGlobal {
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> manejarDniInvalido(IllegalArgumentException ex) {
        Map<String, String> respuesta = new HashMap<>();
        respuesta.put("error", ex.getMessage());
        return respuesta;
    }

    @ExceptionHandler(RecursoNoEncontradoExcepcion.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> manejarError(RecursoNoEncontradoExcepcion ex) {

        Map<String, String> respuesta = new HashMap<>();
        respuesta.put("error", ex.getMessage());

        return respuesta;
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public Map<String, String> manejarErrorServicioExterno(IllegalStateException ex) {
        Map<String, String> respuesta = new HashMap<>();
        respuesta.put("error", ex.getMessage());
        return respuesta;
    }
}
