import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class CrearTablas {

    static final String URL = "jdbc:sqlite:virtuapet.db";

    static void crearTablas() {

        String sqlMascotas = "CREATE TABLE IF NOT EXISTS mascotas ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "nombre TEXT,"
                + "especie TEXT,"
                + "raza TEXT,"
                + "fechaNacimiento TEXT,"
                + "sexo TEXT,"
                + "color TEXT,"
                + "microchip TEXT UNIQUE"
                + ")";

        String sqlVacunas = "CREATE TABLE IF NOT EXISTS vacunas ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "mascota_microchip TEXT,"
                + "nombre TEXT,"
                + "fechaAplicacion TEXT,"
                + "fechaProximaDosis TEXT,"
                + "veterinario TEXT,"
                + "lote TEXT"
                + ")";

        String sqlRevisiones = "CREATE TABLE IF NOT EXISTS revision ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "mascota_microchip TEXT,"
                + "fechaRevision TEXT,"
                + "motivoRevision TEXT,"
                + "diagnostico TEXT,"
                + "notas TEXT,"
                + "veterinario TEXT"
                + ")";

        // NUEVA tabla: tratamientos. Mismo patrón que las anteriores.
        String sqlTratamientos = "CREATE TABLE IF NOT EXISTS tratamientos ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "mascota_microchip TEXT,"
                + "nombreMedicamento TEXT,"
                + "dosis TEXT,"
                + "frecuencia TEXT,"
                + "fechaInicio TEXT,"
                + "fechaFin TEXT"
                + ")";

        // NUEVA tabla: pesos. "peso" se guarda como REAL (número decimal),
        // no como TEXT, porque en RegistroPeso.java es un double, no un String.
        String sqlPesos = "CREATE TABLE IF NOT EXISTS pesos ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "mascota_microchip TEXT,"
                + "fecha TEXT,"
                + "peso REAL,"
                + "notas TEXT"
                + ")";

        try (Connection conexion = DriverManager.getConnection(URL);
             Statement sentencia = conexion.createStatement()) {

            sentencia.execute(sqlMascotas);
            sentencia.execute(sqlVacunas);
            sentencia.execute(sqlRevisiones);
            sentencia.execute(sqlTratamientos);
            sentencia.execute(sqlPesos);
            System.out.println("Tablas comprobadas/creadas correctamente.");

        } catch (SQLException e) {
            System.out.println("Error al crear las tablas: " + e.getMessage());
        }
    }
}
