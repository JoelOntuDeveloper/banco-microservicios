package banco.jontuna.ms_banco_cuenta.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "CUENTA")
public class Cuenta {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CuentaId")
    private Long cuentaId;
    
    @Column(name = "NumeroCuenta", unique = true, nullable = false, length = 30)
    private String numeroCuenta;
    
    @Column(name = "TipoCuenta", nullable = false, length = 30)
    private String tipoCuenta;
    
    @Column(name = "SaldoInicial", nullable = false, precision = 15, scale = 2)
    private BigDecimal saldoInicial;
    
    @Column(name = "Estado", length = 20)
    private String estado;
}