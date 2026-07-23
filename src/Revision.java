import java.time.LocalDate;

public class Revision {
    private LocalDate fecha;
    private String motivo;
    private String diagnostico;
    private String notas;

    public String getVeterinario() {
        return veterinario;
    }

    public void setVeterinario(String veterinario) {
        this.veterinario = veterinario;
    }

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }

    public String getDiagnostico() {
        return diagnostico;
    }

    public void setDiagnostico(String diagnostico) {
        this.diagnostico = diagnostico;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    private String veterinario;

    public Revision(LocalDate fecha, String motivo, String diagnostico, String notas, String veterinario) {
        this.fecha = fecha;
        this.motivo = motivo;
        this.diagnostico = diagnostico;
        this.notas = notas;
        this.veterinario = veterinario;
    }
}
