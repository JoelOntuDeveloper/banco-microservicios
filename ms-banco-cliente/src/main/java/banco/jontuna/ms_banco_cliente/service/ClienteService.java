package banco.jontuna.ms_banco_cliente.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import banco.jontuna.ms_banco_cliente.dto.ClienteRequestDTO;
import banco.jontuna.ms_banco_cliente.dto.ClienteResponseDTO;
import banco.jontuna.ms_banco_cliente.model.Cliente;
import banco.jontuna.ms_banco_cliente.model.Persona;
import banco.jontuna.ms_banco_cliente.repository.ClienteRepository;
import banco.jontuna.ms_banco_cliente.repository.PersonaRepository;
import banco.jontuna.ms_banco_cliente.util.exception.ClienteNotFoundException;
import banco.jontuna.ms_banco_cliente.util.exception.PersonaAlreadyExistsException;
import banco.jontuna.ms_banco_cliente.util.exception.ValidationException;
import banco.jontuna.ms_banco_cliente.util.mapper.ClienteMapper;

@Service
@Transactional
public class ClienteService {

    private static final Logger logger = LoggerFactory.getLogger(ClienteService.class);

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private ClienteMapper clienteMapper;

    @Autowired
    private ClienteEventPublisher clienteEventPublisher; // Inyectamos el publisher

    public ClienteResponseDTO crearCliente(ClienteRequestDTO clienteRequest) {
        logger.info("Iniciando creación de cliente: {}", clienteRequest.getPersona().getNombre());

        try {
            if (personaRepository.existsByIdentificacion(clienteRequest.getPersona().getIdentificacion())) {
                throw new PersonaAlreadyExistsException("identificación", clienteRequest.getPersona().getIdentificacion());
            }

            if (clienteRequest.getContrasena() == null || clienteRequest.getContrasena().trim().isEmpty()) {
                throw new ValidationException("La contraseña no puede estar vacía");
            }

            if (clienteRequest.getContrasena().length() < 6) {
                throw new ValidationException("La contraseña debe tener al menos 6 caracteres");
            }

            Cliente cliente = clienteMapper.toEntity(clienteRequest);

            Cliente clienteGuardado = clienteRepository.save(cliente);
            logger.info("Cliente guardado con ID: {}", clienteGuardado.getClienteId());

            // Publicar evento asíncrono - Con manejo de excepciones integrado
            clienteEventPublisher.publicarClienteCreado(
                clienteGuardado.getClienteId(),
                clienteGuardado.getPersona().getNombre(),
                clienteGuardado.getPersona().getIdentificacion()
            );

            return clienteMapper.toResponseDTO(clienteGuardado);

        } catch (PersonaAlreadyExistsException | ValidationException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error inesperado al crear cliente", e);
            throw new RuntimeException("Error inesperado al crear el cliente: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<ClienteResponseDTO> obtenerTodosLosClientes() {
        try {
            return clienteRepository.findAll()
                    .stream()
                    .map(clienteMapper::toResponseDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error al obtener todos los clientes", e);
            throw new RuntimeException("Error al obtener la lista de clientes");
        }
    }

    @Transactional(readOnly = true)
    public Optional<ClienteResponseDTO> obtenerClientePorId(Long id) {
        try {
            Optional<Cliente> cliente = clienteRepository.findById(id);
            if (cliente.isEmpty()) {
                throw new ClienteNotFoundException(id);
            }
            return cliente.map(clienteMapper::toResponseDTO);
        } catch (ClienteNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error al obtener cliente por ID: {}", id, e);
            throw new RuntimeException("Error al obtener el cliente");
        }
    }

    @Transactional(readOnly = true)
    public Optional<ClienteResponseDTO> obtenerClientePorIdentificacion(String identificacion) {
        try {
            Optional<Cliente> cliente = clienteRepository.findByPersonaIdentificacion(identificacion);
            if (cliente.isEmpty()) {
                throw new ClienteNotFoundException(identificacion, true);
            }
            return cliente.map(clienteMapper::toResponseDTO);
        } catch (ClienteNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error al obtener cliente por identificación: {}", identificacion, e);
            throw new RuntimeException("Error al obtener el cliente por identificación");
        }
    }

    public ClienteResponseDTO actualizarCliente(Long id, ClienteRequestDTO clienteRequest) {
        try {
            return clienteRepository.findById(id)
                .map(clienteExistente -> {
                    if (clienteRequest.getContrasena() != null) {
                        if (clienteRequest.getContrasena().trim().isEmpty()) {
                            throw new ValidationException("La contraseña no puede estar vacía");
                        }
                        if (clienteRequest.getContrasena().length() < 6) {
                            throw new ValidationException("La contraseña debe tener al menos 6 caracteres");
                        }
                        clienteExistente.setContrasena(clienteRequest.getContrasena());
                    }
                    
                    Persona persona = clienteExistente.getPersona();
                    if (clienteRequest.getPersona().getNombre() != null) {
                        persona.setNombre(clienteRequest.getPersona().getNombre());
                    }
                    if (clienteRequest.getPersona().getGenero() != null) {
                        persona.setGenero(clienteRequest.getPersona().getGenero());
                    }
                    if (clienteRequest.getPersona().getEdad() != null) {
                        persona.setEdad(clienteRequest.getPersona().getEdad());
                    }
                    if (clienteRequest.getPersona().getDireccion() != null) {
                        persona.setDireccion(clienteRequest.getPersona().getDireccion());
                    }
                    if (clienteRequest.getPersona().getTelefono() != null) {
                        persona.setTelefono(clienteRequest.getPersona().getTelefono());
                    }
                    
                    Cliente clienteActualizado = clienteRepository.save(clienteExistente);
                    logger.info("Cliente ID: {} actualizado exitosamente", id);
                    
                    return clienteMapper.toResponseDTO(clienteActualizado);
                })
                .orElseThrow(() -> new ClienteNotFoundException(id));
        } catch (ClienteNotFoundException | ValidationException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error al actualizar cliente ID: {}", id, e);
            throw new RuntimeException("Error al actualizar el cliente");
        }
    }

    public void desactivarCliente(Long id) {
        try {
            Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ClienteNotFoundException(id));
            
            if ("INACTIVO".equals(cliente.getEstado())) {
                throw new ValidationException("El cliente ya se encuentra INACTIVO");
            }
            
            cliente.setEstado("INACTIVO");
            clienteRepository.save(cliente);
            logger.info("Cliente ID: {} desactivado exitosamente", id);
            
        } catch (ClienteNotFoundException | ValidationException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error al desactivar cliente ID: {}", id, e);
            throw new RuntimeException("Error al desactivar el cliente");
        }
    }

    public boolean existeClientePorIdentificacion(String identificacion) {
        try {
            return clienteRepository.existsByPersonaIdentificacion(identificacion);
        } catch (Exception e) {
            logger.error("Error al verificar existencia de cliente con identificación: {}", identificacion, e);
            throw new RuntimeException("Error al verificar la existencia del cliente");
        }
    }
}
