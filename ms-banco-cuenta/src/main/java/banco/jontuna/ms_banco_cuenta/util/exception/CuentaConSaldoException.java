package banco.jontuna.ms_banco_cuenta.util.exception;

import java.math.BigDecimal;

public class CuentaConSaldoException extends RuntimeException {
    public CuentaConSaldoException(String message) {
        super(message);
    }
    
    public CuentaConSaldoException(String numeroCuenta, BigDecimal saldo) {
        super(String.format("La cuenta %s no puede ser eliminada porque tiene saldo pendiente: %.2f", 
                            numeroCuenta, saldo));
    }
}