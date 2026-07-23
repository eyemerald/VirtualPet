    import java.time.LocalDate;

    public class RegistroPeso {
        public LocalDate getFecha() {
            return fecha;
        }

        public void setFecha(LocalDate fecha) {
            this.fecha = fecha;
        }

        public double getPeso() {
            return peso;
        }

        public void setPeso(double peso) {
            this.peso = peso;
        }

        public String getNotas() {
            return notas;
        }

        public void setNotas(String notas) {
            this.notas = notas;
        }

        private LocalDate fecha;
        private double peso;
        private String notas;

        public RegistroPeso(LocalDate fecha, double peso, String notas) {
            this.fecha = fecha;
            this.peso = peso;
            this.notas = notas;
        }
    }

