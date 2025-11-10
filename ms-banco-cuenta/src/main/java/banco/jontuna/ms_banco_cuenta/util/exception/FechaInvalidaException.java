package banco.jontuna.ms_banco_cuenta.util.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class FechaInvalidaException extends RuntimeException {
    
    public FechaInvalidaException(String message) {
        super(message);
    }
    
    public FechaInvalidaException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public static FechaInvalidaException fechasNulas() {
        return new FechaInvalidaException("Las fechas de inicio y fin son obligatorias");
    }
    
    public static FechaInvalidaException fechaInicioPosterior() {
        return new FechaInvalidaException("La fecha de inicio no puede ser posterior a la fecha fin");
    }
    
    public static FechaInvalidaException rangoExcesivo() {
        return new FechaInvalidaException("El rango de fechas no puede ser mayor a 1 año");
    }
    
    public static FechaInvalidaException fechasFuturas() {
        return new FechaInvalidaException("Las fechas no pueden ser futuras");
    }
    
    public static FechaInvalidaException formatoInvalido() {
        return new FechaInvalidaException("Formato de fecha inválido. Use el formato YYYY-MM-DD");
    }
}