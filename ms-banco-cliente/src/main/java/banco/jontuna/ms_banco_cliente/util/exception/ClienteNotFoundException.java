package banco.jontuna.ms_banco_cliente.util.exception;

public class ClienteNotFoundException extends RuntimeException {
    public ClienteNotFoundException(String message) {
        super(message);
    }
    
    public ClienteNotFoundException(Long clienteId) {
        super("Cliente no encontrado con ID: " + clienteId);
    }
    
    public ClienteNotFoundException(String identificacion, boolean porIdentificacion) {
        super("Cliente no encontrado con identificación: " + identificacion);
    }
}