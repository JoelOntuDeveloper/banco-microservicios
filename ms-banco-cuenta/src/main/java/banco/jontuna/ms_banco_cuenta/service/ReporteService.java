package banco.jontuna.ms_banco_cuenta.service;

import banco.jontuna.ms_banco_cuenta.dto.CuentaResumenDTO;
import banco.jontuna.ms_banco_cuenta.dto.EstadoCuentaReporteDTO;
import banco.jontuna.ms_banco_cuenta.dto.MovimientoDetalleDTO;
import banco.jontuna.ms_banco_cuenta.model.Cuenta;
import banco.jontuna.ms_banco_cuenta.model.Movimiento;
import banco.jontuna.ms_banco_cuenta.repository.CuentaRepository;
import banco.jontuna.ms_banco_cuenta.repository.custom.CustomMovimientoRepository;
import banco.jontuna.ms_banco_cuenta.util.exception.FechaInvalidaException;
import banco.jontuna.ms_banco_cuenta.util.exception.CuentasNoEncontradasException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ReporteService {

    private static final Logger logger = LoggerFactory.getLogger(ReporteService.class);

    @Autowired
    private CuentaRepository cuentaRepository;

    @Autowired
    private CustomMovimientoRepository customMovimientoRepository;

    @Autowired
    private MovimientoService movimientoService;

    public EstadoCuentaReporteDTO generarReporteEstadoCuenta(Long clienteId, LocalDate fechaInicio, LocalDate fechaFin) {
        try {
            logger.info("Generando reporte de estado de cuenta para cliente: {} entre {} y {}", 
                        clienteId, fechaInicio, fechaFin);

            validarFechas(fechaInicio, fechaFin);

            List<Cuenta> cuentas = cuentaRepository.findByClienteIdAndEstado(clienteId, "ACTIVA");
            
            if (cuentas.isEmpty()) {
                logger.warn("No se encontraron cuentas activas para el cliente: {}", clienteId);
                throw CuentasNoEncontradasException.paraCliente(clienteId);
            }

            LocalDateTime fechaInicioTime = fechaInicio.atStartOfDay();
            LocalDateTime fechaFinTime = fechaFin.atTime(LocalTime.MAX);

            List<Movimiento> movimientos = customMovimientoRepository
                    .findMovimientosByClienteAndFechaRange(clienteId, fechaInicioTime, fechaFinTime);

            if (movimientos.isEmpty()) {
                logger.warn("No se encontraron movimientos para el cliente {} en el rango {} - {}", 
                            clienteId, fechaInicio, fechaFin);
                throw CuentasNoEncontradasException.paraClienteEnRango(
                    clienteId, fechaInicio.toString(), fechaFin.toString());
            }

            Map<Long, List<Movimiento>> movimientosPorCuenta = movimientos.stream()
                    .collect(Collectors.groupingBy(m -> m.getCuenta().getCuentaId()));

            List<CuentaResumenDTO> cuentasDTO = cuentas.stream()
                    .map(cuenta -> {
                        CuentaResumenDTO cuentaDTO = convertirCuentaADTO(cuenta);
                        
                        List<Movimiento> movimientosCuenta = movimientosPorCuenta.getOrDefault(cuenta.getCuentaId(), new ArrayList<>());
                        List<MovimientoDetalleDTO> movimientosDTO = movimientosCuenta.stream()
                                .map(this::convertirMovimientoADTO)
                                .collect(Collectors.toList());
                        
                        cuentaDTO.setMovimientos(movimientosDTO);
                        return cuentaDTO;
                    })
                    .collect(Collectors.toList());

            if (cuentasDTO.isEmpty()) {
                throw CuentasNoEncontradasException.paraClienteEnRango(
                    clienteId, fechaInicio.toString(), fechaFin.toString());
            }

            EstadoCuentaReporteDTO reporte = new EstadoCuentaReporteDTO(clienteId, fechaInicio, fechaFin, cuentasDTO);
            
            logger.info("Reporte generado exitosamente para cliente: {}. Total cuentas: {}, Saldo total: {}", 
                        clienteId, cuentasDTO.size(), reporte.getSaldoTotal());

            return reporte;

        } catch (FechaInvalidaException | CuentasNoEncontradasException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error al generar reporte de estado de cuenta para cliente: {}", clienteId, e);
            throw new RuntimeException("Error al generar el reporte: " + e.getMessage());
        }
    }

    private CuentaResumenDTO convertirCuentaADTO(Cuenta cuenta) {
        try {
            if (!"ACTIVA".equals(cuenta.getEstado())) {
                throw CuentasNoEncontradasException.cuentaInactiva(cuenta.getCuentaId(), cuenta.getEstado());
            }
            
            BigDecimal saldoActual = movimientoService.calcularSaldoDisponible(cuenta.getCuentaId());
            
            return new CuentaResumenDTO(
                cuenta.getCuentaId(),
                cuenta.getNumeroCuenta(),
                cuenta.getTipoCuenta(),
                cuenta.getEstado(),
                saldoActual
            );
        } catch (CuentasNoEncontradasException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error al convertir cuenta a DTO: {}", cuenta.getCuentaId(), e);
            return new CuentaResumenDTO(
                cuenta.getCuentaId(),
                cuenta.getNumeroCuenta(),
                cuenta.getTipoCuenta(),
                cuenta.getEstado(),
                cuenta.getSaldoInicial()
            );
        }
    }

    private MovimientoDetalleDTO convertirMovimientoADTO(Movimiento movimiento) {
        return new MovimientoDetalleDTO(
            movimiento.getMovimientoId(),
            movimiento.getFecha(),
            movimiento.getTipoMovimiento(),
            movimiento.getValor(),
            movimiento.getTipoMovimiento().equals("DEBITO") ? 
            "Debito de $" + movimiento.getValor() : "Crédito de $" + movimiento.getValor(),
            movimiento.getSaldo()
        );
    }

    private void validarFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        if (fechaInicio == null || fechaFin == null) {
            throw FechaInvalidaException.fechasNulas();
        }

        if (fechaInicio.isAfter(fechaFin)) {
            throw FechaInvalidaException.fechaInicioPosterior();
        }

        if (fechaInicio.plusYears(1).isBefore(fechaFin)) {
            throw FechaInvalidaException.rangoExcesivo();
        }

        LocalDate hoy = LocalDate.now();
        if (fechaInicio.isAfter(hoy) || fechaFin.isAfter(hoy)) {
            throw FechaInvalidaException.fechasFuturas();
        }
    }

    public void validarFechas(String fechaInicioStr, String fechaFinStr) {
        try {
            LocalDate fechaInicio = LocalDate.parse(fechaInicioStr);
            LocalDate fechaFin = LocalDate.parse(fechaFinStr);
            validarFechas(fechaInicio, fechaFin);
        } catch (DateTimeParseException e) {
            throw FechaInvalidaException.formatoInvalido();
        }
    }
}