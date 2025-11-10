package banco.jontuna.ms_banco_cuenta.controller;

import banco.jontuna.ms_banco_cuenta.dto.EstadoCuentaReporteDTO;
import banco.jontuna.ms_banco_cuenta.service.ReporteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    private static final Logger logger = LoggerFactory.getLogger(ReporteController.class);

    @Autowired
    private ReporteService reporteService;

    @GetMapping("/{clienteId}")
    public ResponseEntity<EstadoCuentaReporteDTO> generarReporteEstadoCuenta(
            @PathVariable("clienteId") Long clienteId,
            @RequestParam("fechaInicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam("fechaFin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {

        logger.info("Solicitando reporte de estado de cuenta - Cliente: {}, Fechas: {} a {}", 
                    clienteId, fechaInicio, fechaFin);

        EstadoCuentaReporteDTO reporte = reporteService.generarReporteEstadoCuenta(clienteId, fechaInicio, fechaFin);
        return ResponseEntity.ok(reporte);
    }
}