package banco.jontuna.ms_banco_cliente.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "CLIENTE")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long clienteId; // ✅ corresponde al campo AUTO_INCREMENT en la tabla

    @NotBlank(message = "La contraseña es obligatoria")
    private String contrasena;

    private String estado = "ACTIVO";

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "PersonaId", referencedColumnName = "PersonaId", nullable = false)
    private Persona persona;

    // Constructores
    public Cliente() {}

    public Cliente(String contrasena, Persona persona) {
        this.contrasena = contrasena;
        this.persona = persona;
    }
}
