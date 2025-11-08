package banco.jontuna.ms_banco_cliente.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import banco.jontuna.ms_banco_cliente.model.Cliente;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    @Query("SELECT c FROM Cliente c WHERE c.persona.identificacion = :identificacion")
    Optional<Cliente> findByPersonaIdentificacion(@Param("identificacion") String identificacion);
    
    @Query("SELECT c FROM Cliente c WHERE c.estado = :estado")
    List<Cliente> findByEstado(@Param("estado") String estado);
    
    @Query("SELECT COUNT(c) > 0 FROM Cliente c WHERE c.persona.identificacion = :identificacion")
    boolean existsByPersonaIdentificacion(@Param("identificacion") String identificacion);
}
