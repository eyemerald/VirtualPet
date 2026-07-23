import java.time.LocalDate;
import java.util.ArrayList;
import java.time.Period;


public class Main {
    public static void main(String[] args) {

        // ================== MASCOTA 1: TOBY ==================

        // 1. Creamos una mascota de prueba (esto ya lo tenías)
        Mascota toby = new Mascota("Toby", "Perro", "French Bulldog",
                LocalDate.of(2022, 3, 15), "Macho", "Marrón", "941000012345678");

        // 2. Comprobamos los datos básicos
        System.out.println("Nombre: " + toby.getNombre());
        System.out.println("Especie: " + toby.getEspecie());
        System.out.println("Raza: " + toby.getRaza());
        System.out.println("Fecha de nacimiento: " + toby.getFechaNacimiento());

        // 3. Añadimos una vacuna (esto ya lo tenías)
        Vacuna rabia = new Vacuna("Rabia", LocalDate.of(2026, 1, 10),
                LocalDate.of(2027, 1, 10), "Dr. García", "LOTE-2026-01");
        toby.getVacunas().add(rabia);

        // 4. NUEVO: añadimos una revisión veterinaria
        // Igual que con la vacuna: creas el objeto Revision y lo metes en la lista de Toby
        Revision chequeo = new Revision(LocalDate.of(2026, 6, 20), "Revisión anual",
                "Buen estado general", "Peso ligeramente por encima de lo ideal", "Dra. Martín");
        toby.getRevisiones().add(chequeo);

        // 5. NUEVO: añadimos un tratamiento
        Tratamiento antibiotico = new Tratamiento("Amoxicilina", "250 mg",
                "Cada 12 horas", LocalDate.of(2026, 6, 20), LocalDate.of(2026, 6, 30));
        toby.getTratamientos().add(antibiotico);

        // 6. NUEVO: añadimos un registro de peso
        RegistroPeso pesoJulio = new RegistroPeso(LocalDate.of(2026, 7, 1), 11.8,
                "Pesado en consulta");
        toby.getPesos().add(pesoJulio);

        // ================== MASCOTA 2: LUNA ==================
        // NUEVO: creamos una segunda mascota, totalmente independiente de Toby.
        // Cada Mascota tiene sus propias listas (vacunas, revisiones...) porque
        // se crean dentro del constructor de Mascota (new ArrayList<>()) para cada objeto.

        Mascota luna = new Mascota("Luna", "Gato", "Europeo Común",
                LocalDate.of(2023, 8, 2), "Hembra", "Gris atigrado", "941000098765432");

        Vacuna trivalente = new Vacuna("Trivalente felina", LocalDate.of(2026, 2, 5),
                LocalDate.of(2027, 2, 5), "Dr. García", "LOTE-2026-05");
        luna.getVacunas().add(trivalente);

        ArrayList<Mascota> todasLasMascotas = new ArrayList<>();
        todasLasMascotas.add(toby);
        todasLasMascotas.add(luna);

        System.out.println("\n\n########## LISTADO GENERAL (" + todasLasMascotas.size() + " mascotas) ##########");

        for (Mascota m : todasLasMascotas) {
            mostrarFichaCompleta(m);
        }
    }

    // ================== MÉTODO PROPIO ==================
    // NUEVO: en vez de repetir 4 bloques de "for" cada vez que quiero imprimir
    // una mascota (uno para vacunas, otro para revisiones...), lo meto todo
    // en un método. Así main() queda más limpio y este código se reutiliza
    // para Toby, Luna, o cualquier mascota futura.
    static void mostrarFichaCompleta(Mascota m) {
        System.out.println("\n===== FICHA DE " + m.getNombre().toUpperCase() + " =====");
        System.out.println("Especie: " + m.getEspecie() + " | Raza: " + m.getRaza());
        System.out.println("Edad: " + calcularEdad(m));   // ← esta línea nueva aquí

        System.out.println("\nVacunas:");
        for (Vacuna v : m.getVacunas()) {
            System.out.println("- " + v.getNombre() + " (" + v.getFechaAplicacion()
                    + ", próxima: " + v.getFechaProximaDosis() + ")");
        }

        System.out.println("\nRevisiones:");
        for (Revision r : m.getRevisiones()) {
            System.out.println("- " + r.getFecha() + ": " + r.getMotivo()
                    + " -> " + r.getDiagnostico());
        }

        System.out.println("\nTratamientos:");
        for (Tratamiento t : m.getTratamientos()) {
            System.out.println("- " + t.getNombreMedicamento() + " (" + t.getDosis()
                    + ", " + t.getFrecuencia() + ") del " + t.getFechaInicio()
                    + " al " + t.getFechaFin());
        }

        System.out.println("\nHistorial de peso:");
        for (RegistroPeso p : m.getPesos()) {
            System.out.println("- " + p.getFecha() + ": " + p.getPeso() + " kg (" + p.getNotas() + ")");
        }
    }
    static String calcularEdad(Mascota m) {
        Period edad = Period.between(m.getFechaNacimiento(), LocalDate.now());
        return edad.getYears() + " años y " + edad.getMonths() + " meses";
    }

}