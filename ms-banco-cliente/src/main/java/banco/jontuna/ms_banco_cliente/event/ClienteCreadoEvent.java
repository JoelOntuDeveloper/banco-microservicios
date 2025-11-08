package banco.jontuna.ms_banco_cliente.event;

import lombok.Data;

@Data
public class ClienteCreadoEvent {
    private Long clienteId;
    private String nombreCliente;
    private String identificacion;
    private String estado;

    public ClienteCreadoEvent() {}

    public ClienteCreadoEvent(Long clienteId, String nombreCliente, String identificacion, String estado) {
        this.clienteId = clienteId;
        this.nombreCliente = nombreCliente;
        this.identificacion = identificacion;
        this.estado = estado;
    }

    // Getters y Setters
    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }
    public String getNombreCliente() { return nombreCliente; }
    public void setNombreCliente(String nombreCliente) { this.nombreCliente = nombreCliente; }
    public String getIdentificacion() { return identificacion; }
    public void setIdentificacion(String identificacion) { this.identificacion = identificacion; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}