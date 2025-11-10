package banco.jontuna.ms_banco_cuenta.util.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class CuentasNoEncontradasException extends RuntimeException {
    
    public CuentasNoEncontradasException(String message) {
        super(message);
    }
    
    public CuentasNoEncontradasException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public static CuentasNoEncontradasException paraCliente(Long clienteId) {
        return new CuentasNoEncontradasException(
            String.format("No se encontraron cuentas activas para el cliente con ID: %d", clienteId)
        );
    }
    
    public static CuentasNoEncontradasException paraClienteEnRango(Long clienteId, String fechaInicio, String fechaFin) {
        return new CuentasNoEncontradasException(
            String.format("No se encontraron cuentas con movimientos para el cliente ID: %d en el rango %s a %s", 
                            clienteId, fechaInicio, fechaFin)
        );
    }
    
    public static CuentasNoEncontradasException cuentaEspecifica(Long cuentaId) {
        return new CuentasNoEncontradasException(
            String.format("No se encontró la cuenta con ID: %d", cuentaId)
        );
    }
    
    public static CuentasNoEncontradasException cuentaInactiva(Long cuentaId, String estado) {
        return new CuentasNoEncontradasException(
            String.format("La cuenta con ID: %d no está activa. Estado actual: %s", cuentaId, estado)
        );
    }
}