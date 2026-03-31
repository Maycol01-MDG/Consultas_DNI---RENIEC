package com.consulta_dni.dni_api.servicio.impl;

import com.consulta_dni.dni_api.dto.PersonaRespuestaDTO;
import com.consulta_dni.dni_api.dto.ReniecRespuestaDTO;
import com.consulta_dni.dni_api.excepcion.RecursoNoEncontradoExcepcion;
import com.consulta_dni.dni_api.modelo.Persona;
import com.consulta_dni.dni_api.repositorio.PersonaRepositorio;
import com.consulta_dni.dni_api.servicio.PersonaServicio;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Service
public class PersonaServicioImpl implements PersonaServicio {

    private final RestTemplate restTemplate;
    private final PersonaRepositorio personaRepositorio;

    @Value("${reniec.api.url}")
    private String reniecApiUrl;

    @Value("${reniec.api.token}")
    private String reniecApiToken;

    public PersonaServicioImpl(RestTemplate restTemplate, PersonaRepositorio personaRepositorio) {
        this.restTemplate = restTemplate;
        this.personaRepositorio = personaRepositorio;
    }

    @Override
    public PersonaRespuestaDTO obtenerPorDni(String dni) {

        // 1. Validar formato del DNI
        if (dni == null || !dni.matches("\\d{8}")) {
            throw new IllegalArgumentException("El DNI debe tener exactamente 8 dígitos numéricos");
        }

        Persona personaEnCache = personaRepositorio.findById(dni).orElse(null);
        if (personaEnCache != null) {
            String nombreCompletoCache = personaEnCache.getNombres() + " "
                    + personaEnCache.getApellidoPaterno() + " "
                    + personaEnCache.getApellidoMaterno();
            return new PersonaRespuestaDTO(personaEnCache.getDni(), nombreCompletoCache.trim());
        }

        if (reniecApiToken == null || reniecApiToken.isBlank()) {
            throw new IllegalStateException("Falta configurar RENIEC_API_TOKEN para consultar RENIEC");
        }

        // 2. Construir headers con el token de autorización
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + reniecApiToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Accept", "application/json");

        HttpEntity<Void> entidad = new HttpEntity<>(headers);

        // 3. Construir la URL con el parámetro del DNI
        String url = reniecApiUrl + "?numero={dni}";

        try {
            // 4. Llamar a la API externa de RENIEC
            ResponseEntity<ReniecRespuestaDTO> respuesta = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entidad,
                    ReniecRespuestaDTO.class,
                    dni // Spring Boot inyectará el DNI aquí de forma segura
            );

            ReniecRespuestaDTO datos = respuesta.getBody();

            // 5. Verificar que la respuesta tenga datos válidos
            if (datos == null || datos.getNumeroDocumento() == null) {
                throw new RecursoNoEncontradoExcepcion("No se encontró persona con DNI: " + dni);
            }

            // 6. Construir nombre completo: nombres + apellido paterno + apellido materno
            String nombreCompleto = capitalizar(datos.getNombres()) + " "
                    + capitalizar(datos.getApellidoPaterno()) + " "
                    + capitalizar(datos.getApellidoMaterno());

            if (nombreCompleto.isBlank() && datos.getNombreCompleto() != null && !datos.getNombreCompleto().isBlank()) {
                nombreCompleto = capitalizar(datos.getNombreCompleto());
            }

            String nombres = capitalizar(datos.getNombres());
            String apellidoPaterno = capitalizar(datos.getApellidoPaterno());
            String apellidoMaterno = capitalizar(datos.getApellidoMaterno());
            String nombreCompletoNormalizado = nombreCompleto.trim();

            if ((nombres.isBlank() || apellidoPaterno.isBlank() || apellidoMaterno.isBlank()) && !nombreCompletoNormalizado.isBlank()) {
                String[] partes = nombreCompletoNormalizado.split("\\s+");
                if (partes.length >= 3) {
                    apellidoPaterno = partes[0];
                    apellidoMaterno = partes[1];
                    nombres = nombreCompletoNormalizado.substring((apellidoPaterno + " " + apellidoMaterno + " ").length()).trim();
                }
            }

            Persona persona = new Persona(datos.getNumeroDocumento(), nombres, apellidoPaterno, apellidoMaterno);
            personaRepositorio.save(persona);

            return new PersonaRespuestaDTO(persona.getDni(), nombreCompletoNormalizado);

        } catch (HttpClientErrorException.NotFound e) {
            throw new RecursoNoEncontradoExcepcion("No se encontró persona con DNI: " + dni);

        } catch (HttpClientErrorException.Unauthorized e) {
            throw new IllegalStateException("Token de autorización inválido o expirado. Verifica tu token del proveedor RENIEC");

        } catch (HttpClientErrorException.TooManyRequests e) {
            throw new IllegalStateException("Se superó el límite de consultas. Intenta más tarde");

        } catch (HttpClientErrorException e) {
            throw new IllegalStateException("Error al consultar el servicio RENIEC: " + e.getStatusCode());

        } catch (ResourceAccessException e) {
            throw new IllegalStateException("No se pudo conectar con el servicio RENIEC. Verifica tu conexión");
        }
    }

    /**
     * Convierte texto en formato "JUAN CARLOS" a "Juan Carlos"
     */
    private String capitalizar(String texto) {
        if (texto == null || texto.isBlank()) return "";
        String[] palabras = texto.trim().toLowerCase().split("\\s+");
        StringBuilder resultado = new StringBuilder();
        for (String palabra : palabras) {
            if (!palabra.isEmpty()) {
                resultado.append(Character.toUpperCase(palabra.charAt(0)))
                        .append(palabra.substring(1))
                        .append(" ");
            }
        }
        return resultado.toString().trim();
    }
}
