package banco.jontuna.ms_banco_cuenta.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import banco.jontuna.ms_banco_cuenta.dto.MovimientoRequestDTO;
import banco.jontuna.ms_banco_cuenta.dto.MovimientoResponseDTO;
import banco.jontuna.ms_banco_cuenta.service.MovimientoService;

import java.util.List;

@RestController
@RequestMapping("/api/movimientos")
@CrossOrigin(origins = "*")
public class MovimientoController {

    @Autowired
    private MovimientoService movimientoService;

    @PostMapping("/cuenta/{cuentaId}")
    public ResponseEntity<MovimientoResponseDTO> registrarMovimiento(
            @PathVariable("cuentaId") Long cuentaId,
            @Valid @RequestBody MovimientoRequestDTO movimientoRequest) {
        try {
            MovimientoResponseDTO movimiento = movimientoService.registrarMovimiento(cuentaId, movimientoRequest);
            return ResponseEntity.ok(movimiento);
        } catch (RuntimeException e) {
            throw e;
        }
    }

    @GetMapping("/cuenta/{cuentaId}")
    public ResponseEntity<List<MovimientoResponseDTO>> obtenerMovimientosPorCuenta(@PathVariable("cuentaId") Long cuentaId) {
        List<MovimientoResponseDTO> movimientos = movimientoService.obtenerMovimientosPorCuenta(cuentaId);
        return ResponseEntity.ok(movimientos);
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<MovimientoResponseDTO>> obtenerMovimientosPorCliente(@PathVariable("clienteId") Long clienteId) {
        List<MovimientoResponseDTO> movimientos = movimientoService.obtenerMovimientosPorCliente(clienteId);
        return ResponseEntity.ok(movimientos);
    }

    @GetMapping("/{movimientoId}")
    public ResponseEntity<MovimientoResponseDTO> obtenerMovimientoPorId(@PathVariable("movimientoId") Long movimientoId) {
        MovimientoResponseDTO movimiento = movimientoService.obtenerMovimientoPorId(movimientoId);
        return ResponseEntity.ok(movimiento);
    }

    @GetMapping
    public ResponseEntity<List<MovimientoResponseDTO>> obtenerTodosLosMovimientos() {
        List<MovimientoResponseDTO> movimientos = movimientoService.obtenerTodosLosMovimientos();
        return ResponseEntity.ok(movimientos);
    }
}