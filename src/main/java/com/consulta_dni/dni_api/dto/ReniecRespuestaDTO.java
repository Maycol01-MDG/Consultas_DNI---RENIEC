package com.consulta_dni.dni_api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReniecRespuestaDTO {

    @JsonProperty("nombres")
    @JsonAlias({"first_name", "name"})
    private String nombres;

    @JsonProperty("apellidoPa")
    @JsonAlias({"first_last_name", "apellidoPaterno"})
    private String apellidoPaterno;

    @JsonProperty("apellidoMa")
    @JsonAlias({"second_last_name", "apellidoMaterno"})
    private String apellidoMaterno;

    @JsonProperty("tipoDocumento")
    private String tipoDocumento;

    @JsonProperty("numeroDocumento")
    @JsonAlias({"document_number", "dni"})
    private String numeroDocumento;

    @JsonProperty("digitoVerificador")
    private String digitoVerificador;

    @JsonProperty("full_name")
    @JsonAlias({"fullName", "nombre"})
    private String nombreCompleto;
}
