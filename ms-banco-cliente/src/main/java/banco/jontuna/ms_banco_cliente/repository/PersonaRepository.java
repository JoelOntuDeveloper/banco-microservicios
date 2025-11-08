package banco.jontuna.ms_banco_cliente.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import banco.jontuna.ms_banco_cliente.model.Persona;

@Repository
public interface PersonaRepository extends JpaRepository<Persona, Long> {
    
    @Query("SELECT p FROM Persona p WHERE p.identificacion = :identificacion")
    Optional<Persona> findByIdentificacion(@Param("identificacion") String identificacion);
    
    @Query("SELECT COUNT(p) > 0 FROM Persona p WHERE p.identificacion = :identificacion")
    boolean existsByIdentificacion(@Param("identificacion") String identificacion);
}
