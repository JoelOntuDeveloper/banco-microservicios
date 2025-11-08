package banco.jontuna.ms_banco_cliente.util.mapper;

import org.springframework.stereotype.Component;

import banco.jontuna.ms_banco_cliente.dto.ClienteRequestDTO;
import banco.jontuna.ms_banco_cliente.dto.ClienteResponseDTO;
import banco.jontuna.ms_banco_cliente.dto.PersonaDTO;
import banco.jontuna.ms_banco_cliente.model.Cliente;
import banco.jontuna.ms_banco_cliente.model.Persona;

@Component
public class ClienteMapper {

    // Persona Mappings
    public Persona toEntity(PersonaDTO dto) {
        if (dto == null) return null;
        
        Persona entity = new Persona();
        entity.setPersonaId(dto.getPersonaId());
        entity.setIdentificacion(dto.getIdentificacion());
        entity.setNombre(dto.getNombre());
        entity.setGenero(dto.getGenero());
        entity.setEdad(dto.getEdad());
        entity.setDireccion(dto.getDireccion());
        entity.setTelefono(dto.getTelefono());
        return entity;
    }

    public PersonaDTO toDTO(Persona entity) {
        if (entity == null) return null;
        
        PersonaDTO dto = new PersonaDTO();
        dto.setPersonaId(entity.getPersonaId());
        dto.setIdentificacion(entity.getIdentificacion());
        dto.setNombre(entity.getNombre());
        dto.setGenero(entity.getGenero());
        dto.setEdad(entity.getEdad());
        dto.setDireccion(entity.getDireccion());
        dto.setTelefono(entity.getTelefono());
        return dto;
    }

    // DTO to Entity
    public Cliente toEntity(ClienteRequestDTO dto) {
        if (dto == null) return null;
        
        Cliente entity = new Cliente();
        entity.setContrasena(dto.getContrasena());
        entity.setPersona(toEntity(dto.getPersona()));
        return entity;
    }

    public ClienteResponseDTO toResponseDTO(Cliente entity) {
        if (entity == null) return null;
        
        ClienteResponseDTO dto = new ClienteResponseDTO();
        dto.setClienteId(entity.getClienteId());
        dto.setEstado(entity.getEstado());
        dto.setPersona(toDTO(entity.getPersona()));
        return dto;
    }
}