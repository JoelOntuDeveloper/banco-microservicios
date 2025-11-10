package banco.jontuna.ms_banco_cuenta.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class MovimientoRequestDTO {
    @NotNull(message = "El valor es obligatorio")
    @Digits(integer = 15, fraction = 2, message = "El valor debe tener máximo 15 enteros y 2 decimales")
    private BigDecimal valor;

    public MovimientoRequestDTO() {}

    public MovimientoRequestDTO(BigDecimal valor) {
        this.valor = valor;
    }
}