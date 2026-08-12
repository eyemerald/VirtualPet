import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;
public class GestorArchivos {

static String safeName(String s) {
    if (s == null) return "";
    String cleaned = s.replaceAll("[\\\\/:*?\"<>|]", "_");
    cleaned = cleaned.trim().replaceAll("\\s+", "_");
    if (cleaned.length() > 100) cleaned = cleaned.substring(0, 100);
    return cleaned;
}

    static void crearCarpetaMascota(int idMascota, String nombre) throws IOException {
        String safe = safeName(nombre);
        Path carpeta = RutasApp.getCarpetaMascotas().resolve(idMascota + "_" + safe);
        if (!Files.exists(carpeta)) {
            Files.createDirectories(carpeta);
        }
    }

    static void renombrarCarpetaMascota(int idMascota, String nombreAntiguo, String nombreNuevo) throws IOException {
        String safeAnt = safeName(nombreAntiguo);
        String safeNew = safeName(nombreNuevo);
        if (Objects.equals(safeAnt, safeNew)) return; // nada que hacer

        Path carpetaAnt = RutasApp.getCarpetaMascotas().resolve(idMascota + "_" + safeAnt);
        Path carpetaNew = RutasApp.getCarpetaMascotas().resolve(idMascota + "_" + safeNew);

        if (!Files.exists(carpetaAnt)) return; // nada que renombrar

        Files.move(carpetaAnt, carpetaNew);
    }

    static void borrarCarpetaMascota(int idMascota, String nombre) throws IOException {
        String safe = safeName(nombre);
        Path carpeta = RutasApp.getCarpetaMascotas().resolve(idMascota + "_" + safe);
        if (!Files.exists(carpeta)) return;

        Files.walkFileTree(carpeta, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
