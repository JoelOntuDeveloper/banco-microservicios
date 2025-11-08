package banco.jontuna.ms_banco_cliente.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PersonaDTO {

    private Long personaId;
    
    @NotBlank(message = "La identificación es obligatoria")
    private String identificacion;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String nombre;

    @Size(max = 20)
    private String genero;

    @Min(0) @Max(150)
    private Integer edad;

    @Size(max = 200)
    private String direccion;

    @Size(max = 20)
    private String telefono;

    // Constructores
    public PersonaDTO() {}

    public PersonaDTO(String identificacion, String nombre, String genero, Integer edad, String direccion, String telefono) {
        this.identificacion = identificacion;
        this.nombre = nombre;
        this.genero = genero;
        this.edad = edad;
        this.direccion = direccion;
        this.telefono = telefono;
    }
}
