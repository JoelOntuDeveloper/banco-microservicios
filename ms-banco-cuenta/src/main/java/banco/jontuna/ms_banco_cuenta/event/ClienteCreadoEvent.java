package banco.jontuna.ms_banco_cuenta.event;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ClienteCreadoEvent implements Serializable {
    private Long clienteId;
    private String nombre;
    private String identificacion;
    private LocalDateTime fechaCreacion;

    public ClienteCreadoEvent() {}

    public ClienteCreadoEvent(Long clienteId, String nombre, String identificacion) {
        this.clienteId = clienteId;
        this.nombre = nombre;
        this.identificacion = identificacion;
        this.fechaCreacion = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "ClienteCreadoEvent{" +
                "clienteId=" + clienteId +
                ", nombre='" + nombre + '\'' +
                ", identificacion='" + identificacion + '\'' +
                ", fechaCreacion=" + fechaCreacion +
                '}';
    }
}