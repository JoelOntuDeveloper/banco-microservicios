package banco.jontuna.ms_banco_cuenta.repository.custom;

import banco.jontuna.ms_banco_cuenta.model.Movimiento;
import java.time.LocalDateTime;
import java.util.List;

public interface CustomMovimientoRepository {
    List<Movimiento> findMovimientosByClienteAndFechaRange(Long clienteId, LocalDateTime fechaInicio, LocalDateTime fechaFin);
}