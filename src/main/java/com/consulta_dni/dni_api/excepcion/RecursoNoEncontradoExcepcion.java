package com.consulta_dni.dni_api.excepcion;

public class RecursoNoEncontradoExcepcion extends RuntimeException {

    public RecursoNoEncontradoExcepcion(String mensaje) {
        super(mensaje);
    }
}
