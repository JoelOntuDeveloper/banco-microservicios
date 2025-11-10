package banco.jontuna.ms_banco_cuenta.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class CuentaResponseDTO {
    private Long cuentaId;
    private String numeroCuenta;
    private String tipoCuenta;
    private BigDecimal saldoInicial;
    private String estado;
    private Long clienteId;

    public CuentaResponseDTO() {}

    public CuentaResponseDTO(Long cuentaId, String numeroCuenta, String tipoCuenta, 
                            BigDecimal saldoInicial, String estado, Long clienteId) {
        this.cuentaId = cuentaId;
        this.numeroCuenta = numeroCuenta;
        this.tipoCuenta = tipoCuenta;
        this.saldoInicial = saldoInicial;
        this.estado = estado;
        this.clienteId = clienteId;
    }
}