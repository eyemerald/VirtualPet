import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;

public class GestorMascotas {

    static final String URL = "jdbc:sqlite:virtuapet.db";

    // ================== SELECCIÓN ==================

    static ArrayList<String> mostrarMascotasParaSeleccion() {
        ArrayList<String> microchips = new ArrayList<>();
        String sql = "SELECT nombre, especie, microchip FROM mascotas";

        try (Connection conexion = DriverManager.getConnection(URL);
             Statement sentencia = conexion.createStatement();
             ResultSet filas = sentencia.executeQuery(sql)) {

            int numero = 1;
            while (filas.next()) {
                System.out.println(numero + ". " + filas.getString("nombre")
                        + " (" + filas.getString("especie") + ")");
                microchips.add(filas.getString("microchip"));
                numero++;
            }

        } catch (SQLException e) {
            System.out.println("Error al leer las mascotas: " + e.getMessage());
        }

        return microchips;
    }

    // ================== GUARDAR ==================

    static void guardarMascota(Mascota m) {
        String sql = "INSERT INTO mascotas (nombre, especie, raza, fechaNacimiento, sexo, color, microchip) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conexion = DriverManager.getConnection(URL);
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, m.getNombre());
            sentencia.setString(2, m.getEspecie());
            sentencia.setString(3, m.getRaza());
            sentencia.setString(4, m.getFechaNacimiento().toString());
            sentencia.setString(5, m.getSexo());
            sentencia.setString(6, m.getColor());
            sentencia.setString(7, m.getMicrochip());

            sentencia.executeUpdate();
            System.out.println("Mascota guardada: " + m.getNombre());

        } catch (SQLException e) {
            System.out.println("Error al guardar la mascota: " + e.getMessage());
        }
    }

    static void guardarVacuna(String microchipMascota, Vacuna v) {
        String sql = "INSERT INTO vacunas (mascota_microchip, nombre, fechaAplicacion, fechaProximaDosis, veterinario, lote) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conexion = DriverManager.getConnection(URL);
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, microchipMascota);
            sentencia.setString(2, v.getNombre());
            sentencia.setString(3, v.getFechaAplicacion().toString());
            sentencia.setString(4, v.getFechaProximaDosis().toString());
            sentencia.setString(5, v.getVeterinario());
            sentencia.setString(6, v.getLote());

            sentencia.executeUpdate();
            System.out.println("Vacuna guardada para la mascota con microchip: " + microchipMascota);

        } catch (SQLException e) {
            System.out.println("Error al guardar la vacuna: " + e.getMessage());
        }
    }

    static void guardarRevision(String microchipMascota, Revision r) {
        String sql = "INSERT INTO revision (mascota_microchip, fechaRevision, motivoRevision, diagnostico, notas, veterinario) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conexion = DriverManager.getConnection(URL);
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, microchipMascota);
            sentencia.setString(2, r.getFecha().toString());
            sentencia.setString(3, r.getMotivo());
            sentencia.setString(4, r.getDiagnostico());
            sentencia.setString(5, r.getNotas());
            sentencia.setString(6, r.getVeterinario());

            sentencia.executeUpdate();
            System.out.println("Revisión guardada para la mascota con microchip: " + microchipMascota);

        } catch (SQLException e) {
            System.out.println("Error al guardar la revisión: " + e.getMessage());
        }
    }

    // NUEVO: guarda un tratamiento asociado a una mascota
    static void guardarTratamiento(String microchipMascota, Tratamiento t) {
        String sql = "INSERT INTO tratamientos (mascota_microchip, nombreMedicamento, dosis, frecuencia, fechaInicio, fechaFin) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conexion = DriverManager.getConnection(URL);
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, microchipMascota);
            sentencia.setString(2, t.getNombreMedicamento());
            sentencia.setString(3, t.getDosis());
            sentencia.setString(4, t.getFrecuencia());
            sentencia.setString(5, t.getFechaInicio().toString());
            sentencia.setString(6, t.getFechaFin().toString());

            sentencia.executeUpdate();
            System.out.println("Tratamiento guardado para la mascota con microchip: " + microchipMascota);

        } catch (SQLException e) {
            System.out.println("Error al guardar el tratamiento: " + e.getMessage());
        }
    }

    // NUEVO: guarda un registro de peso asociado a una mascota
    static void guardarPeso(String microchipMascota, RegistroPeso p) {
        String sql = "INSERT INTO pesos (mascota_microchip, fecha, peso, notas) "
                + "VALUES (?, ?, ?, ?)";

        try (Connection conexion = DriverManager.getConnection(URL);
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, microchipMascota);
            sentencia.setString(2, p.getFecha().toString());
            sentencia.setDouble(3, p.getPeso()); // setDouble, no setString: la columna es REAL
            sentencia.setString(4, p.getNotas());

            sentencia.executeUpdate();
            System.out.println("Registro de peso guardado para la mascota con microchip: " + microchipMascota);

        } catch (SQLException e) {
            System.out.println("Error al guardar el peso: " + e.getMessage());
        }
    }

    // ================== DATOS PARA EL PDF ==================
    // Estos métodos NO imprimen nada por consola: devuelven los datos ya
    // formateados como texto, para que ExportadorMascota los use tal cual.

    // Devuelve los datos básicos de una mascota como un array de texto,
    // siempre en este orden: [nombre, especie, raza, fechaNacimiento, sexo, color, microchip]
    // Devuelve null si no existe ninguna mascota con ese microchip.
    static String[] obtenerDatosBasicos(String microchip) {
        String sql = "SELECT nombre, especie, raza, fechaNacimiento, sexo, color, microchip "
                + "FROM mascotas WHERE microchip = ?";

        try (Connection conexion = DriverManager.getConnection(URL);
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, microchip);

            try (ResultSet fila = sentencia.executeQuery()) {
                if (fila.next()) {
                    return new String[]{
                            fila.getString("nombre"),
                            fila.getString("especie"),
                            fila.getString("raza"),
                            fila.getString("fechaNacimiento"),
                            fila.getString("sexo"),
                            fila.getString("color"),
                            fila.getString("microchip")
                    };
                }
            }

        } catch (SQLException e) {
            System.out.println("Error al leer los datos de la mascota: " + e.getMessage());
        }

        return null; // no se encontró ninguna mascota con ese microchip
    }

    static ArrayList<String> obtenerLineasVacunas(String microchip) {
        ArrayList<String> lineas = new ArrayList<>();
        String sql = "SELECT * FROM vacunas WHERE mascota_microchip = ?";

        try (Connection conexion = DriverManager.getConnection(URL);
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, microchip);

            try (ResultSet filas = sentencia.executeQuery()) {
                while (filas.next()) {
                    lineas.add(filas.getString("nombre") + " (" + filas.getString("fechaAplicacion")
                            + ", próxima: " + filas.getString("fechaProximaDosis") + ")");
                }
            }

        } catch (SQLException e) {
            System.out.println("Error al leer las vacunas: " + e.getMessage());
        }

        return lineas;
    }

    static ArrayList<String> obtenerLineasRevisiones(String microchip) {
        ArrayList<String> lineas = new ArrayList<>();
        String sql = "SELECT * FROM revision WHERE mascota_microchip = ?";

        try (Connection conexion = DriverManager.getConnection(URL);
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, microchip);

            try (ResultSet filas = sentencia.executeQuery()) {
                while (filas.next()) {
                    lineas.add(filas.getString("fechaRevision") + " - " + filas.getString("motivoRevision")
                            + " -> " + filas.getString("diagnostico"));
                }
            }

        } catch (SQLException e) {
            System.out.println("Error al leer las revisiones: " + e.getMessage());
        }

        return lineas;
    }

    // NUEVO, con filtro: si soloActuales es true, salta (con "continue")
    // los tratamientos cuya fechaFin ya haya pasado respecto a hoy.
    static ArrayList<String> obtenerLineasTratamientos(String microchip, boolean soloActuales) {
        ArrayList<String> lineas = new ArrayList<>();
        String sql = "SELECT * FROM tratamientos WHERE mascota_microchip = ?";

        try (Connection conexion = DriverManager.getConnection(URL);
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, microchip);

            try (ResultSet filas = sentencia.executeQuery()) {
                while (filas.next()) {
                    LocalDate fechaFin = LocalDate.parse(filas.getString("fechaFin"));
                    boolean finalizado = fechaFin.isBefore(LocalDate.now());

                    // Si solo queremos los actuales y este ya terminó, lo saltamos
                    if (soloActuales && finalizado) {
                        continue;
                    }

                    lineas.add(filas.getString("nombreMedicamento") + " (" + filas.getString("dosis")
                            + ", " + filas.getString("frecuencia") + ") del " + filas.getString("fechaInicio")
                            + " al " + filas.getString("fechaFin"));
                }
            }

        } catch (SQLException e) {
            System.out.println("Error al leer los tratamientos: " + e.getMessage());
        }

        return lineas;
    }

    static ArrayList<String> obtenerLineasPesos(String microchip) {
        ArrayList<String> lineas = new ArrayList<>();
        String sql = "SELECT * FROM pesos WHERE mascota_microchip = ?";

        try (Connection conexion = DriverManager.getConnection(URL);
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, microchip);

            try (ResultSet filas = sentencia.executeQuery()) {
                while (filas.next()) {
                    lineas.add(filas.getString("fecha") + ": " + filas.getDouble("peso")
                            + " kg (" + filas.getString("notas") + ")");
                }
            }

        } catch (SQLException e) {
            System.out.println("Error al leer los pesos: " + e.getMessage());
        }

        return lineas;
    }

    // ================== LEER (para consola) ==================

    static void mostrarTodasLasMascotas() {
        String sql = "SELECT * FROM mascotas";

        try (Connection conexion = DriverManager.getConnection(URL);
             Statement sentencia = conexion.createStatement();
             ResultSet filas = sentencia.executeQuery(sql)) {

            boolean hayMascotas = false;

            while (filas.next()) {
                hayMascotas = true;

                String nombre = filas.getString("nombre");
                String especie = filas.getString("especie");
                String raza = filas.getString("raza");
                String microchip = filas.getString("microchip");

                System.out.println("\n===== FICHA DE " + nombre.toUpperCase() + " =====");
                System.out.println("Especie: " + especie + " | Raza: " + raza);
                System.out.println("Microchip: " + microchip);

                mostrarVacunasDe(microchip);
                mostrarRevisionesDe(microchip);
                mostrarTratamientosDe(microchip);
                mostrarPesosDe(microchip);
            }

            if (!hayMascotas) {
                System.out.println("Todavía no hay ninguna mascota guardada.");
            }

        } catch (SQLException e) {
            System.out.println("Error al leer las mascotas: " + e.getMessage());
        }
    }

    static void mostrarVacunasDe(String microchip) {
        String sql = "SELECT * FROM vacunas WHERE mascota_microchip = ?";

        try (Connection conexion = DriverManager.getConnection(URL);
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, microchip);

            try (ResultSet filas = sentencia.executeQuery()) {
                System.out.println("Vacunas:");
                boolean hay = false;

                while (filas.next()) {
                    hay = true;
                    System.out.println("- " + filas.getString("nombre")
                            + " (" + filas.getString("fechaAplicacion")
                            + ", próxima: " + filas.getString("fechaProximaDosis") + ")");
                }

                if (!hay) {
                    System.out.println("- (sin vacunas registradas)");
                }
            }

        } catch (SQLException e) {
            System.out.println("Error al leer las vacunas: " + e.getMessage());
        }
    }

    static void mostrarRevisionesDe(String microchip) {
        String sql = "SELECT * FROM revision WHERE mascota_microchip = ?";

        try (Connection conexion = DriverManager.getConnection(URL);
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, microchip);

            try (ResultSet filas = sentencia.executeQuery()) {
                System.out.println("Revisiones:");
                boolean hay = false;

                while (filas.next()) {
                    hay = true;
                    System.out.println("- " + filas.getString("fechaRevision")
                            + " (" + filas.getString("motivoRevision")
                            + ", diagnóstico: " + filas.getString("diagnostico") + ")");
                }

                if (!hay) {
                    System.out.println("- (sin revisiones registradas)");
                }
            }

        } catch (SQLException e) {
            System.out.println("Error al leer las revisiones: " + e.getMessage());
        }
    }

    // NUEVO
    static void mostrarTratamientosDe(String microchip) {
        String sql = "SELECT * FROM tratamientos WHERE mascota_microchip = ?";

        try (Connection conexion = DriverManager.getConnection(URL);
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, microchip);

            try (ResultSet filas = sentencia.executeQuery()) {
                System.out.println("Tratamientos:");
                boolean hay = false;

                while (filas.next()) {
                    hay = true;
                    System.out.println("- " + filas.getString("nombreMedicamento")
                            + " (" + filas.getString("dosis") + ", " + filas.getString("frecuencia")
                            + ") del " + filas.getString("fechaInicio")
                            + " al " + filas.getString("fechaFin"));
                }

                if (!hay) {
                    System.out.println("- (sin tratamientos registrados)");
                }
            }

        } catch (SQLException e) {
            System.out.println("Error al leer los tratamientos: " + e.getMessage());
        }
    }

    // NUEVO
    static void mostrarPesosDe(String microchip) {
        String sql = "SELECT * FROM pesos WHERE mascota_microchip = ?";

        try (Connection conexion = DriverManager.getConnection(URL);
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, microchip);

            try (ResultSet filas = sentencia.executeQuery()) {
                System.out.println("Historial de peso:");
                boolean hay = false;

                while (filas.next()) {
                    hay = true;
                    // getDouble, no getString: la columna "peso" es REAL
                    System.out.println("- " + filas.getString("fecha") + ": "
                            + filas.getDouble("peso") + " kg (" + filas.getString("notas") + ")");
                }

                if (!hay) {
                    System.out.println("- (sin registros de peso)");
                }
            }

        } catch (SQLException e) {
            System.out.println("Error al leer los pesos: " + e.getMessage());
        }
    }
}