package banco.jontuna.ms_banco_cliente.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import banco.jontuna.ms_banco_cliente.dto.ClienteRequestDTO;
import banco.jontuna.ms_banco_cliente.dto.ClienteResponseDTO;
import banco.jontuna.ms_banco_cliente.service.ClienteService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/clientes")
@CrossOrigin(origins = "*")
public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    @PostMapping
    public ResponseEntity<ClienteResponseDTO> crearCliente(@Valid @RequestBody ClienteRequestDTO clienteRequest) {
        ClienteResponseDTO clienteCreado = clienteService.crearCliente(clienteRequest);
        return ResponseEntity.ok(clienteCreado);
    }

    @GetMapping
    public ResponseEntity<List<ClienteResponseDTO>> obtenerTodosLosClientes() {
        List<ClienteResponseDTO> clientes = clienteService.obtenerTodosLosClientes();
        return ResponseEntity.ok(clientes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> obtenerClientePorId(@PathVariable("id") Long id) {
        ClienteResponseDTO cliente = clienteService.obtenerClientePorId(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado")); // Esto será manejado por el ExceptionHandler
        return ResponseEntity.ok(cliente);
    }

    @GetMapping("/identificacion/{identificacion}")
    public ResponseEntity<ClienteResponseDTO> obtenerClientePorIdentificacion(@PathVariable("identificacion") String identificacion) {
        ClienteResponseDTO cliente = clienteService.obtenerClientePorIdentificacion(identificacion)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado")); // Esto será manejado por el ExceptionHandler
        return ResponseEntity.ok(cliente);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> actualizarCliente(
            @PathVariable("id") Long id,
            @Valid @RequestBody ClienteRequestDTO clienteRequest) {
        ClienteResponseDTO clienteActualizado = clienteService.actualizarCliente(id, clienteRequest);
        return ResponseEntity.ok(clienteActualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivarCliente(@PathVariable("id") Long id) {
        clienteService.desactivarCliente(id);
        return ResponseEntity.ok().build();
    }
}