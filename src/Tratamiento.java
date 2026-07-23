import java.time.LocalDate;

public class Tratamiento {
    private String nombreMedicamento;

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public String getFrecuencia() {
        return frecuencia;
    }

    public void setFrecuencia(String frecuencia) {
        this.frecuencia = frecuencia;
    }

    public String getDosis() {
        return dosis;
    }

    public void setDosis(String dosis) {
        this.dosis = dosis;
    }

    public String getNombreMedicamento() {
        return nombreMedicamento;
    }

    public void setNombreMedicamento(String nombreMedicamento) {
        this.nombreMedicamento = nombreMedicamento;
    }

    private String dosis;
    private String frecuencia;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    public Tratamiento(String nombreMedicamento, String dosis, String frecuencia, LocalDate fechaInicio, LocalDate fechaFin) {
        this.nombreMedicamento = nombreMedicamento;
        this.dosis = dosis;
        this.frecuencia = frecuencia;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
    }
}
