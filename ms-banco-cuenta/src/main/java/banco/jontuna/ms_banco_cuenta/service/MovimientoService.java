package banco.jontuna.ms_banco_cuenta.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import banco.jontuna.ms_banco_cuenta.dto.MovimientoRequestDTO;
import banco.jontuna.ms_banco_cuenta.dto.MovimientoResponseDTO;
import banco.jontuna.ms_banco_cuenta.model.Cuenta;
import banco.jontuna.ms_banco_cuenta.model.Movimiento;
import banco.jontuna.ms_banco_cuenta.repository.CuentaRepository;
import banco.jontuna.ms_banco_cuenta.repository.MovimientoRepository;
import banco.jontuna.ms_banco_cuenta.util.exception.CuentaInactivaException;
import banco.jontuna.ms_banco_cuenta.util.exception.CuentaNotFoundException;
import banco.jontuna.ms_banco_cuenta.util.exception.MovimientoInvalidoException;
import banco.jontuna.ms_banco_cuenta.util.exception.SaldoInsuficienteException;
import banco.jontuna.ms_banco_cuenta.util.mapper.MovimientoMapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class MovimientoService {

    private static final Logger logger = LoggerFactory.getLogger(MovimientoService.class);

    @Autowired
    private MovimientoRepository movimientoRepository;

    @Autowired
    private CuentaRepository cuentaRepository;

    @Autowired
    private MovimientoMapper movimientoMapper;

    public MovimientoResponseDTO registrarMovimiento(Long cuentaId, MovimientoRequestDTO movimientoRequest) {
        try {
            logger.info("Registrando movimiento para cuenta ID: {} - Valor recibido: {}", 
                    cuentaId, movimientoRequest.getValor());

            validarMovimiento(movimientoRequest);

            Cuenta cuenta = obtenerYValidarCuenta(cuentaId);

            String tipoMovimiento = determinarTipoMovimiento(movimientoRequest.getValor());
            
            BigDecimal valorAbsoluto = movimientoRequest.getValor().abs();

            BigDecimal saldoActual = calcularSaldoDisponible(cuentaId);
            
            if ("RETIRO".equals(tipoMovimiento)) {
                validarRetiro(saldoActual, valorAbsoluto);
            }

            BigDecimal nuevoSaldo = calcularNuevoSaldo(tipoMovimiento, saldoActual, valorAbsoluto);

            Movimiento movimiento = crearMovimiento(tipoMovimiento, valorAbsoluto, nuevoSaldo, cuenta);
            Movimiento movimientoGuardado = movimientoRepository.save(movimiento);

            logger.info("Movimiento {} registrado exitosamente para cuenta ID: {}. Valor: {}, Nuevo saldo: {}", 
                        tipoMovimiento, cuentaId, valorAbsoluto, nuevoSaldo);

            return crearResponseDTO(movimientoGuardado, movimientoRequest.getValor());

        } catch (CuentaNotFoundException | SaldoInsuficienteException | 
                CuentaInactivaException | MovimientoInvalidoException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error al registrar movimiento para cuenta ID: {}", cuentaId, e);
            throw new RuntimeException("Error al registrar el movimiento: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<MovimientoResponseDTO> obtenerMovimientosPorCuenta(Long cuentaId) {
        try {
            logger.info("Obteniendo movimientos para cuenta ID: {}", cuentaId);
            
            if (!cuentaRepository.existsById(cuentaId)) {
                throw new CuentaNotFoundException(cuentaId);
            }
            
            return movimientoRepository.findByCuentaCuentaIdOrderByFechaDesc(cuentaId)
                    .stream()
                    .map(movimientoMapper::toResponseDTO)
                    .collect(Collectors.toList());
        } catch (CuentaNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error al obtener movimientos para cuenta ID: {}", cuentaId, e);
            throw new RuntimeException("Error al obtener los movimientos de la cuenta");
        }
    }

    @Transactional(readOnly = true)
    public List<MovimientoResponseDTO> obtenerMovimientosPorCliente(Long clienteId) {
        try {
            logger.info("Obteniendo movimientos para cliente ID: {}", clienteId);
            return movimientoRepository.findByCuentaClienteId(clienteId)
                    .stream()
                    .map(movimientoMapper::toResponseDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error al obtener movimientos para cliente ID: {}", clienteId, e);
            throw new RuntimeException("Error al obtener los movimientos del cliente");
        }
    }

    @Transactional(readOnly = true)
    public MovimientoResponseDTO obtenerMovimientoPorId(Long movimientoId) {
        try {
            logger.info("Obteniendo movimiento con ID: {}", movimientoId);
            Movimiento movimiento = movimientoRepository.findById(movimientoId)
                    .orElseThrow(() -> new RuntimeException("Movimiento no encontrado con ID: " + movimientoId));
            return movimientoMapper.toResponseDTO(movimiento);
        } catch (Exception e) {
            logger.error("Error al obtener movimiento por ID: {}", movimientoId, e);
            throw new RuntimeException("Error al obtener el movimiento");
        }
    }

    @Transactional(readOnly = true)
    public List<MovimientoResponseDTO> obtenerTodosLosMovimientos() {
        try {
            logger.info("Obteniendo todos los movimientos");
            return movimientoRepository.findAll()
                    .stream()
                    .map(movimientoMapper::toResponseDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error al obtener todos los movimientos", e);
            throw new RuntimeException("Error al obtener los movimientos");
        }
    }

    public BigDecimal calcularSaldoDisponible(Long cuentaId) {
        try {
            Optional<Movimiento> ultimoMovimiento = movimientoRepository
                    .findTopByCuentaCuentaIdOrderByFechaDesc(cuentaId);
            
            return ultimoMovimiento
                    .map(Movimiento::getSaldo)
                    .orElse(BigDecimal.ZERO);
        } catch (Exception e) {
            logger.error("Error al calcular saldo para cuenta ID: {}", cuentaId, e);
            throw new RuntimeException("Error al calcular el saldo disponible");
        }
    }

    public void crearMovimientoInicial(Cuenta cuenta) {
        try {
            Movimiento movimientoInicial = new Movimiento(
                "DEPOSITO",
                cuenta.getSaldoInicial(),
                cuenta.getSaldoInicial(),
                cuenta
            );
            movimientoRepository.save(movimientoInicial);
            logger.info("Movimiento inicial registrado para cuenta: {}", cuenta.getNumeroCuenta());
        } catch (Exception e) {
            logger.error("Error al crear movimiento inicial para cuenta: {}", cuenta.getNumeroCuenta(), e);
            throw new RuntimeException("Error al crear movimiento inicial");
        }
    }

    private void validarMovimiento(MovimientoRequestDTO movimientoRequest) {
        if (movimientoRequest.getValor() == null) {
            throw new MovimientoInvalidoException("El valor del movimiento es obligatorio");
        }

        if (movimientoRequest.getValor().compareTo(BigDecimal.ZERO) == 0) {
            throw new MovimientoInvalidoException("El valor del movimiento no puede ser cero");
        }

        if (movimientoRequest.getValor().abs().compareTo(new BigDecimal("0.01")) < 0) {
            throw new MovimientoInvalidoException("El valor mínimo del movimiento es 0.01");
        }

        if (movimientoRequest.getValor().abs().compareTo(new BigDecimal("1000000")) > 0) {
            throw new MovimientoInvalidoException("El valor máximo del movimiento es 1,000,000.00");
        }
    }

    private String determinarTipoMovimiento(BigDecimal valor) {
        if (valor.compareTo(BigDecimal.ZERO) > 0) {
            return "DEPOSITO";
        } else {
            return "RETIRO";
        }
    }


    private Cuenta obtenerYValidarCuenta(Long cuentaId) {
        Cuenta cuenta = cuentaRepository.findById(cuentaId)
                .orElseThrow(() -> new CuentaNotFoundException(cuentaId));

        if (!"ACTIVA".equals(cuenta.getEstado())) {
            throw new CuentaInactivaException(cuenta.getNumeroCuenta(), cuenta.getEstado());
        }

        return cuenta;
    }

    private void validarRetiro(BigDecimal saldoActual, BigDecimal valorRetiro) {
        if (saldoActual.compareTo(valorRetiro) < 0) {
            throw new SaldoInsuficienteException(saldoActual, valorRetiro);
        }
    }


    private BigDecimal calcularNuevoSaldo(String tipoMovimiento, BigDecimal saldoActual, BigDecimal valorAbsoluto) {
        if ("DEPOSITO".equals(tipoMovimiento)) {
            return saldoActual.add(valorAbsoluto);
        } else {
            return saldoActual.subtract(valorAbsoluto);
        }
    }

    private Movimiento crearMovimiento(String tipoMovimiento, BigDecimal valorAbsoluto, 
                                    BigDecimal nuevoSaldo, Cuenta cuenta) {
        Movimiento movimiento = new Movimiento();
        movimiento.setTipoMovimiento(tipoMovimiento);
        movimiento.setValor(valorAbsoluto);
        movimiento.setSaldo(nuevoSaldo);
        movimiento.setCuenta(cuenta);
        return movimiento;
    }

    private MovimientoResponseDTO crearResponseDTO(Movimiento movimiento, BigDecimal valorOriginal) {
        MovimientoResponseDTO dto = new MovimientoResponseDTO();
        dto.setMovimientoId(movimiento.getMovimientoId());
        dto.setFecha(movimiento.getFecha());
        dto.setTipoMovimiento(movimiento.getTipoMovimiento());
        dto.setValor(valorOriginal);
        dto.setSaldo(movimiento.getSaldo());
        dto.setCuentaId(movimiento.getCuenta().getCuentaId());
        dto.setNumeroCuenta(movimiento.getCuenta().getNumeroCuenta());
        return dto;
    }
}