package banco.jontuna.ms_banco_cuenta.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CuentaRequestDTO {
    @NotBlank(message = "El tipo de cuenta es obligatorio")
    private String tipoCuenta;

    @NotNull(message = "El saldo inicial es obligatorio")
    @DecimalMin(value = "0.00", message = "El saldo inicial no puede ser negativo")
    private BigDecimal saldoInicial;

    @NotNull(message = "El ID del cliente es obligatorio")
    private Long clienteId;

    public CuentaRequestDTO() {}

    public CuentaRequestDTO(String tipoCuenta, BigDecimal saldoInicial, Long clienteId) {
        this.tipoCuenta = tipoCuenta;
        this.saldoInicial = saldoInicial;
        this.clienteId = clienteId;
    }
}