package banco.jontuna.ms_banco_cuenta.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import banco.jontuna.ms_banco_cuenta.dto.CuentaRequestDTO;
import banco.jontuna.ms_banco_cuenta.dto.CuentaResponseDTO;
import banco.jontuna.ms_banco_cuenta.service.CuentaService;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/cuentas")
@CrossOrigin(origins = "*")
public class CuentaController {

    @Autowired
    private CuentaService cuentaService;

    @PostMapping
    public ResponseEntity<CuentaResponseDTO> crearCuenta(@Valid @RequestBody CuentaRequestDTO cuentaRequest) {
        try {
            CuentaResponseDTO cuentaCreada = cuentaService.crearCuenta(cuentaRequest);
            return ResponseEntity.ok(cuentaCreada);
        } catch (RuntimeException e) {
            throw e;
        }
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<CuentaResponseDTO>> obtenerCuentasPorCliente(@PathVariable("clienteId") Long clienteId) {
        List<CuentaResponseDTO> cuentas = cuentaService.obtenerCuentasPorCliente(clienteId);
        return ResponseEntity.ok(cuentas);
    }

    @GetMapping("/{cuentaId}")
    public ResponseEntity<CuentaResponseDTO> obtenerCuentaPorId(@PathVariable("cuentaId") Long cuentaId) {
        CuentaResponseDTO cuenta = cuentaService.obtenerCuentaPorId(cuentaId);
        return ResponseEntity.ok(cuenta);
    }

    @GetMapping("/numero/{numeroCuenta}")
    public ResponseEntity<CuentaResponseDTO> obtenerCuentaPorNumero(@PathVariable("numeroCuenta") String numeroCuenta) {
        CuentaResponseDTO cuenta = cuentaService.obtenerCuentaPorNumero(numeroCuenta);
        return ResponseEntity.ok(cuenta);
    }

    @GetMapping
    public ResponseEntity<List<CuentaResponseDTO>> obtenerTodasLasCuentas() {
        List<CuentaResponseDTO> cuentas = cuentaService.obtenerTodasLasCuentas();
        return ResponseEntity.ok(cuentas);
    }

    @GetMapping("/{cuentaId}/saldo")
    public ResponseEntity<BigDecimal> consultarSaldo(@PathVariable("cuentaId") Long cuentaId) {
        BigDecimal saldo = cuentaService.consultarSaldo(cuentaId);
        return ResponseEntity.ok(saldo);
    }

    @PatchMapping("/{cuentaId}/estado")
    public ResponseEntity<CuentaResponseDTO> actualizarEstadoCuenta(
            @PathVariable("cuentaId") Long cuentaId,
            @RequestParam("estado") String estado) {
        CuentaResponseDTO cuentaActualizada = cuentaService.actualizarEstadoCuenta(cuentaId, estado);
        return ResponseEntity.ok(cuentaActualizada);
    }

    @DeleteMapping("/{cuentaId}")
    public ResponseEntity<CuentaResponseDTO> eliminarCuenta(@PathVariable("cuentaId") Long cuentaId) {
        CuentaResponseDTO cuentaActualizada = cuentaService.eliminarCuentaLogicamente(cuentaId);
        return ResponseEntity.ok(cuentaActualizada);
    }

    @PatchMapping("/{cuentaId}")
    public ResponseEntity<CuentaResponseDTO> reactivarCuenta(@PathVariable("cuentaId") Long cuentaId) {
        CuentaResponseDTO cuentaActualizada = cuentaService.reactivarCuenta(cuentaId);
        return ResponseEntity.ok(cuentaActualizada);
    }
}