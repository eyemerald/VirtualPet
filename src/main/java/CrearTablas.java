import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class CrearTablas {

    static final String URL = "jdbc:sqlite:virtuapet.db";

    static void crearTablas() {

        // "microchip" ya no lleva UNIQUE ni es obligatorio: no todos los
        // animales llevan chip. El identificador real vuelve a ser "id".
        String sqlMascotas = "CREATE TABLE IF NOT EXISTS mascotas ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "nombre TEXT,"
                + "especie TEXT,"
                + "raza TEXT,"
                + "fechaNacimiento TEXT,"
                + "sexo TEXT,"
                + "color TEXT,"
                + "microchip TEXT"
                + ")";

        // Todas las tablas relacionadas usan "mascota_id" (número), que
        // se relaciona con la columna "id" de mascotas.
        String sqlVacunas = "CREATE TABLE IF NOT EXISTS vacunas ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "mascota_id INTEGER,"
                + "nombre TEXT,"
                + "fechaAplicacion TEXT,"
                + "fechaProximaDosis TEXT,"
                + "veterinario TEXT,"
                + "lote TEXT,"
                + "FOREIGN KEY (mascota_id) REFERENCES mascotas(id) ON DELETE CASCADE"
                + ")";

        String sqlRevisiones = "CREATE TABLE IF NOT EXISTS revision ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "mascota_id INTEGER,"
                + "fechaRevision TEXT,"
                + "motivoRevision TEXT,"
                + "diagnostico TEXT,"
                + "notas TEXT,"
                + "veterinario TEXT,"
                + "FOREIGN KEY (mascota_id) REFERENCES mascotas(id) ON DELETE CASCADE"
                + ")";

        // fechaFin puede quedar en NULL si el tratamiento es crónico/indefinido
        // (SQLite permite NULL en cualquier columna por defecto, no hace
        // falta declarar nada especial para permitirlo)
        String sqlTratamientos = "CREATE TABLE IF NOT EXISTS tratamientos ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "mascota_id INTEGER,"
                + "nombreMedicamento TEXT,"
                + "dosis TEXT,"
                + "frecuencia TEXT,"
                + "fechaInicio TEXT,"
                + "fechaFin TEXT,"
                + "FOREIGN KEY (mascota_id) REFERENCES mascotas(id) ON DELETE CASCADE"
                + ")";

        String sqlPesos = "CREATE TABLE IF NOT EXISTS pesos ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "mascota_id INTEGER,"
                + "fecha TEXT,"
                + "peso REAL,"
                + "notas TEXT,"
                + "FOREIGN KEY (mascota_id) REFERENCES mascotas(id) ON DELETE CASCADE"
                + ")";

        try (Connection conexion = DriverManager.getConnection(URL);
             Statement sentencia = conexion.createStatement()) {

            sentencia.execute("PRAGMA foreign_keys = ON");

            sentencia.execute(sqlMascotas);
            sentencia.execute(sqlVacunas);
            sentencia.execute(sqlRevisiones);
            sentencia.execute(sqlTratamientos);
            sentencia.execute(sqlPesos);
            AppLogger.logInfo("Tablas comprobadas/creadas correctamente.");

        } catch (SQLException e) {
            AppLogger.logSevere("Error al crear las tablas: " + e.getMessage());
        }
    }
}
