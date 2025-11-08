package banco.jontuna.ms_banco_cliente.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ClienteRequestDTO {
    @NotNull(message = "Los datos de la persona son obligatorios")
    private PersonaDTO persona;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String contrasena;

    // Constructores
    public ClienteRequestDTO() {}

    public ClienteRequestDTO(PersonaDTO persona, String contrasena) {
        this.persona = persona;
        this.contrasena = contrasena;
    }

}