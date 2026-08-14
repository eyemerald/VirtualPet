import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.IOException;
import java.util.ArrayList;
import java.nio.file.Path;
import java.nio.file.Files;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

public class ExportadorMascota {

    static void escribirLinea(PDPageContentStream contenido, String texto,
                              float x, float y, float tamaño) throws IOException {
        String textoSeguro = texto.replace("⚠", "[AVISO]");
        contenido.beginText();
        contenido.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), tamaño);
        contenido.newLineAtOffset(x, y);
        contenido.showText(textoSeguro);
        contenido.endText();
    }

    static void exportarFicha(int idMascota, boolean esResumen) {

        String[] datos = GestorMascotas.obtenerDatosBasicos(idMascota);
        if (datos == null) {
            System.out.println("No se encontró ninguna mascota con ese ID.");
            return;
        }

        float margen = 50;
        float y = 750;

        try (PDDocument documento = new PDDocument()) {
            PDPage pagina = new PDPage();
            documento.addPage(pagina);

            try (PDPageContentStream contenido = new PDPageContentStream(documento, pagina)) {

                // --- Cabecera con los datos básicos ---
                String tituloPDF = esResumen ? "Ficha Resumen - " + datos[0] : "Ficha Clínica Completa - " + datos[0];
                escribirLinea(contenido, tituloPDF, margen, y, 16);
                y -= 25;
                escribirLinea(contenido, "Especie: " + datos[1] + "   |   Raza: " + datos[2], margen, y, 11);
                y -= 15;
                escribirLinea(contenido, "Fecha de nacimiento: " + datos[3], margen, y, 11);
                y -= 15;
                escribirLinea(contenido, "Sexo: " + datos[4] + "   |   Color: " + datos[5], margen, y, 11);
                y -= 15;

                String chipTexto = (datos[6] == null || datos[6].trim().isEmpty()) ? "Sin microchip" : datos[6];
                escribirLinea(contenido, "Microchip: " + chipTexto, margen, y, 11);
                y -= 30;

                if (esResumen) {
                    // --- Modo Resumen (Cuidador): Tratamientos Activos únicamente ---
                    ArrayList<String> tratamientosActivos = GestorMascotas.obtenerLineasTratamientos(idMascota, true);
                    y = escribirSeccion(contenido, "Tratamientos activos", tratamientosActivos, margen, y);
                } else {
                    // --- Modo Completo (Veterinario): Todo el historial ---
                    y = escribirSeccion(contenido, "Vacunas", GestorMascotas.obtenerLineasVacunas(idMascota), margen, y);
                    y = escribirSeccion(contenido, "Revisiones", GestorMascotas.obtenerLineasRevisiones(idMascota), margen, y);

                    ArrayList<String> tratamientosTodos = GestorMascotas.obtenerLineasTratamientos(idMascota, false);
                    y = escribirSeccion(contenido, "Tratamientos (historial completo)", tratamientosTodos, margen, y);

                    escribirSeccion(contenido, "Historial de peso", GestorMascotas.obtenerLineasPesos(idMascota), margen, y);
                }
            }

            // Sanitizamos el nombre de archivo
            String nombreMascota = (datos[0] == null) ? "mascota" : datos[0];
            String safeName = nombreMascota.replaceAll("[^a-zA-Z0-9-_.]", "_");
            if (safeName.length() > 50) safeName = safeName.substring(0, 50);

            try {
                GestorArchivos.crearCarpetaMascota(idMascota, nombreMascota);
            } catch (Exception e) {
                AppLogger.logSevere("No se pudo crear la carpeta de la mascota: " + e.getMessage());
            }

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmmss");
            String timestamp = LocalDateTime.now().format(fmt);
            String sufijo = esResumen ? "_resumen_" : "_completo_";
            String nombreArchivo = safeName + sufijo + timestamp + ".pdf";

            Path carpeta = Path.of("mascotas", idMascota + "_" + GestorArchivos.safeName(nombreMascota));
            try {
                if (!Files.exists(carpeta)) Files.createDirectories(carpeta);
                Path destino = carpeta.resolve(nombreArchivo);
                documento.save(destino.toString());
                AppLogger.logInfo("PDF generado correctamente: " + destino.toString());
            } catch (IOException e) {
                AppLogger.logSevere("Error al guardar el PDF en la carpeta de la mascota: " + e.getMessage());
            }

        } catch (IOException e) {
            AppLogger.logSevere("Error al generar el PDF: " + e.getMessage());
        }
    }

    static float escribirSeccion(PDPageContentStream contenido, String titulo,
                                 ArrayList<String> lineas, float margen, float y) throws IOException {

        escribirLinea(contenido, titulo + ":", margen, y, 13);
        y -= 18;

        if (lineas.isEmpty()) {
            escribirLinea(contenido, "- (sin registros)", margen, y, 10);
            y -= 15;
        } else {
            for (String linea : lineas) {
                escribirLinea(contenido, "- " + linea, margen, y, 10);
                y -= 15;
            }
        }

        return y - 15;
    }
}