package banco.jontuna.ms_banco_cliente.dto;

import lombok.Data;

@Data
public class ClienteResponseDTO {
    private Long clienteId;
    private String estado;
    private PersonaDTO persona;

    // Constructores
    public ClienteResponseDTO() {}

    public ClienteResponseDTO(Long clienteId, String estado, PersonaDTO persona) {
        this.clienteId = clienteId;
        this.estado = estado;
        this.persona = persona;
    }
}