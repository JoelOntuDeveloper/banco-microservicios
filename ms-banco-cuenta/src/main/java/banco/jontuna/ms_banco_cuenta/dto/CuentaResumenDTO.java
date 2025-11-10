package banco.jontuna.ms_banco_cuenta.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;

@Data
public class CuentaResumenDTO {
    private Long cuentaId;
    private String numeroCuenta;
    private String tipoCuenta;
    private String estado;
    private BigDecimal saldoActual;
    private List<MovimientoDetalleDTO> movimientos;

    public CuentaResumenDTO() {}

    public CuentaResumenDTO(Long cuentaId, String numeroCuenta, String tipoCuenta, 
                            String estado, BigDecimal saldoActual) {
        this.cuentaId = cuentaId;
        this.numeroCuenta = numeroCuenta;
        this.tipoCuenta = tipoCuenta;
        this.estado = estado;
        this.saldoActual = saldoActual;
    }
}