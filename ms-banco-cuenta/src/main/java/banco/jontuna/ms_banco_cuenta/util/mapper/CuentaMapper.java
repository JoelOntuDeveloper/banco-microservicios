package banco.jontuna.ms_banco_cuenta.util.mapper;

import org.springframework.stereotype.Component;

import banco.jontuna.ms_banco_cuenta.dto.CuentaRequestDTO;
import banco.jontuna.ms_banco_cuenta.dto.CuentaResponseDTO;
import banco.jontuna.ms_banco_cuenta.model.Cuenta;

@Component
public class CuentaMapper {

    public Cuenta toEntity(CuentaRequestDTO dto) {
        if (dto == null) return null;
        
        Cuenta entity = new Cuenta();
        entity.setTipoCuenta(dto.getTipoCuenta());
        entity.setSaldoInicial(dto.getSaldoInicial());
        entity.setClienteId(dto.getClienteId());
        return entity;
    }

    public CuentaResponseDTO toResponseDTO(Cuenta entity) {
        if (entity == null) return null;
        
        CuentaResponseDTO dto = new CuentaResponseDTO();
        dto.setCuentaId(entity.getCuentaId());
        dto.setNumeroCuenta(entity.getNumeroCuenta());
        dto.setTipoCuenta(entity.getTipoCuenta());
        dto.setSaldoInicial(entity.getSaldoInicial());
        dto.setEstado(entity.getEstado());
        dto.setClienteId(entity.getClienteId());
        return dto;
    }
}