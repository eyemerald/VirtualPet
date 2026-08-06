import java.time.LocalDate;

public class Vacuna {
    private String nombre;
    private LocalDate fechaAplicacion;
    private LocalDate fechaProximaDosis;
    private String veterinario;
    private String lote;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public LocalDate getFechaAplicacion() {
        return fechaAplicacion;
    }

    public void setFechaAplicacion(LocalDate fechaAplicacion) {
        this.fechaAplicacion = fechaAplicacion;
    }

    public LocalDate getFechaProximaDosis() {
        return fechaProximaDosis;
    }

    public void setFechaProximaDosis(LocalDate fechaProximaDosis) {
        this.fechaProximaDosis = fechaProximaDosis;
    }

    public String getVeterinario() {
        return veterinario;
    }

    public void setVeterinario(String veterinario) {
        this.veterinario = veterinario;
    }

    public String getLote() {
        return lote;
    }

    public void setLote(String lote) {
        this.lote = lote;
    }

    public Vacuna(String nombre, LocalDate fechaAplicacion, LocalDate fechaProximaDosis, String veterinario, String lote) {
        this.nombre = nombre;
        this.fechaAplicacion = fechaAplicacion;
        this.fechaProximaDosis = fechaProximaDosis;
        this.veterinario = veterinario;
        this.lote = lote;
    }
}