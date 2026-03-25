package com.consulta_dni.dni_api.repositorio;

import com.consulta_dni.dni_api.modelo.Persona;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonaRepositorio extends JpaRepository<Persona, String> {
}
