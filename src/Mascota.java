import java.time.LocalDate;
import java.util.ArrayList;

    public class Mascota {
        private String nombre;
        private String especie;

        public ArrayList<RegistroPeso> getPesos() {
            return pesos;
        }

        public void setPesos(ArrayList<RegistroPeso> pesos) {
            this.pesos = pesos;
        }

        public ArrayList<Tratamiento> getTratamientos() {
            return tratamientos;
        }

        public void setTratamientos(ArrayList<Tratamiento> tratamientos) {
            this.tratamientos = tratamientos;
        }

        public ArrayList<Revision> getRevisiones() {
            return revisiones;
        }

        public void setRevisiones(ArrayList<Revision> revisiones) {
            this.revisiones = revisiones;
        }

        public ArrayList<Vacuna> getVacunas() {
            return vacunas;
        }

        public void setVacunas(ArrayList<Vacuna> vacunas) {
            this.vacunas = vacunas;
        }

        public String getMicrochip() {
            return microchip;
        }

        public void setMicrochip(String microchip) {
            this.microchip = microchip;
        }

        public String getColor() {
            return color;
        }

        public void setColor(String color) {
            this.color = color;
        }

        public String getSexo() {
            return sexo;
        }

        public void setSexo(String sexo) {
            this.sexo = sexo;
        }

        public LocalDate getFechaNacimiento() {
            return fechaNacimiento;
        }

        public void setFechaNacimiento(LocalDate fechaNacimiento) {
            this.fechaNacimiento = fechaNacimiento;
        }

        public String getRaza() {
            return raza;
        }

        public void setRaza(String raza) {
            this.raza = raza;
        }

        public String getEspecie() {
            return especie;
        }

        public void setEspecie(String especie) {
            this.especie = especie;
        }

        public String getNombre() {
            return nombre;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }

        private String raza;
        private LocalDate fechaNacimiento;
        private String sexo;
        private String color;
        private String microchip;

        private ArrayList<Vacuna> vacunas;
        private ArrayList<Revision> revisiones;
        private ArrayList<Tratamiento> tratamientos;
        private ArrayList<RegistroPeso> pesos;

        public Mascota(String nombre, String especie, String raza, LocalDate fechaNacimiento, String sexo, String color, String microchip){
            this.nombre = nombre;
            this.especie = especie;
            this.raza = raza;
            this.fechaNacimiento = fechaNacimiento;
            this.sexo = sexo;
            this.color = color;
            this.microchip = microchip;

            this.vacunas = new ArrayList<>();
            this.revisiones = new ArrayList<>();
            this.tratamientos = new ArrayList<>();
            this.pesos = new ArrayList<>();
        }
    }
