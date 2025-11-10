package banco.jontuna.ms_banco_cuenta.repository.custom.impl;

import banco.jontuna.ms_banco_cuenta.model.Movimiento;
import banco.jontuna.ms_banco_cuenta.repository.custom.CustomMovimientoRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class CustomMovimientoRepositoryImpl implements CustomMovimientoRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Movimiento> findMovimientosByClienteAndFechaRange(Long clienteId, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        String jpql = """
            SELECT m FROM Movimiento m 
            JOIN m.cuenta c 
            WHERE c.clienteId = :clienteId 
            AND m.fecha BETWEEN :fechaInicio AND :fechaFin 
            ORDER BY m.fecha DESC
            """;

        TypedQuery<Movimiento> query = entityManager.createQuery(jpql, Movimiento.class);
        query.setParameter("clienteId", clienteId);
        query.setParameter("fechaInicio", fechaInicio);
        query.setParameter("fechaFin", fechaFin);

        return query.getResultList();
    }
}