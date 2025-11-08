package banco.jontuna.ms_banco_cuenta.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "MOVIMIENTO")
public class Movimiento {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "movimientoId")
    private Long movimientoId;
    
    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;
    
    @Column(name = "tipoMovimiento", nullable = false, length = 30)
    private String tipoMovimiento;
    
    @Column(name = "valor", nullable = false, precision = 15, scale = 2)
    private BigDecimal valor;
    
    @Column(name = "saldo", nullable = false, precision = 15, scale = 2)
    private BigDecimal saldo;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cuentaId", nullable = false)
    private Cuenta cuenta;
}
