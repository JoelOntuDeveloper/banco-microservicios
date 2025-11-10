package banco.jontuna.ms_banco_cuenta.handler;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import banco.jontuna.ms_banco_cuenta.dto.ErrorResponse;
import banco.jontuna.ms_banco_cuenta.util.exception.CuentaNoActivaException;
import banco.jontuna.ms_banco_cuenta.util.exception.CuentaConSaldoException;
import banco.jontuna.ms_banco_cuenta.util.exception.CuentaInactivaException;
import banco.jontuna.ms_banco_cuenta.util.exception.CuentaNotFoundException;
import banco.jontuna.ms_banco_cuenta.util.exception.MovimientoInvalidoException;
import banco.jontuna.ms_banco_cuenta.util.exception.SaldoInsuficienteException;
import banco.jontuna.ms_banco_cuenta.util.exception.FechaInvalidaException;
import banco.jontuna.ms_banco_cuenta.util.exception.CuentasNoEncontradasException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        String errorDetails = String.join(", ", errors.values());
        
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Error de Validación",
            "Los datos proporcionados no son válidos",
            request.getRequestURI(),
            errorDetails
        );

        logger.warn("Error de validación en Cuenta-Service: {}", errorDetails);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(CuentaNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCuentaNotFoundException(
            CuentaNotFoundException ex, HttpServletRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            "Cuenta No Encontrada",
            ex.getMessage(),
            request.getRequestURI()
        );

        logger.warn("Cuenta no encontrada: {}", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(CuentasNoEncontradasException.class)
    public ResponseEntity<ErrorResponse> handleCuentasNoEncontradasException(
            CuentasNoEncontradasException ex, HttpServletRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            "Cuentas No Encontradas",
            ex.getMessage(),
            request.getRequestURI()
        );

        logger.warn("Cuentas no encontradas: {}", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(FechaInvalidaException.class)
    public ResponseEntity<ErrorResponse> handleFechaInvalidaException(
            FechaInvalidaException ex, HttpServletRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Fecha Inválida",
            ex.getMessage(),
            request.getRequestURI()
        );

        logger.warn("Fecha inválida: {}", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(SaldoInsuficienteException.class)
    public ResponseEntity<ErrorResponse> handleSaldoInsuficienteException(
            SaldoInsuficienteException ex, HttpServletRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Saldo no disponible",
            ex.getMessage(),
            request.getRequestURI()
        );

        logger.warn("Saldo no disponible: {}", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(CuentaInactivaException.class)
    public ResponseEntity<ErrorResponse> handleCuentaInactivaException(
            CuentaInactivaException ex, HttpServletRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Cuenta Inactiva",
            ex.getMessage(),
            request.getRequestURI()
        );

        logger.warn("Cuenta inactiva: {}", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MovimientoInvalidoException.class)
    public ResponseEntity<ErrorResponse> handleMovimientoInvalidoException(
            MovimientoInvalidoException ex, HttpServletRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Movimiento Inválido",
            ex.getMessage(),
            request.getRequestURI()
        );

        logger.warn("Movimiento inválido: {}", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(CuentaConSaldoException.class)
    public ResponseEntity<ErrorResponse> handleCuentaConSaldoException(
            CuentaConSaldoException ex, HttpServletRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Cuenta con Saldo",
            ex.getMessage(),
            request.getRequestURI()
        );

        logger.warn("Cuenta con saldo: {}", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(CuentaNoActivaException.class)
    public ResponseEntity<ErrorResponse> handleCuentaConMovimientosRecientesException(
            CuentaNoActivaException ex, HttpServletRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Cuenta con Movimientos Recientes",
            ex.getMessage(),
            request.getRequestURI()
        );

        logger.warn("Cuenta con movimientos recientes: {}", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(
            RuntimeException ex, HttpServletRequest request) {
        
        logger.error("Runtime exception en Cuenta-Service: ", ex);

        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Error en la Aplicación",
            ex.getMessage(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex, HttpServletRequest request) {
        
        logger.error("Error interno del servidor en Cuenta-Service: ", ex);

        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Error Interno del Servidor",
            "Ocurrió un error inesperado en el servicio de cuentas. Por favor, contacte al administrador.",
            request.getRequestURI()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}