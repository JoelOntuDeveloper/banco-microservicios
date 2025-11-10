package banco.jontuna.ms_banco_cuenta.service;

import banco.jontuna.ms_banco_cuenta.dto.EstadoCuentaReporteDTO;
import banco.jontuna.ms_banco_cuenta.model.Cuenta;
import banco.jontuna.ms_banco_cuenta.model.Movimiento;
import banco.jontuna.ms_banco_cuenta.repository.CuentaRepository;
import banco.jontuna.ms_banco_cuenta.repository.MovimientoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:cleanup.sql")
class ReporteControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CuentaRepository cuentaRepository;

    @Autowired
    private MovimientoRepository movimientoRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Long clienteIdExistente;
    private Long clienteIdSinCuentas;
    private Cuenta cuentaAhorros;
    private Cuenta cuentaCorriente;
    private Movimiento movimientoDeposito;
    private Movimiento movimientoRetiro;

    @BeforeEach
    void setUp() {
        
        clienteIdExistente = 1001L;
        clienteIdSinCuentas = 9999L;

        // Crear datos de prueba
        cuentaAhorros = new Cuenta();
        cuentaAhorros.setClienteId(clienteIdExistente);
        cuentaAhorros.setNumeroCuenta("1000000001");
        cuentaAhorros.setTipoCuenta("AHORROS");
        cuentaAhorros.setEstado("ACTIVA");
        cuentaAhorros.setSaldoInicial(BigDecimal.valueOf(1000.00));
        cuentaAhorros = cuentaRepository.save(cuentaAhorros);

        cuentaCorriente = new Cuenta();
        cuentaCorriente.setClienteId(clienteIdExistente);
        cuentaCorriente.setNumeroCuenta("1000000002");
        cuentaCorriente.setTipoCuenta("CORRIENTE");
        cuentaCorriente.setEstado("ACTIVA");
        cuentaCorriente.setSaldoInicial(BigDecimal.valueOf(2000.00));
        cuentaCorriente = cuentaRepository.save(cuentaCorriente);

        movimientoDeposito = new Movimiento();
        movimientoDeposito.setCuenta(cuentaAhorros);
        movimientoDeposito.setFecha(LocalDateTime.of(2024, 1, 15, 10, 30, 0));
        movimientoDeposito.setTipoMovimiento("CREDITO");
        movimientoDeposito.setValor(BigDecimal.valueOf(500.00));
        movimientoDeposito.setSaldo(BigDecimal.valueOf(1500.00));
        movimientoDeposito = movimientoRepository.save(movimientoDeposito);

        movimientoRetiro = new Movimiento();
        movimientoRetiro.setCuenta(cuentaAhorros);
        movimientoRetiro.setFecha(LocalDateTime.of(2024, 1, 20, 14, 15, 0));
        movimientoRetiro.setTipoMovimiento("DEBITO");
        movimientoRetiro.setValor(BigDecimal.valueOf(200.00));
        movimientoRetiro.setSaldo(BigDecimal.valueOf(1300.00));
        movimientoRetiro = movimientoRepository.save(movimientoRetiro);

        Movimiento movimientoCorriente = new Movimiento();
        movimientoCorriente.setCuenta(cuentaCorriente);
        movimientoCorriente.setFecha(LocalDateTime.of(2024, 1, 25, 9, 0, 0));
        movimientoCorriente.setTipoMovimiento("CREDITO");
        movimientoCorriente.setValor(BigDecimal.valueOf(1000.00));
        movimientoCorriente.setSaldo(BigDecimal.valueOf(3000.00));
        movimientoRepository.save(movimientoCorriente);

        Cuenta cuentaInactiva = new Cuenta();
        cuentaInactiva.setClienteId(clienteIdExistente);
        cuentaInactiva.setNumeroCuenta("1000000003");
        cuentaInactiva.setTipoCuenta("AHORROS");
        cuentaInactiva.setEstado("INACTIVA");
        cuentaInactiva.setSaldoInicial(BigDecimal.valueOf(500.00));
        cuentaRepository.save(cuentaInactiva);
    }

    @Test
    void generarReporteEstadoCuenta_ClienteExistenteConMovimientos_DeberiaRetornarReporteCompleto() throws Exception {
        // Arrange
        String fechaInicio = "2024-01-01";
        String fechaFin = "2024-01-31";
        String url = "/api/reportes/{clienteId}";

        // Act & Assert
        mockMvc.perform(get(url, clienteIdExistente)
                .param("fechaInicio", fechaInicio)
                .param("fechaFin", fechaFin)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.clienteId").value(clienteIdExistente))
                .andExpect(jsonPath("$.fechaInicio").value(fechaInicio))
                .andExpect(jsonPath("$.fechaFin").value(fechaFin))
                .andExpect(jsonPath("$.saldoTotal").isNumber())
                .andExpect(jsonPath("$.cuentas").isArray())
                .andExpect(jsonPath("$.cuentas.length()").value(2)) // Solo cuentas ACTIVAS
                .andExpect(jsonPath("$.cuentas[0].numeroCuenta").exists())
                .andExpect(jsonPath("$.cuentas[0].tipoCuenta").exists())
                .andExpect(jsonPath("$.cuentas[0].estado").value("ACTIVA"))
                .andExpect(jsonPath("$.cuentas[0].saldoActual").isNumber())
                .andExpect(jsonPath("$.cuentas[0].movimientos").isArray())
                .andExpect(jsonPath("$.cuentas[0].movimientos[0].movimientoId").exists())
                .andExpect(jsonPath("$.cuentas[0].movimientos[0].fecha").exists())
                .andExpect(jsonPath("$.cuentas[0].movimientos[0].tipoMovimiento").isString())
                .andExpect(jsonPath("$.cuentas[0].movimientos[0].valor").isNumber())
                .andExpect(jsonPath("$.cuentas[0].movimientos[0].descripcion").isString())
                .andExpect(jsonPath("$.cuentas[0].movimientos[0].saldoDespues").isNumber());
    }

    @Test
    void generarReporteEstadoCuenta_ClienteExistenteConRangoFechasEspecifico_DeberiaRetornarSoloMovimientosEnRango() throws Exception {
        // Arrange
        String fechaInicio = "2024-01-16";
        String fechaFin = "2024-01-25";
        String url = "/api/reportes/{clienteId}";

        // Act
        String response = mockMvc.perform(get(url, clienteIdExistente)
                .param("fechaInicio", fechaInicio)
                .param("fechaFin", fechaFin)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Assert
        EstadoCuentaReporteDTO reporte = objectMapper.readValue(response, EstadoCuentaReporteDTO.class);

        var cuentaAhorrosReporte = reporte.getCuentas().stream()
                .filter(c -> c.getNumeroCuenta().equals("1000000001"))
                .findFirst()
                .orElseThrow();

        assertEquals(1, cuentaAhorrosReporte.getMovimientos().size());
        assertEquals("DEBITO", cuentaAhorrosReporte.getMovimientos().get(0).getTipoMovimiento());
        assertTrue(cuentaAhorrosReporte.getMovimientos().get(0).getDescripcion().contains("Debito"));
    }

    @Test
    void generarReporteEstadoCuenta_ClienteInexistente_DeberiaRetornarNotFound() throws Exception {
        // Arrange
        String fechaInicio = "2024-01-01";
        String fechaFin = "2024-01-31";
        String url = "/api/reportes/{clienteId}";

        // Act & Assert
        mockMvc.perform(get(url, clienteIdSinCuentas)
                .param("fechaInicio", fechaInicio)
                .param("fechaFin", fechaFin)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Cuentas No Encontradas"))
                .andExpect(jsonPath("$.message").value("No se encontraron cuentas activas para el cliente con ID: " + clienteIdSinCuentas));
    }

    @Test
    void generarReporteEstadoCuenta_FechaInicioPosteriorAFechaFin_DeberiaRetornarBadRequest() throws Exception {
        // Arrange
        String fechaInicio = "2024-01-31";
        String fechaFin = "2024-01-01";
        String url = "/api/reportes/{clienteId}";

        // Act & Assert
        mockMvc.perform(get(url, clienteIdExistente)
                .param("fechaInicio", fechaInicio)
                .param("fechaFin", fechaFin)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Fecha Inválida"))
                .andExpect(jsonPath("$.message").value("La fecha de inicio no puede ser posterior a la fecha fin"));
    }

    @Test
    void generarReporteEstadoCuenta_RangoFechasMayorAUnAnio_DeberiaRetornarBadRequest() throws Exception {
        // Arrange
        String fechaInicio = "2023-01-01";
        String fechaFin = "2024-01-02";
        String url = "/api/reportes/{clienteId}";

        // Act & Assert
        mockMvc.perform(get(url, clienteIdExistente)
                .param("fechaInicio", fechaInicio)
                .param("fechaFin", fechaFin)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Fecha Inválida"))
                .andExpect(jsonPath("$.message").value("El rango de fechas no puede ser mayor a 1 año"));
    }

    @Test
    void generarReporteEstadoCuenta_FechaFutura_DeberiaRetornarBadRequest() throws Exception {
        // Arrange
        String fechaInicio = "2025-11-15";
        String fechaFin = "2025-11-20";
        String url = "/api/reportes/{clienteId}";

        // Act & Assert
        mockMvc.perform(get(url, clienteIdExistente)
                .param("fechaInicio", fechaInicio)
                .param("fechaFin", fechaFin)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Fecha Inválida"))
                .andExpect(jsonPath("$.message").value("Las fechas no pueden ser futuras"));
    }

    @Test
    void generarReporteEstadoCuenta_ClienteSinMovimientosEnRango_DeberiaRetornarNotFound() throws Exception {
        // Arrange
        String fechaInicio = "2023-09-01";
        String fechaFin = "2023-09-30";
        String url = "/api/reportes/{clienteId}";

        // Act & Assert
        mockMvc.perform(get(url, clienteIdExistente)
                .param("fechaInicio", fechaInicio)
                .param("fechaFin", fechaFin)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Cuentas No Encontradas"))
                .andExpect(jsonPath("$.message").value("No se encontraron movimientos para el cliente ID: " + clienteIdExistente + " en el rango 2023-09-01 a 2023-09-30"));
    }

    @Test
    void generarReporteEstadoCuenta_VerificarDescripcionesMovimientos_DeberiaContenerDescripcionesCorrectas() throws Exception {
        // Arrange
        String fechaInicio = "2024-01-01";
        String fechaFin = "2024-01-31";
        String url = "/api/reportes/{clienteId}";

        // Act
        String response = mockMvc.perform(get(url, clienteIdExistente)
                .param("fechaInicio", fechaInicio)
                .param("fechaFin", fechaFin)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Assert
        EstadoCuentaReporteDTO reporte = objectMapper.readValue(response, EstadoCuentaReporteDTO.class);

        reporte.getCuentas().forEach(cuenta -> {
            cuenta.getMovimientos().forEach(movimiento -> {
                assertNotNull(movimiento.getDescripcion());
                if ("DEBITO".equals(movimiento.getTipoMovimiento())) {
                    assertTrue(movimiento.getDescripcion().contains("Debito"));
                } else if ("CREDITO".equals(movimiento.getTipoMovimiento())) {
                    assertTrue(movimiento.getDescripcion().contains("Crédito"));
                }
                assertTrue(movimiento.getDescripcion().contains(movimiento.getValor().toString()));
            });
        });
    }

    @Test
    void generarReporteEstadoCuenta_ValidarEstructuraCompletaDTO_DeberiaTenerTodosLosCamposRequeridos() throws Exception {
        // Arrange
        String fechaInicio = "2024-01-01";
        String fechaFin = "2024-01-31";
        String url = "/api/reportes/{clienteId}";

        // Act
        String response = mockMvc.perform(get(url, clienteIdExistente)
                .param("fechaInicio", fechaInicio)
                .param("fechaFin", fechaFin)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Assert
        EstadoCuentaReporteDTO reporte = objectMapper.readValue(response, EstadoCuentaReporteDTO.class);

        assertNotNull(reporte.getClienteId());
        assertNotNull(reporte.getFechaInicio());
        assertNotNull(reporte.getFechaFin());
        assertNotNull(reporte.getSaldoTotal());
        assertNotNull(reporte.getCuentas());
        assertFalse(reporte.getCuentas().isEmpty());

        reporte.getCuentas().forEach(cuenta -> {
            assertNotNull(cuenta.getCuentaId());
            assertNotNull(cuenta.getNumeroCuenta());
            assertNotNull(cuenta.getTipoCuenta());
            assertEquals("ACTIVA", cuenta.getEstado());
            assertNotNull(cuenta.getSaldoActual());
            assertNotNull(cuenta.getMovimientos());

            cuenta.getMovimientos().forEach(movimiento -> {
                assertNotNull(movimiento.getMovimientoId());
                assertNotNull(movimiento.getFecha());
                assertNotNull(movimiento.getTipoMovimiento());
                assertTrue("CREDITO".equals(movimiento.getTipoMovimiento()) || "DEBITO".equals(movimiento.getTipoMovimiento()));
                assertNotNull(movimiento.getValor());
                assertTrue(movimiento.getValor().compareTo(BigDecimal.ZERO) > 0);
                assertNotNull(movimiento.getDescripcion());
                assertNotNull(movimiento.getSaldoDespues());
            });
        });
    }

    @Test
    void generarReporteEstadoCuenta_SoloCuentasActivasIncluidas_DeberiaExcluirCuentasInactivas() throws Exception {
        // Arrange
        String fechaInicio = "2024-01-01";
        String fechaFin = "2024-01-31";
        String url = "/api/reportes/{clienteId}";

        // Act
        String response = mockMvc.perform(get(url, clienteIdExistente)
                .param("fechaInicio", fechaInicio)
                .param("fechaFin", fechaFin)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Assert
        EstadoCuentaReporteDTO reporte = objectMapper.readValue(response, EstadoCuentaReporteDTO.class);

        assertEquals(2, reporte.getCuentas().size());
        
        boolean cuentaInactivaPresente = reporte.getCuentas().stream()
                .anyMatch(cuenta -> "1000000003".equals(cuenta.getNumeroCuenta()));
        assertFalse(cuentaInactivaPresente, "La cuenta inactiva no debería estar incluida en el reporte");
        
        reporte.getCuentas().forEach(cuenta -> 
            assertEquals("ACTIVA", cuenta.getEstado(), "Todas las cuentas en el reporte deben estar activas")
        );
    }

    @Test
    void generarReporteEstadoCuenta_ParametrosFaltantes_DeberiaRetornarBadRequest() throws Exception {
        // Arrange
        String url = "/api/reportes/{clienteId}";

        // Act & Assert - Sin fechaInicio
        mockMvc.perform(get(url, clienteIdExistente)
                .param("fechaFin", "2024-01-31")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        // Act & Assert - Sin fechaFin
        mockMvc.perform(get(url, clienteIdExistente)
                .param("fechaInicio", "2024-01-01")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void generarReporteEstadoCuenta_CalculoSaldoTotalCorrecto_DeberiaSumarSaldosDeTodasLasCuentas() throws Exception {
        // Arrange
        String fechaInicio = "2024-01-01";
        String fechaFin = "2024-01-31";
        String url = "/api/reportes/{clienteId}";

        // Act
        String response = mockMvc.perform(get(url, clienteIdExistente)
                .param("fechaInicio", fechaInicio)
                .param("fechaFin", fechaFin)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Assert
        EstadoCuentaReporteDTO reporte = objectMapper.readValue(response, EstadoCuentaReporteDTO.class);

        BigDecimal saldoTotalCalculado = reporte.getCuentas().stream()
                .map(cuenta -> cuenta.getSaldoActual())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        assertEquals(0, saldoTotalCalculado.compareTo(reporte.getSaldoTotal()), 
                "El saldo total del reporte debe ser igual a la suma de los saldos de todas las cuentas");
    }
}