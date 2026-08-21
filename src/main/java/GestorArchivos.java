import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
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

    // Genera un único .zip con TODO lo que VirtualPet guarda: la base de
    // datos (virtualpet.db) y la carpeta "mascotas" completa (todas las
    // subcarpetas, con sus PDFs e informes). Pensado como copia de
    // seguridad manual — y, el día de mañana, como el mismo formato que
    // la app Android podría leer para importar los datos vía WiFi.
    static void exportarTodo(File destinoZip) throws IOException {
        try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(destinoZip.toPath()))) {

            // 1. La base de datos, en la raíz del zip
            Path archivoDb = RutasApp.getArchivoBaseDatos();
            if (Files.exists(archivoDb)) {
                agregarArchivoAlZip(zip, archivoDb, "virtuapet.db");
            }

            // 2. La carpeta "mascotas" completa, recorriendo todo lo que
            // haya dentro (subcarpetas por mascota, con sus documentos)
            Path carpetaMascotas = RutasApp.getCarpetaMascotas();
            if (Files.exists(carpetaMascotas)) {
                Files.walkFileTree(carpetaMascotas, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        // Ruta dentro del zip: relativa a "mascotas", nunca
                        // la ruta absoluta del disco de este usuario
                        Path relativa = carpetaMascotas.relativize(file);
                        String rutaEnZip = "mascotas/" + relativa.toString().replace("\\", "/");
                        agregarArchivoAlZip(zip, file, rutaEnZip);
                        return FileVisitResult.CONTINUE;
                    }
                });
            }
        }
    }

    private static void agregarArchivoAlZip(ZipOutputStream zip, Path archivo, String nombreEnZip) throws IOException {
        zip.putNextEntry(new ZipEntry(nombreEnZip));
        try (FileInputStream entrada = new FileInputStream(archivo.toFile())) {
            entrada.transferTo(zip);
        }
        zip.closeEntry();
    }
}
