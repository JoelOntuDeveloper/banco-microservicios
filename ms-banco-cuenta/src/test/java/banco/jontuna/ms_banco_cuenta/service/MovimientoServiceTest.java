package banco.jontuna.ms_banco_cuenta.service;

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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovimientoServiceTest {

    @Mock
    private MovimientoRepository movimientoRepository;

    @Mock
    private CuentaRepository cuentaRepository;

    @Mock
    private MovimientoMapper movimientoMapper;

    @InjectMocks
    private MovimientoService movimientoService;

    private Cuenta cuentaActiva;
    private Cuenta cuentaInactiva;
    private Movimiento movimientoDeposito;
    private Movimiento movimientoRetiro;
    private MovimientoRequestDTO movimientoRequestDeposito;
    private MovimientoRequestDTO movimientoRequestRetiro;

    @BeforeEach
    void setUp() {
        cuentaActiva = new Cuenta();
        cuentaActiva.setCuentaId(1L);
        cuentaActiva.setNumeroCuenta("1234567890");
        cuentaActiva.setTipoCuenta("AHORROS");
        cuentaActiva.setSaldoInicial(BigDecimal.ZERO);
        cuentaActiva.setEstado("ACTIVA");
        cuentaActiva.setClienteId(1L);

        cuentaInactiva = new Cuenta();
        cuentaInactiva.setCuentaId(2L);
        cuentaInactiva.setNumeroCuenta("0987654321");
        cuentaInactiva.setTipoCuenta("CORRIENTE");
        cuentaInactiva.setSaldoInicial(BigDecimal.ZERO);
        cuentaInactiva.setEstado("BLOQUEADA");
        cuentaInactiva.setClienteId(1L);

        movimientoDeposito = new Movimiento();
        movimientoDeposito.setMovimientoId(1L);
        movimientoDeposito.setTipoMovimiento("DEPOSITO");
        movimientoDeposito.setValor(new BigDecimal("500.00"));
        movimientoDeposito.setSaldo(new BigDecimal("500.00"));
        movimientoDeposito.setCuenta(cuentaActiva);
        movimientoDeposito.setFecha(LocalDateTime.now());

        movimientoRetiro = new Movimiento();
        movimientoRetiro.setMovimientoId(2L);
        movimientoRetiro.setTipoMovimiento("RETIRO");
        movimientoRetiro.setValor(new BigDecimal("200.00"));
        movimientoRetiro.setSaldo(new BigDecimal("300.00"));
        movimientoRetiro.setCuenta(cuentaActiva);
        movimientoRetiro.setFecha(LocalDateTime.now());

        movimientoRequestDeposito = new MovimientoRequestDTO(new BigDecimal("500.00"));
        movimientoRequestRetiro = new MovimientoRequestDTO(new BigDecimal("-200.00"));
    }

    @Test
    void registrarMovimiento_WithPositiveValueAndActiveAccount_ShouldRegisterDeposit() {
        // Arrange
        when(cuentaRepository.findById(1L)).thenReturn(Optional.of(cuentaActiva));
        when(movimientoRepository.findTopByCuentaCuentaIdOrderByFechaDesc(1L))
                .thenReturn(Optional.empty()); // Sin movimientos previos
        when(movimientoRepository.save(any(Movimiento.class))).thenReturn(movimientoDeposito);
                
        // Act
        MovimientoResponseDTO result = movimientoService.registrarMovimiento(1L, movimientoRequestDeposito);

        // Assert
        assertNotNull(result);
        assertEquals("DEPOSITO", result.getTipoMovimiento());
        assertEquals(new BigDecimal("500.00"), result.getValor());
        assertEquals(new BigDecimal("500.00"), result.getSaldo());
        
        verify(cuentaRepository, times(1)).findById(1L);
        verify(movimientoRepository, times(1)).save(any(Movimiento.class));
    }

    @Test
    void registrarMovimiento_WithNegativeValueAndSufficientBalance_ShouldRegisterWithdrawal() {
        // Arrange
        when(cuentaRepository.findById(1L)).thenReturn(Optional.of(cuentaActiva));
        
        // Configurar saldo actual de 1000.00
        Movimiento ultimoMovimiento = new Movimiento();
        ultimoMovimiento.setSaldo(new BigDecimal("1000.00"));
        when(movimientoRepository.findTopByCuentaCuentaIdOrderByFechaDesc(1L))
                .thenReturn(Optional.of(ultimoMovimiento));
        
        when(movimientoRepository.save(any(Movimiento.class))).thenReturn(movimientoRetiro);

        // Act
        MovimientoResponseDTO result = movimientoService.registrarMovimiento(1L, movimientoRequestRetiro);

        // Assert
        assertNotNull(result);
        assertEquals("RETIRO", result.getTipoMovimiento());
        assertEquals(new BigDecimal("-200.00"), result.getValor());
        
        verify(cuentaRepository, times(1)).findById(1L);
        verify(movimientoRepository, times(1)).save(any(Movimiento.class));
    }

    @Test
    void registrarMovimiento_WithNonExistentAccount_ShouldThrowCuentaNotFoundException() {
        // Arrange
        when(cuentaRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        CuentaNotFoundException exception = assertThrows(
            CuentaNotFoundException.class,
            () -> movimientoService.registrarMovimiento(999L, movimientoRequestDeposito)
        );

        assertEquals("Cuenta no encontrada con ID: 999", exception.getMessage());
        verify(cuentaRepository, times(1)).findById(999L);
        verify(movimientoRepository, never()).save(any(Movimiento.class));
    }

    @Test
    void registrarMovimiento_WithInactiveAccount_ShouldThrowCuentaInactivaException() {
        // Arrange
        when(cuentaRepository.findById(2L)).thenReturn(Optional.of(cuentaInactiva));

        // Act & Assert
        CuentaInactivaException exception = assertThrows(
            CuentaInactivaException.class,
            () -> movimientoService.registrarMovimiento(2L, movimientoRequestDeposito)
        );

        assertTrue(exception.getMessage().contains("no está activa"));
        verify(cuentaRepository, times(1)).findById(2L);
        verify(movimientoRepository, never()).save(any(Movimiento.class));
    }

    @Test
    void registrarMovimiento_WithZeroValue_ShouldThrowMovimientoInvalidoException() {
        // Arrange
        MovimientoRequestDTO requestCero = new MovimientoRequestDTO(BigDecimal.ZERO);

        // Act & Assert
        MovimientoInvalidoException exception = assertThrows(
            MovimientoInvalidoException.class,
            () -> movimientoService.registrarMovimiento(1L, requestCero)
        );

        assertTrue(exception.getMessage().contains("no puede ser cero"));
        verify(cuentaRepository, never()).findById(anyLong());
        verify(movimientoRepository, never()).save(any(Movimiento.class));
    }

    @Test
    void registrarMovimiento_WithNegativeValueAndInsufficientBalance_ShouldThrowSaldoInsuficienteException() {
        // Arrange
        when(cuentaRepository.findById(1L)).thenReturn(Optional.of(cuentaActiva));
        
        // Configurar saldo actual de 100.00 (insuficiente para retiro de 200.00)
        Movimiento ultimoMovimiento = new Movimiento();
        ultimoMovimiento.setSaldo(new BigDecimal("100.00"));
        when(movimientoRepository.findTopByCuentaCuentaIdOrderByFechaDesc(1L))
                .thenReturn(Optional.of(ultimoMovimiento));

        // Act & Assert
        SaldoInsuficienteException exception = assertThrows(
            SaldoInsuficienteException.class,
            () -> movimientoService.registrarMovimiento(1L, movimientoRequestRetiro)
        );

        assertTrue(exception.getMessage().contains("Saldo no disponible"));
        assertTrue(exception.getMessage().contains("100,00"));
        assertTrue(exception.getMessage().contains("200,00"));
        
        verify(cuentaRepository, times(1)).findById(1L);
        verify(movimientoRepository, never()).save(any(Movimiento.class));
    }

    @Test
    void registrarMovimiento_WithValueBelowMinimum_ShouldThrowMovimientoInvalidoException() {
        // Arrange
        MovimientoRequestDTO requestMinimo = new MovimientoRequestDTO(new BigDecimal("0.005"));

        // Act & Assert
        MovimientoInvalidoException exception = assertThrows(
            MovimientoInvalidoException.class,
            () -> movimientoService.registrarMovimiento(1L, requestMinimo)
        );

        assertTrue(exception.getMessage().contains("El valor mínimo del movimiento es 0.01"));
        verify(cuentaRepository, never()).findById(anyLong());
        verify(movimientoRepository, never()).save(any(Movimiento.class));
    }

    @Test
    void registrarMovimiento_WithValueAboveMaximum_ShouldThrowMovimientoInvalidoException() {
        // Arrange
        MovimientoRequestDTO requestMaximo = new MovimientoRequestDTO(new BigDecimal("2000000.00"));

        // Act & Assert
        MovimientoInvalidoException exception = assertThrows(
            MovimientoInvalidoException.class,
            () -> movimientoService.registrarMovimiento(1L, requestMaximo)
        );

        assertTrue(exception.getMessage().contains("El valor máximo del movimiento es 1,000,000.00"));
        verify(cuentaRepository, never()).findById(anyLong());
        verify(movimientoRepository, never()).save(any(Movimiento.class));
    }

    @Test
    void obtenerMovimientosPorCuenta_WithExistingAccount_ShouldReturnMovementsList() {
        // Arrange
        List<Movimiento> movimientos = Arrays.asList(movimientoDeposito, movimientoRetiro);
        when(movimientoRepository.findByCuentaCuentaIdOrderByFechaDesc(1L)).thenReturn(movimientos);
        when(cuentaRepository.existsById(1L)).thenReturn(true);
        
        MovimientoResponseDTO response1 = new MovimientoResponseDTO(
            1L, movimientoDeposito.getFecha(), "DEPOSITO", 
            new BigDecimal("500.00"), new BigDecimal("500.00"), 1L, "1234567890"
        );
        MovimientoResponseDTO response2 = new MovimientoResponseDTO(
            2L, movimientoRetiro.getFecha(), "RETIRO", 
            new BigDecimal("-200.00"), new BigDecimal("300.00"), 1L, "1234567890"
        );
        
        when(movimientoMapper.toResponseDTO(movimientoDeposito)).thenReturn(response1);
        when(movimientoMapper.toResponseDTO(movimientoRetiro)).thenReturn(response2);

        // Act
        List<MovimientoResponseDTO> result = movimientoService.obtenerMovimientosPorCuenta(1L);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("DEPOSITO", result.get(0).getTipoMovimiento());
        assertEquals("RETIRO", result.get(1).getTipoMovimiento());
        
        verify(movimientoRepository, times(1)).findByCuentaCuentaIdOrderByFechaDesc(1L);
        verify(cuentaRepository, times(1)).existsById(1L);
    }

    @Test
    void obtenerMovimientosPorCuenta_WithNonExistentAccount_ShouldThrowCuentaNotFoundException() {
        // Arrange
        when(cuentaRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        CuentaNotFoundException exception = assertThrows(
            CuentaNotFoundException.class,
            () -> movimientoService.obtenerMovimientosPorCuenta(999L)
        );

        assertEquals("Cuenta no encontrada con ID: 999", exception.getMessage());
        verify(cuentaRepository, times(1)).existsById(999L);
        verify(movimientoRepository, never()).findByCuentaCuentaIdOrderByFechaDesc(anyLong());
    }

    @Test
    void obtenerMovimientosPorCliente_WithExistingClient_ShouldReturnMovementsList() {
        // Arrange
        List<Movimiento> movimientos = Arrays.asList(movimientoDeposito, movimientoRetiro);
        when(movimientoRepository.findByCuentaClienteId(1L)).thenReturn(movimientos);
        
        MovimientoResponseDTO response1 = new MovimientoResponseDTO(
            1L, movimientoDeposito.getFecha(), "DEPOSITO", 
            new BigDecimal("500.00"), new BigDecimal("500.00"), 1L, "1234567890"
        );
        MovimientoResponseDTO response2 = new MovimientoResponseDTO(
            2L, movimientoRetiro.getFecha(), "RETIRO", 
            new BigDecimal("-200.00"), new BigDecimal("300.00"), 1L, "1234567890"
        );
        
        when(movimientoMapper.toResponseDTO(movimientoDeposito)).thenReturn(response1);
        when(movimientoMapper.toResponseDTO(movimientoRetiro)).thenReturn(response2);

        // Act
        List<MovimientoResponseDTO> result = movimientoService.obtenerMovimientosPorCliente(1L);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(movimientoRepository, times(1)).findByCuentaClienteId(1L);
    }

    @Test
    void calcularSaldoDisponible_WithExistingAccountAndMovements_ShouldReturnCurrentBalance() {
        // Arrange
        Movimiento ultimoMovimiento = new Movimiento();
        ultimoMovimiento.setSaldo(new BigDecimal("1500.00"));
        when(movimientoRepository.findTopByCuentaCuentaIdOrderByFechaDesc(1L))
                .thenReturn(Optional.of(ultimoMovimiento));

        // Act
        BigDecimal result = movimientoService.calcularSaldoDisponible(1L);

        // Assert
        assertNotNull(result);
        assertEquals(new BigDecimal("1500.00"), result);
        verify(movimientoRepository, times(1)).findTopByCuentaCuentaIdOrderByFechaDesc(1L);
    }

    @Test
    void calcularSaldoDisponible_WithExistingAccountAndNoMovements_ShouldReturnZero() {
        // Arrange
        when(movimientoRepository.findTopByCuentaCuentaIdOrderByFechaDesc(1L))
                .thenReturn(Optional.empty());

        // Act
        BigDecimal result = movimientoService.calcularSaldoDisponible(1L);

        // Assert
        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result);
        verify(movimientoRepository, times(1)).findTopByCuentaCuentaIdOrderByFechaDesc(1L);
    }

    @Test
    void calcularSaldoDisponible_WithException_ShouldThrowRuntimeException() {
        // Arrange
        when(movimientoRepository.findTopByCuentaCuentaIdOrderByFechaDesc(1L))
                .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> movimientoService.calcularSaldoDisponible(1L)
        );

        assertTrue(exception.getMessage().contains("Error al calcular el saldo disponible"));
        verify(movimientoRepository, times(1)).findTopByCuentaCuentaIdOrderByFechaDesc(1L);
    }

    @Test
    void crearMovimientoInicial_WithValidAccount_ShouldCreateInitialMovement() {
        // Arrange
        cuentaActiva.setSaldoInicial(new BigDecimal("1000.00"));
        when(movimientoRepository.save(any(Movimiento.class))).thenReturn(movimientoDeposito);

        // Act
        movimientoService.crearMovimientoInicial(cuentaActiva);

        // Assert
        verify(movimientoRepository, times(1)).save(any(Movimiento.class));
    }

    @Test
    void crearMovimientoInicial_WithException_ShouldThrowRuntimeException() {
        // Arrange
        cuentaActiva.setSaldoInicial(new BigDecimal("1000.00"));
        when(movimientoRepository.save(any(Movimiento.class)))
                .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> movimientoService.crearMovimientoInicial(cuentaActiva)
        );

        assertTrue(exception.getMessage().contains("Error al crear movimiento inicial"));
        verify(movimientoRepository, times(1)).save(any(Movimiento.class));
    }

    @Test
    void obtenerMovimientoPorId_WithExistingMovement_ShouldReturnMovement() {
        // Arrange
        when(movimientoRepository.findById(1L)).thenReturn(Optional.of(movimientoDeposito));
        
        MovimientoResponseDTO expectedResponse = new MovimientoResponseDTO(
            1L, movimientoDeposito.getFecha(), "DEPOSITO", 
            new BigDecimal("500.00"), new BigDecimal("500.00"), 1L, "1234567890"
        );
        when(movimientoMapper.toResponseDTO(movimientoDeposito)).thenReturn(expectedResponse);

        // Act
        MovimientoResponseDTO result = movimientoService.obtenerMovimientoPorId(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getMovimientoId());
        assertEquals("DEPOSITO", result.getTipoMovimiento());
        verify(movimientoRepository, times(1)).findById(1L);
    }

    @Test
    void obtenerMovimientoPorId_WithNonExistentMovement_ShouldThrowRuntimeException() {
        // Arrange
        when(movimientoRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> movimientoService.obtenerMovimientoPorId(999L)
        );

        assertTrue(exception.getMessage().contains("Error al obtener el movimiento"));
        verify(movimientoRepository, times(1)).findById(999L);
    }

    @Test
    void obtenerTodosLosMovimientos_WithMovements_ShouldReturnAllMovements() {
        // Arrange
        List<Movimiento> movimientos = Arrays.asList(movimientoDeposito, movimientoRetiro);
        when(movimientoRepository.findAll()).thenReturn(movimientos);
        
        MovimientoResponseDTO response1 = new MovimientoResponseDTO(
            1L, movimientoDeposito.getFecha(), "DEPOSITO", 
            new BigDecimal("500.00"), new BigDecimal("500.00"), 1L, "1234567890"
        );
        MovimientoResponseDTO response2 = new MovimientoResponseDTO(
            2L, movimientoRetiro.getFecha(), "RETIRO", 
            new BigDecimal("-200.00"), new BigDecimal("300.00"), 1L, "1234567890"
        );
        
        when(movimientoMapper.toResponseDTO(movimientoDeposito)).thenReturn(response1);
        when(movimientoMapper.toResponseDTO(movimientoRetiro)).thenReturn(response2);

        // Act
        List<MovimientoResponseDTO> result = movimientoService.obtenerTodosLosMovimientos();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(movimientoRepository, times(1)).findAll();
    }
}