package banco.jontuna.ms_banco_cuenta.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class MovimientoResponseDTO {
    private Long movimientoId;
    private LocalDateTime fecha;
    private String tipoMovimiento;
    private BigDecimal valor;
    private BigDecimal saldo;
    private Long cuentaId;
    private String numeroCuenta;

    public MovimientoResponseDTO() {}

    public MovimientoResponseDTO(Long movimientoId, LocalDateTime fecha, String tipoMovimiento, 
                                BigDecimal valor, BigDecimal saldo, Long cuentaId, String numeroCuenta) {
        this.movimientoId = movimientoId;
        this.fecha = fecha;
        this.tipoMovimiento = tipoMovimiento;
        this.valor = valor;
        this.saldo = saldo;
        this.cuentaId = cuentaId;
        this.numeroCuenta = numeroCuenta;
    }
}