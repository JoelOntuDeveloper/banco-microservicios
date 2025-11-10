package banco.jontuna.ms_banco_cliente.service;

import banco.jontuna.ms_banco_cliente.dto.ClienteRequestDTO;
import banco.jontuna.ms_banco_cliente.dto.ClienteResponseDTO;
import banco.jontuna.ms_banco_cliente.dto.PersonaDTO;
import banco.jontuna.ms_banco_cliente.model.Cliente;
import banco.jontuna.ms_banco_cliente.model.Persona;
import banco.jontuna.ms_banco_cliente.repository.ClienteRepository;
import banco.jontuna.ms_banco_cliente.repository.PersonaRepository;
import banco.jontuna.ms_banco_cliente.util.exception.ClienteNotFoundException;
import banco.jontuna.ms_banco_cliente.util.exception.PersonaAlreadyExistsException;
import banco.jontuna.ms_banco_cliente.util.exception.ValidationException;
import banco.jontuna.ms_banco_cliente.util.mapper.ClienteMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private PersonaRepository personaRepository;

    @Mock
    private ClienteMapper clienteMapper;

    @InjectMocks
    private ClienteService clienteService;

    @Test
    void crearCliente_ValidCliente_ShouldReturnClienteResponseDTO() {
        // Arrange
        ClienteRequestDTO request = createValidClienteRequest();
        Cliente cliente = createValidCliente();
        Cliente clienteGuardado = createValidCliente();
        clienteGuardado.setClienteId(1L);
        ClienteResponseDTO expectedResponse = new ClienteResponseDTO();

        when(personaRepository.existsByIdentificacion(anyString())).thenReturn(false);
        when(clienteMapper.toEntity(request)).thenReturn(cliente);
        when(clienteRepository.save(cliente)).thenReturn(clienteGuardado);
        when(clienteMapper.toResponseDTO(clienteGuardado)).thenReturn(expectedResponse);

        // Act
        ClienteResponseDTO result = clienteService.crearCliente(request);

        // Assert
        assertNotNull(result);
        verify(personaRepository).existsByIdentificacion(anyString());
        verify(clienteRepository).save(cliente);
    }

    @Test
    void crearCliente_PersonaAlreadyExists_ShouldThrowPersonaAlreadyExistsException() {
        // Arrange
        ClienteRequestDTO request = createValidClienteRequest();
        when(personaRepository.existsByIdentificacion(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(PersonaAlreadyExistsException.class, () -> {
            clienteService.crearCliente(request);
        });
    }

    @Test
    void crearCliente_EmptyPassword_ShouldThrowValidationException() {
        // Arrange
        ClienteRequestDTO request = createValidClienteRequest();
        request.setContrasena("");

        when(personaRepository.existsByIdentificacion(anyString())).thenReturn(false);

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            clienteService.crearCliente(request);
        });
    }

    @Test
    void crearCliente_ShortPassword_ShouldThrowValidationException() {
        // Arrange
        ClienteRequestDTO request = createValidClienteRequest();
        request.setContrasena("123");

        when(personaRepository.existsByIdentificacion(anyString())).thenReturn(false);

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            clienteService.crearCliente(request);
        });
    }

    @Test
    void obtenerTodosLosClientes_ExistingClients_ShouldReturnClientesList() {
        // Arrange
        Cliente cliente1 = createValidCliente();
        Cliente cliente2 = createValidCliente();
        cliente2.setClienteId(2L);
        List<Cliente> clientes = Arrays.asList(cliente1, cliente2);
        ClienteResponseDTO responseDTO = new ClienteResponseDTO();

        when(clienteRepository.findAll()).thenReturn(clientes);
        when(clienteMapper.toResponseDTO(any(Cliente.class))).thenReturn(responseDTO);

        // Act
        List<ClienteResponseDTO> result = clienteService.obtenerTodosLosClientes();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(clienteRepository).findAll();
        verify(clienteMapper, times(2)).toResponseDTO(any(Cliente.class));
    }

    @Test
    void obtenerClientePorId_ValidId_ShouldReturnClienteResponseDTO() {
        // Arrange
        Long clienteId = 1L;
        Cliente cliente = createValidCliente();
        ClienteResponseDTO expectedResponse = new ClienteResponseDTO();

        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));
        when(clienteMapper.toResponseDTO(cliente)).thenReturn(expectedResponse);

        // Act
        Optional<ClienteResponseDTO> result = clienteService.obtenerClientePorId(clienteId);

        // Assert
        assertTrue(result.isPresent());
        verify(clienteRepository).findById(clienteId);
    }

    @Test
    void obtenerClientePorId_InvalidId_ShouldThrowClienteNotFoundException() {
        // Arrange
        Long clienteId = 999L;
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ClienteNotFoundException.class, () -> {
            clienteService.obtenerClientePorId(clienteId);
        });
    }

    @Test
    void obtenerClientePorIdentificacion_ValidIdentificacion_ShouldReturnClienteResponseDTO() {
        // Arrange
        String identificacion = "1234567890";
        Cliente cliente = createValidCliente();
        ClienteResponseDTO expectedResponse = new ClienteResponseDTO();

        when(clienteRepository.findByPersonaIdentificacion(identificacion)).thenReturn(Optional.of(cliente));
        when(clienteMapper.toResponseDTO(cliente)).thenReturn(expectedResponse);

        // Act
        Optional<ClienteResponseDTO> result = clienteService.obtenerClientePorIdentificacion(identificacion);

        // Assert
        assertTrue(result.isPresent());
        verify(clienteRepository).findByPersonaIdentificacion(identificacion);
    }

    @Test
    void obtenerClientePorIdentificacion_InvalidIdentificacion_ShouldThrowClienteNotFoundException() {
        // Arrange
        String identificacion = "invalid";
        when(clienteRepository.findByPersonaIdentificacion(identificacion)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ClienteNotFoundException.class, () -> {
            clienteService.obtenerClientePorIdentificacion(identificacion);
        });
    }

    @Test
    void actualizarCliente_ValidUpdate_ShouldReturnUpdatedClienteResponseDTO() {
        // Arrange
        Long clienteId = 1L;
        ClienteRequestDTO request = createValidClienteRequest();
        request.setContrasena("newPassword123");
        
        Cliente clienteExistente = createValidCliente();
        Cliente clienteActualizado = createValidCliente();
        clienteActualizado.setContrasena("newPassword123");
        ClienteResponseDTO expectedResponse = new ClienteResponseDTO();

        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(clienteExistente));
        when(clienteRepository.save(clienteExistente)).thenReturn(clienteActualizado);
        when(clienteMapper.toResponseDTO(clienteActualizado)).thenReturn(expectedResponse);

        // Act
        ClienteResponseDTO result = clienteService.actualizarCliente(clienteId, request);

        // Assert
        assertNotNull(result);
        verify(clienteRepository).findById(clienteId);
        verify(clienteRepository).save(clienteExistente);
    }

    @Test
    void actualizarCliente_InvalidId_ShouldThrowClienteNotFoundException() {
        // Arrange
        Long clienteId = 999L;
        ClienteRequestDTO request = createValidClienteRequest();
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ClienteNotFoundException.class, () -> {
            clienteService.actualizarCliente(clienteId, request);
        });
    }

    @Test
    void actualizarCliente_EmptyPassword_ShouldThrowValidationException() {
        // Arrange
        Long clienteId = 1L;
        ClienteRequestDTO request = createValidClienteRequest();
        request.setContrasena("");
        
        Cliente clienteExistente = createValidCliente();
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(clienteExistente));

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            clienteService.actualizarCliente(clienteId, request);
        });
    }

    @Test
    void actualizarCliente_ShortPassword_ShouldThrowValidationException() {
        // Arrange
        Long clienteId = 1L;
        ClienteRequestDTO request = createValidClienteRequest();
        request.setContrasena("123");
        
        Cliente clienteExistente = createValidCliente();
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(clienteExistente));

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            clienteService.actualizarCliente(clienteId, request);
        });
    }

    @Test
    void actualizarCliente_PartialUpdate_ShouldUpdateOnlyProvidedFields() {
        // Arrange
        Long clienteId = 1L;
        ClienteRequestDTO request = new ClienteRequestDTO();
        request.setContrasena(null); // No actualizar contraseña
        
        PersonaDTO personaPartial = new PersonaDTO();
        personaPartial.setNombre("Nuevo Nombre");
        personaPartial.setGenero(null); // No actualizar género
        personaPartial.setEdad(null); // No actualizar edad
        request.setPersona(personaPartial);
        
        Cliente clienteExistente = createValidCliente();
        Cliente clienteActualizado = createValidCliente();
        clienteActualizado.getPersona().setNombre("Nuevo Nombre");
        ClienteResponseDTO expectedResponse = new ClienteResponseDTO();

        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(clienteExistente));
        when(clienteRepository.save(clienteExistente)).thenReturn(clienteActualizado);
        when(clienteMapper.toResponseDTO(clienteActualizado)).thenReturn(expectedResponse);

        // Act
        ClienteResponseDTO result = clienteService.actualizarCliente(clienteId, request);

        // Assert
        assertNotNull(result);
        assertEquals("Nuevo Nombre", clienteExistente.getPersona().getNombre());
        // Verificar que la contraseña no se cambió
        assertNotEquals("newPassword123", clienteExistente.getContrasena());
    }

    @Test
    void desactivarCliente_ActiveCliente_ShouldDeactivateCliente() {
        // Arrange
        Long clienteId = 1L;
        Cliente cliente = createValidCliente();
        cliente.setEstado("ACTIVO");

        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));
        when(clienteRepository.save(cliente)).thenReturn(cliente);

        // Act
        clienteService.desactivarCliente(clienteId);

        // Assert
        assertEquals("INACTIVO", cliente.getEstado());
        verify(clienteRepository).save(cliente);
    }

    @Test
    void desactivarCliente_InvalidId_ShouldThrowClienteNotFoundException() {
        // Arrange
        Long clienteId = 999L;
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ClienteNotFoundException.class, () -> {
            clienteService.desactivarCliente(clienteId);
        });
    }

    @Test
    void desactivarCliente_AlreadyInactive_ShouldThrowValidationException() {
        // Arrange
        Long clienteId = 1L;
        Cliente cliente = createValidCliente();
        cliente.setEstado("INACTIVO");

        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            clienteService.desactivarCliente(clienteId);
        });
    }

    @Test
    void existeClientePorIdentificacion_ExistingIdentificacion_ShouldReturnTrue() {
        // Arrange
        String identificacion = "1234567890";
        when(clienteRepository.existsByPersonaIdentificacion(identificacion)).thenReturn(true);

        // Act
        boolean result = clienteService.existeClientePorIdentificacion(identificacion);

        // Assert
        assertTrue(result);
        verify(clienteRepository).existsByPersonaIdentificacion(identificacion);
    }

    @Test
    void existeClientePorIdentificacion_NonExistingIdentificacion_ShouldReturnFalse() {
        // Arrange
        String identificacion = "0000000000";
        when(clienteRepository.existsByPersonaIdentificacion(identificacion)).thenReturn(false);

        // Act
        boolean result = clienteService.existeClientePorIdentificacion(identificacion);

        // Assert
        assertFalse(result);
    }

    private ClienteRequestDTO createValidClienteRequest() {
        ClienteRequestDTO request = new ClienteRequestDTO();
        request.setContrasena("validPassword123");
        
        PersonaDTO persona = new PersonaDTO();
        persona.setIdentificacion("1234567890");
        persona.setNombre("Juan Pérez");
        persona.setGenero("M");
        persona.setEdad(30);
        persona.setDireccion("Calle Principal 123");
        persona.setTelefono("0987654321");
        
        request.setPersona(persona);
        return request;
    }

    private Cliente createValidCliente() {
        Cliente cliente = new Cliente();
        cliente.setClienteId(1L);
        cliente.setContrasena("validPassword123");
        cliente.setEstado("ACTIVO");
        
        Persona persona = new Persona();
        persona.setIdentificacion("1234567890");
        persona.setNombre("Juan Pérez");
        persona.setGenero("M");
        persona.setEdad(30);
        persona.setDireccion("Calle Principal 123");
        persona.setTelefono("0987654321");
        
        cliente.setPersona(persona);
        return cliente;
    }
}