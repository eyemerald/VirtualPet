import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

// Centraliza DÓNDE vive todo lo que VirtualPet guarda en disco: la base de
// datos SQLite y la carpeta de cada mascota (PDFs exportados, informes...).
//
// Antes, cada clase usaba una ruta relativa ("virtualpet.db", "mascotas/..."),
// lo que funcionaba bien ejecutando desde el proyecto (mvn javafx:run), pero
// es frágil con el .exe instalado: la carpeta "de trabajo" real depende de
// desde dónde Windows lance el proceso, y puede no tener permisos de
// escritura (por ejemplo, dentro de "Archivos de Programa").
//
// La solución estándar en apps de escritorio es usar una carpeta fija en el
// perfil del propio usuario — en Windows, %APPDATA%\VirtualPet\ — que siempre
// existe y siempre es escribible por quien está usando el programa, sea cual
// sea la carpeta desde la que se lanzó el .exe.
public class RutasApp {

    // Se calcula una sola vez al arrancar el programa.
    private static final Path CARPETA_DATOS = calcularCarpetaDatos();

    private static Path calcularCarpetaDatos() {
        String appData = System.getenv("APPDATA"); // definido en Windows
        Path base;
        if (appData != null && !appData.isBlank()) {
            base = Paths.get(appData, "VirtualPet");
        } else {
            // Fallback para cuando no exista %APPDATA% (Linux/Mac, o un
            // Windows raro sin la variable definida): carpeta oculta en
            // el home del usuario, convención habitual fuera de Windows.
            base = Paths.get(System.getProperty("user.home"), ".virtuapet");
        }

        try {
            Files.createDirectories(base);
        } catch (IOException e) {
            AppLogger.logSevere("No se pudo crear la carpeta de datos de VirtualPet: " + e.getMessage());
        }

        return base;
    }

    // URL de conexión JDBC a la base de datos SQLite, ya apuntando a la
    // carpeta fija en vez de a una ruta relativa.
    static String getUrlBaseDatos() {
        return "jdbc:sqlite:" + getArchivoBaseDatos().toAbsolutePath();
    }

    // Ruta directa (no la URL JDBC) al archivo virtuapet.db, para casos
    // donde se necesita el propio archivo — por ejemplo, para incluirlo
    // en una copia de seguridad en .zip.
    static Path getArchivoBaseDatos() {
        return CARPETA_DATOS.resolve("virtuapet.db");
    }

    // Carpeta donde viven las subcarpetas de cada mascota (informes, PDFs
    // exportados...). Se crea si no existe todavía.
    static Path getCarpetaMascotas() {
        Path carpeta = CARPETA_DATOS.resolve("mascotas");
        try {
            Files.createDirectories(carpeta);
        } catch (IOException e) {
            AppLogger.logSevere("No se pudo crear la carpeta de mascotas: " + e.getMessage());
        }
        return carpeta;
    }
}
