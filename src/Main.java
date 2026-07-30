import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {

    static Scanner lector = new Scanner(System.in);
    static DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static void main(String[] args) {

        CrearTablas.crearTablas();

        int opcion;

        do {
            mostrarMenu();
            opcion = Integer.parseInt(lector.nextLine());

            switch (opcion) {
                case 1:
                    crearMascotaNueva();
                    break;
                case 2:
                    añadirVacuna();
                    break;
                case 3:
                    añadirRevision();
                    break;
                case 4:
                    añadirTratamiento();
                    break;
                case 5:
                    añadirPeso();
                    break;
                case 6:
                    GestorMascotas.mostrarTodasLasMascotas();
                    break;
                case 7:
                    exportarPDF();
                    break;
                case 8:
                    System.out.println("Hasta luego.");
                    break;
                default:
                    System.out.println("Opción no válida, elige un número del 1 al 8.");
            }

        } while (opcion != 8);
    }

    static void mostrarMenu() {
        System.out.println("\n===== VIRTUAPET =====");
        System.out.println("1. Añadir mascota nueva");
        System.out.println("2. Añadir vacuna a una mascota");
        System.out.println("3. Añadir revisión a una mascota");
        System.out.println("4. Añadir tratamiento a una mascota");
        System.out.println("5. Añadir registro de peso a una mascota");
        System.out.println("6. Ver todas las mascotas");
        System.out.println("7. Exportar ficha de una mascota a PDF");
        System.out.println("8. Salir");
        System.out.print("Elige una opción: ");
    }

    // NUEVO
    static void exportarPDF() {
        String microchip = elegirMascota();
        if (microchip == null) return;

        System.out.println("¿Incluir tratamientos ya finalizados en el PDF? (si/no)");
        String respuesta = lector.nextLine().trim().toLowerCase();
        boolean soloActuales = !respuesta.equals("si"); // por defecto, solo actuales

        ExportadorMascota.exportarFicha(microchip, soloActuales);
    }

    // Método de ayuda: muestra la lista y devuelve el microchip elegido,
    // o null si el usuario cancela o no hay mascotas. Lo reutilizan
    // añadirVacuna, añadirRevision, añadirTratamiento y añadirPeso,
    // para no repetir la misma selección cuatro veces.
    static String elegirMascota() {
        System.out.println();
        ArrayList<String> microchips = GestorMascotas.mostrarMascotasParaSeleccion();

        if (microchips.isEmpty()) {
            System.out.println("Todavía no hay ninguna mascota guardada. Vuelve al menú y crea una primero.");
            return null;
        }

        System.out.println("0. Cancelar y volver al menú");
        System.out.print("Elige el número de la mascota: ");
        int seleccion = Integer.parseInt(lector.nextLine());

        if (seleccion == 0) {
            System.out.println("Cancelado.");
            return null;
        }

        if (seleccion < 1 || seleccion > microchips.size()) {
            System.out.println("Opción no válida.");
            return null;
        }

        return microchips.get(seleccion - 1);
    }

    static void crearMascotaNueva() {
        System.out.println("Nombre: ");
        String nombre = lector.nextLine();

        System.out.println("Especie: ");
        String especie = lector.nextLine();

        System.out.println("Raza: ");
        String raza = lector.nextLine();

        System.out.println("Fecha de nacimiento (dd/MM/aaaa): ");
        LocalDate fechaNacimiento = LocalDate.parse(lector.nextLine(), formato);

        System.out.println("Sexo: ");
        String sexo = lector.nextLine();

        System.out.println("Color: ");
        String color = lector.nextLine();

        System.out.println("Microchip: ");
        String microchip = lector.nextLine();

        Mascota nueva = new Mascota(nombre, especie, raza, fechaNacimiento, sexo, color, microchip);
        GestorMascotas.guardarMascota(nueva);
    }

    static void añadirVacuna() {
        String microchip = elegirMascota();
        if (microchip == null) return;

        System.out.println("Nombre de la vacuna: ");
        String nombreVacuna = lector.nextLine();

        System.out.println("Fecha de aplicación (dd/MM/aaaa): ");
        LocalDate fechaAplicacion = LocalDate.parse(lector.nextLine(), formato);

        System.out.println("Fecha de próxima dosis (dd/MM/aaaa): ");
        LocalDate fechaProximaDosis = LocalDate.parse(lector.nextLine(), formato);

        System.out.println("Veterinario: ");
        String veterinario = lector.nextLine();

        System.out.println("Lote: ");
        String lote = lector.nextLine();

        Vacuna v = new Vacuna(nombreVacuna, fechaAplicacion, fechaProximaDosis, veterinario, lote);
        GestorMascotas.guardarVacuna(microchip, v);
    }

    // NUEVO
    static void añadirRevision() {
        String microchip = elegirMascota();
        if (microchip == null) return;

        System.out.println("Fecha de la revisión (dd/MM/aaaa): ");
        LocalDate fecha = LocalDate.parse(lector.nextLine(), formato);

        System.out.println("Motivo: ");
        String motivo = lector.nextLine();

        System.out.println("Diagnóstico: ");
        String diagnostico = lector.nextLine();

        System.out.println("Notas: ");
        String notas = lector.nextLine();

        System.out.println("Veterinario: ");
        String veterinario = lector.nextLine();

        Revision r = new Revision(fecha, motivo, diagnostico, notas, veterinario);
        GestorMascotas.guardarRevision(microchip, r);
    }

    // NUEVO
    static void añadirTratamiento() {
        String microchip = elegirMascota();
        if (microchip == null) return;

        System.out.println("Nombre del medicamento: ");
        String nombreMedicamento = lector.nextLine();

        System.out.println("Dosis: ");
        String dosis = lector.nextLine();

        System.out.println("Frecuencia: ");
        String frecuencia = lector.nextLine();

        System.out.println("Fecha de inicio (dd/MM/aaaa): ");
        LocalDate fechaInicio = LocalDate.parse(lector.nextLine(), formato);

        System.out.println("Fecha de fin (dd/MM/aaaa): ");
        LocalDate fechaFin = LocalDate.parse(lector.nextLine(), formato);

        Tratamiento t = new Tratamiento(nombreMedicamento, dosis, frecuencia, fechaInicio, fechaFin);
        GestorMascotas.guardarTratamiento(microchip, t);
    }

    // NUEVO
    static void añadirPeso() {
        String microchip = elegirMascota();
        if (microchip == null) return;

        System.out.println("Fecha del registro (dd/MM/aaaa): ");
        LocalDate fecha = LocalDate.parse(lector.nextLine(), formato);

        System.out.println("Peso (en kg, ejemplo 11.8): ");
        double peso = Double.parseDouble(lector.nextLine());

        System.out.println("Notas: ");
        String notas = lector.nextLine();

        RegistroPeso p = new RegistroPeso(fecha, peso, notas);
        GestorMascotas.guardarPeso(microchip, p);
    }
}