package banco.jontuna.ms_banco_cuenta.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @NotBlank(message = "El número de cuenta es obligatorio")
    @Column(name = "NumeroCuenta", nullable = false, unique = true, length = 30)
    private String numeroCuenta;

    @NotBlank(message = "El tipo de cuenta es obligatorio")
    @Column(name = "TipoCuenta", nullable = false, length = 30)
    private String tipoCuenta;

    @NotNull(message = "El saldo inicial es obligatorio")
    @DecimalMin(value = "0.00", message = "El saldo inicial no puede ser negativo")
    @Column(name = "SaldoInicial", nullable = false, precision = 15, scale = 2)
    private BigDecimal saldoInicial = BigDecimal.ZERO;

    @Column(name = "Estado", length = 20)
    private String estado = "ACTIVA";

    @Column(name = "ClienteId", nullable = false)
    private Long clienteId;

    public Cuenta() {}

    public Cuenta(String numeroCuenta, String tipoCuenta, BigDecimal saldoInicial, Long clienteId) {
        this.numeroCuenta = numeroCuenta;
        this.tipoCuenta = tipoCuenta;
        this.saldoInicial = saldoInicial;
        this.clienteId = clienteId;
    }
}