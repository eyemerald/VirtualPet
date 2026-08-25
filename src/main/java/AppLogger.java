import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.nio.file.Path;
import java.nio.file.Paths;

public class AppLogger {
    private static final Logger LOGGER = Logger.getLogger("VirtualPet");

    static {
        try {
            // Usar la misma carpeta fija que RutasApp (%APPDATA%\VirtualPet\)
            // para evitar problemas de permisos cuando se ejecuta desde el instalador
            String appData = System.getenv("APPDATA");
            Path logPath;
            if (appData != null && !appData.isBlank()) {
                logPath = Paths.get(appData, "VirtualPet", "virtuapet.log");
            } else {
                logPath = Paths.get(System.getProperty("user.home"), ".virtuapet", "virtuapet.log");
            }
            
            // Asegurar que el directorio padre existe
            java.nio.file.Files.createDirectories(logPath.getParent());
            
            FileHandler fh = new FileHandler(logPath.toString(), true);
            fh.setFormatter(new SimpleFormatter());
            LOGGER.addHandler(fh);
            LOGGER.setLevel(Level.INFO);
        } catch (Exception e) {
            System.err.println("No se pudo crear el archivo de log: " + e.getMessage());
        }
    }

    public static void logInfo(String msg) {
        LOGGER.info(msg);
    }

    public static void logWarning(String msg) {
        LOGGER.warning(msg);
    }

    public static void logSevere(String msg) {
        LOGGER.severe(msg);
    }
}
