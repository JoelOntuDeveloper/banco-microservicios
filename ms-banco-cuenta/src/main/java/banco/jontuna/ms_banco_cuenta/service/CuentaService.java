package banco.jontuna.ms_banco_cuenta.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import banco.jontuna.ms_banco_cuenta.dto.CuentaRequestDTO;
import banco.jontuna.ms_banco_cuenta.dto.CuentaResponseDTO;
import banco.jontuna.ms_banco_cuenta.model.Cuenta;
import banco.jontuna.ms_banco_cuenta.repository.CuentaRepository;
import banco.jontuna.ms_banco_cuenta.util.exception.CuentaConSaldoException;
import banco.jontuna.ms_banco_cuenta.util.exception.CuentaNoActivaException;
import banco.jontuna.ms_banco_cuenta.util.exception.CuentaNotFoundException;
import banco.jontuna.ms_banco_cuenta.util.mapper.CuentaMapper;
import jakarta.validation.ValidationException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@Transactional
public class CuentaService {

    private static final Logger logger = LoggerFactory.getLogger(CuentaService.class);

    @Autowired
    private CuentaRepository cuentaRepository;

    @Autowired
    private CuentaMapper cuentaMapper;

    @Autowired
    private MovimientoService movimientoService;

    private Random random = new Random();

    public CuentaResponseDTO crearCuenta(CuentaRequestDTO cuentaRequest) {
        try {
            logger.info("Creando cuenta para cliente ID: {}", cuentaRequest.getClienteId());

            validarDatosCuenta(cuentaRequest);

            String numeroCuenta = generarNumeroCuentaUnico();

            Cuenta cuenta = cuentaMapper.toEntity(cuentaRequest);
            cuenta.setNumeroCuenta(numeroCuenta);

            Cuenta cuentaGuardada = cuentaRepository.save(cuenta);
            logger.info("Cuenta creada exitosamente: {} para cliente ID: {}", 
                        numeroCuenta, cuentaRequest.getClienteId());
            
            if (cuentaGuardada.getSaldoInicial().compareTo(BigDecimal.ZERO) > 0) {
                movimientoService.crearMovimientoInicial(cuentaGuardada);
            }

            return cuentaMapper.toResponseDTO(cuentaGuardada);

        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error al crear cuenta para cliente ID: {}", cuentaRequest.getClienteId(), e);
            throw new RuntimeException("Error al crear la cuenta: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<CuentaResponseDTO> obtenerCuentasPorCliente(Long clienteId) {
        try {
            logger.info("Obteniendo cuentas para cliente ID: {}", clienteId);
            return cuentaRepository.findByClienteId(clienteId)
                    .stream()
                    .map(cuentaMapper::toResponseDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error al obtener cuentas para cliente ID: {}", clienteId, e);
            throw new RuntimeException("Error al obtener las cuentas del cliente");
        }
    }

    @Transactional(readOnly = true)
    public CuentaResponseDTO obtenerCuentaPorId(Long cuentaId) {
        try {
            logger.info("Buscando cuenta con ID: {}", cuentaId);
            Cuenta cuenta = cuentaRepository.findById(cuentaId)
                    .orElseThrow(() -> new CuentaNotFoundException(cuentaId));
            return cuentaMapper.toResponseDTO(cuenta);
        } catch (CuentaNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error al obtener cuenta por ID: {}", cuentaId, e);
            throw new RuntimeException("Error al obtener la cuenta");
        }
    }

    @Transactional(readOnly = true)
    public CuentaResponseDTO obtenerCuentaPorNumero(String numeroCuenta) {
        try {
            logger.info("Buscando cuenta con número: {}", numeroCuenta);
            Cuenta cuenta = cuentaRepository.findByNumeroCuenta(numeroCuenta)
                    .orElseThrow(() -> new CuentaNotFoundException(numeroCuenta, true));
            return cuentaMapper.toResponseDTO(cuenta);
        } catch (CuentaNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error al obtener cuenta por número: {}", numeroCuenta, e);
            throw new RuntimeException("Error al obtener la cuenta");
        }
    }

    @Transactional(readOnly = true)
    public List<CuentaResponseDTO> obtenerTodasLasCuentas() {
        try {
            logger.info("Obteniendo todas las cuentas");
            return cuentaRepository.findAll()
                    .stream()
                    .map(cuentaMapper::toResponseDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error al obtener todas las cuentas", e);
            throw new RuntimeException("Error al obtener las cuentas");
        }
    }

    @Transactional(readOnly = true)
    public BigDecimal consultarSaldo(Long cuentaId) {
        try {
            logger.info("Consultando saldo para cuenta ID: {}", cuentaId);
            
            if (!cuentaRepository.existsById(cuentaId)) {
                throw new CuentaNotFoundException(cuentaId);
            }
            
            BigDecimal saldo = movimientoService.calcularSaldoDisponible(cuentaId);
            logger.info("Saldo consultado para cuenta ID: {} - Saldo: {}", cuentaId, saldo);
            
            return saldo;
        } catch (CuentaNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error al consultar saldo para cuenta ID: {}", cuentaId, e);
            throw new RuntimeException("Error al consultar el saldo");
        }
    }

    public CuentaResponseDTO actualizarEstadoCuenta(Long cuentaId, String nuevoEstado) {
        try {
            logger.info("Actualizando estado de cuenta ID: {} a {}", cuentaId, nuevoEstado);
            
            Cuenta cuenta = cuentaRepository.findById(cuentaId)
                    .orElseThrow(() -> new CuentaNotFoundException(cuentaId));
            
            if (!List.of("ACTIVA", "BLOQUEADA", "CANCELADA").contains(nuevoEstado)) {
                throw new ValidationException("Estado de cuenta inválido: " + nuevoEstado);
            }
            
            cuenta.setEstado(nuevoEstado);
            Cuenta cuentaActualizada = cuentaRepository.save(cuenta);
            
            logger.info("Estado de cuenta ID: {} actualizado exitosamente a {}", cuentaId, nuevoEstado);
            return cuentaMapper.toResponseDTO(cuentaActualizada);
            
        } catch (CuentaNotFoundException | ValidationException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error al actualizar estado de cuenta ID: {}", cuentaId, e);
            throw new RuntimeException("Error al actualizar el estado de la cuenta");
        }
    }

    public CuentaResponseDTO crearCuentaAutomatica(Long clienteId, String nombreCliente) {
        try {
            logger.info("Creando cuenta automática para cliente ID: {}", clienteId);
            
            List<CuentaResponseDTO> cuentasExistentes = obtenerCuentasPorCliente(clienteId);
            if (!cuentasExistentes.isEmpty()) {
                logger.warn("El cliente ID: {} ya tiene {} cuentas existentes. No se creará cuenta automática.", 
                            clienteId, cuentasExistentes.size());
                return null;
            }
            
            CuentaRequestDTO cuentaRequest = new CuentaRequestDTO();
            cuentaRequest.setTipoCuenta("AHORROS");
            cuentaRequest.setSaldoInicial(BigDecimal.ZERO);
            cuentaRequest.setClienteId(clienteId);

            CuentaResponseDTO cuentaCreada = crearCuenta(cuentaRequest);
            
            if (cuentaCreada != null) {
                logger.info("Cuenta automática creada exitosamente: {} para cliente ID: {}", 
                            cuentaCreada.getNumeroCuenta(), clienteId);
            }
            
            return cuentaCreada;
            
        } catch (Exception e) {
            logger.error("Error al crear cuenta automática para cliente ID: {}", clienteId, e);
            return null;
        }
    }

    public CuentaResponseDTO eliminarCuentaLogicamente(Long cuentaId) {
        try {
            logger.info("Iniciando eliminación lógica de cuenta ID: {}", cuentaId);

            Cuenta cuenta = cuentaRepository.findById(cuentaId)
                    .orElseThrow(() -> new CuentaNotFoundException(cuentaId));

            if (!"ACTIVA".equals(cuenta.getEstado())) {
                throw new CuentaNoActivaException(
                    cuenta.getNumeroCuenta(), cuenta.getEstado());
            }

            BigDecimal saldoActual = movimientoService.calcularSaldoDisponible(cuentaId);
            if (saldoActual.compareTo(BigDecimal.ZERO) > 0) {
                throw new CuentaConSaldoException(cuenta.getNumeroCuenta(), saldoActual);
            }

            cuenta.setEstado("ELIMINADA");

            Cuenta cuentaEliminada = cuentaRepository.save(cuenta);

            logger.info("Cuenta ID: {} eliminada lógicamente exitosamente.", cuentaId);

            return cuentaMapper.toResponseDTO(cuentaEliminada);

        } catch (CuentaNotFoundException | CuentaConSaldoException| ValidationException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error al eliminar lógicamente cuenta ID: {}", cuentaId, e);
            throw new RuntimeException("Error al eliminar la cuenta: " + e.getMessage());
        }
    }

    public CuentaResponseDTO reactivarCuenta(Long cuentaId) {
        try {
            logger.info("Reactivando cuenta ID: {}", cuentaId);

            Cuenta cuenta = cuentaRepository.findById(cuentaId)
                    .orElseThrow(() -> new CuentaNotFoundException(cuentaId));

            if (!"ELIMINADA".equals(cuenta.getEstado())) {
                throw new ValidationException(
                    String.format("No se puede reactivar la cuenta %s porque no está eliminada. Estado actual: %s", 
                                cuenta.getNumeroCuenta(), cuenta.getEstado())
                );
            }

            cuenta.setEstado("ACTIVA");

            Cuenta cuentaReactivada = cuentaRepository.save(cuenta);
            
            logger.info("Cuenta ID: {} reactivada exitosamente", cuentaId);

            return cuentaMapper.toResponseDTO(cuentaReactivada);

        } catch (CuentaNotFoundException | ValidationException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error al reactivar cuenta ID: {}", cuentaId, e);
            throw new RuntimeException("Error al reactivar la cuenta: " + e.getMessage());
        }
    }

    private void validarDatosCuenta(CuentaRequestDTO cuentaRequest) {
        if (cuentaRequest.getTipoCuenta() == null || cuentaRequest.getTipoCuenta().trim().isEmpty()) {
            throw new ValidationException("El tipo de cuenta es obligatorio");
        }

        if (cuentaRequest.getSaldoInicial() == null) {
            throw new ValidationException("El saldo inicial es obligatorio");
        }

        if (cuentaRequest.getSaldoInicial().compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("El saldo inicial no puede ser negativo");
        }

        if (cuentaRequest.getClienteId() == null) {
            throw new ValidationException("El ID del cliente es obligatorio");
        }
    }

    private String generarNumeroCuentaUnico() {
        String numeroCuenta;
        int intentos = 0;
        do {
            numeroCuenta = generarNumeroCuenta();
            intentos++;
            if (intentos > 10) {
                throw new RuntimeException("No se pudo generar un número de cuenta único después de 10 intentos");
            }
        } while (cuentaRepository.existsByNumeroCuenta(numeroCuenta));
        return numeroCuenta;
    }

    private String generarNumeroCuenta() {
        return String.format("%010d", random.nextInt(1000000000));
    }
}