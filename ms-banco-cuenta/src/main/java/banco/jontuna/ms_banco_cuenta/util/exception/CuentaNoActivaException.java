package banco.jontuna.ms_banco_cuenta.util.exception;

public class CuentaNoActivaException extends RuntimeException {
    public CuentaNoActivaException(String message) {
        super(message);
    }
    
    public CuentaNoActivaException(String numeroCuenta, String estadoActual) {
        super(String.format("No se puede eliminar la cuenta %s porque no está activa. Estado actual: %s", 
                                numeroCuenta, estadoActual));
    }
}