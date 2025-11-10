package banco.jontuna.ms_banco_cuenta.util.exception;

public class CuentaInactivaException extends RuntimeException {
    public CuentaInactivaException(String message) {
        super(message);
    }
    
    public CuentaInactivaException(String numeroCuenta, String estado) {
        super("La cuenta " + numeroCuenta + " no está activa. Estado actual: " + estado);
    }
}