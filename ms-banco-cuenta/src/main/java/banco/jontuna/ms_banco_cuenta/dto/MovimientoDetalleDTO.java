package banco.jontuna.ms_banco_cuenta.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class MovimientoDetalleDTO {
    private Long movimientoId;
    private LocalDateTime fecha;
    private String tipoMovimiento;
    private BigDecimal valor;
    private String descripcion;
    private BigDecimal saldoDespues;

    public MovimientoDetalleDTO() {}

    public MovimientoDetalleDTO(Long movimientoId, LocalDateTime fecha, String tipoMovimiento, 
                                BigDecimal valor, String descripcion, BigDecimal saldoDespues) {
        this.movimientoId = movimientoId;
        this.fecha = fecha;
        this.tipoMovimiento = tipoMovimiento;
        this.valor = valor;
        this.descripcion = descripcion;
        this.saldoDespues = saldoDespues;
    }
}