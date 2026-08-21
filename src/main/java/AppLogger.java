import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class AppLogger {
    private static final Logger LOGGER = Logger.getLogger("VirtualPet");

    static {
        try {
            FileHandler fh = new FileHandler("virtuapet.log", true);
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
