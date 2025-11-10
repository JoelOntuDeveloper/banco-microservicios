package banco.jontuna.ms_banco_cuenta.util.exception;

import java.math.BigDecimal;

public class SaldoInsuficienteException extends RuntimeException {
    public SaldoInsuficienteException(String message) {
        super(message);
    }
    
    public SaldoInsuficienteException(BigDecimal saldoActual, BigDecimal valorRetiro) {
        super(String.format("Saldo no disponible. Saldo actual: %.2f, Valor a retirar: %.2f", 
                            saldoActual, valorRetiro));
    }
}