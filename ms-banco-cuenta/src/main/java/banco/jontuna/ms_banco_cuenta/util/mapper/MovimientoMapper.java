package banco.jontuna.ms_banco_cuenta.util.mapper;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import banco.jontuna.ms_banco_cuenta.dto.MovimientoRequestDTO;
import banco.jontuna.ms_banco_cuenta.dto.MovimientoResponseDTO;
import banco.jontuna.ms_banco_cuenta.model.Cuenta;
import banco.jontuna.ms_banco_cuenta.model.Movimiento;

@Component
public class MovimientoMapper {

    public Movimiento toEntity(MovimientoRequestDTO dto, Cuenta cuenta) {
        Movimiento entity = new Movimiento();
        entity.setCuenta(cuenta);
        return entity;
    }

    public MovimientoResponseDTO toResponseDTO(Movimiento entity) {
        if (entity == null) return null;
        
        MovimientoResponseDTO dto = new MovimientoResponseDTO();
        dto.setMovimientoId(entity.getMovimientoId());
        dto.setFecha(entity.getFecha());
        dto.setTipoMovimiento(entity.getTipoMovimiento());
        
        BigDecimal valorRespuesta = entity.getValor();
        if ("RETIRO".equals(entity.getTipoMovimiento())) {
            valorRespuesta = entity.getValor().negate();
        }
        dto.setValor(valorRespuesta);
        
        dto.setSaldo(entity.getSaldo());
        dto.setCuentaId(entity.getCuenta().getCuentaId());
        dto.setNumeroCuenta(entity.getCuenta().getNumeroCuenta());
        return dto;
    }
}