import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

public class ExportadorMascota {

    static void escribirLinea(PDPageContentStream contenido, String texto,
                              float x, float y, float tamaño) throws IOException {
        escribirLinea(contenido, texto, x, y, tamaño, false);
    }

    // Con soporte de negrita, para diferenciar títulos de secciones del
    // texto normal — antes todo el PDF usaba el mismo peso de fuente,
    // lo que lo hacía verse plano y difícil de escanear visualmente.
    static void escribirLinea(PDPageContentStream contenido, String texto,
                              float x, float y, float tamaño, boolean negrita) throws IOException {
        String textoSeguro = texto.replace("⚠", "[AVISO]");
        Standard14Fonts.FontName fuente = negrita
                ? Standard14Fonts.FontName.HELVETICA_BOLD
                : Standard14Fonts.FontName.HELVETICA;
        contenido.beginText();
        contenido.setFont(new PDType1Font(fuente), tamaño);
        contenido.newLineAtOffset(x, y);
        contenido.showText(textoSeguro);
        contenido.endText();
    }

    // Línea horizontal fina, usada para separar visualmente cada sección
    // (vacunas, tratamientos...) de la siguiente — antes todo quedaba
    // apilado sin ninguna separación clara.
    static void dibujarSeparador(PDPageContentStream contenido, float x1, float y, float x2) throws IOException {
        contenido.setLineWidth(0.6f);
        contenido.moveTo(x1, y);
        contenido.lineTo(x2, y);
        contenido.stroke();
    }

    // Genera el nombre sugerido por defecto (fecha/hora incluida para no
    // chocar con exportaciones anteriores), para que el FileChooser lo
    // proponga ya relleno y el usuario solo tenga que elegir la carpeta.
    static String nombreSugerido(int idMascota, boolean esResumen) {
        String[] datos = GestorMascotas.obtenerDatosBasicos(idMascota);
        String nombreMascota = (datos == null || datos[0] == null) ? "mascota" : datos[0];
        String safeName = nombreMascota.replaceAll("[^a-zA-Z0-9-_.]", "_");
        if (safeName.length() > 50) safeName = safeName.substring(0, 50);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmmss");
        String timestamp = LocalDateTime.now().format(fmt);
        String sufijo = esResumen ? "_resumen_" : "_completo_";
        return safeName + sufijo + timestamp + ".pdf";
    }

    // Ahora recibe el archivo destino elegido por el propio usuario (con un
    // FileChooser en FichaMascotaVentana), en vez de guardarlo siempre en la
    // carpeta interna de la aplicación — la mayoría de usuarios no conoce
    // ni sabe llegar a %APPDATA%, así que dejamos que elijan dónde guardarlo
    // (Escritorio, Documentos, donde prefieran).
    static void exportarFicha(int idMascota, boolean esResumen, java.io.File destino) {

        String[] datos = GestorMascotas.obtenerDatosBasicos(idMascota);
        if (datos == null) {
            AppLogger.logWarning("No se encontró ninguna mascota con ese ID.");
            return;
        }

        float margen = 50;
        float y = 750;

        try (PDDocument documento = new PDDocument()) {
            PDPage pagina = new PDPage();
            documento.addPage(pagina);

            try (PDPageContentStream contenido = new PDPageContentStream(documento, pagina)) {

                // --- Logo en la cabecera ---
                URL logoUrl = ExportadorMascota.class.getResource("/images/logo.png");
                if (logoUrl != null) {
                    try {
                        PDImageXObject logoImage = PDImageXObject.createFromByteArray(documento, 
                            logoUrl.openStream().readAllBytes(), "logo.png");
                        float logoWidth = 50;
                        float logoHeight = 50 * (float)logoImage.getHeight() / logoImage.getWidth();
                        contenido.drawImage(logoImage, margen, y - logoHeight, logoWidth, logoHeight);
                        margen += logoWidth + 15;
                    } catch (Exception e) {
                        AppLogger.logWarning("No se pudo cargar el logo en el PDF: " + e.getMessage());
                    }
                }

                // --- Cabecera con los datos básicos ---
                String tituloPDF = esResumen ? "Ficha Resumen - " + datos[0] : "Ficha Clínica Completa - " + datos[0];
                escribirLinea(contenido, tituloPDF, margen, y, 16, true);
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
                    // --- Modo Resumen (Cuidador): lo importante primero,
                    // luego los tratamientos activos ---
                    y = escribirSeccion(contenido, "A tener en cuenta", GestorMascotas.obtenerLineasNotas(idMascota), margen, y);
                    ArrayList<String> tratamientosActivos = GestorMascotas.obtenerLineasTratamientos(idMascota, true);
                    y = escribirSeccion(contenido, "Tratamientos activos", tratamientosActivos, margen, y);
                } else {
                    // --- Modo Completo (Veterinario): todo el historial,
                    // con "A tener en cuenta" también destacado al principio ---
                    y = escribirSeccion(contenido, "A tener en cuenta", GestorMascotas.obtenerLineasNotas(idMascota), margen, y);
                    y = escribirSeccion(contenido, "Vacunas", GestorMascotas.obtenerLineasVacunas(idMascota), margen, y);
                    y = escribirSeccion(contenido, "Revisiones", GestorMascotas.obtenerLineasRevisiones(idMascota), margen, y);

                    ArrayList<String> tratamientosTodos = GestorMascotas.obtenerLineasTratamientos(idMascota, false);
                    y = escribirSeccion(contenido, "Tratamientos (historial completo)", tratamientosTodos, margen, y);

                    escribirSeccion(contenido, "Historial de peso", GestorMascotas.obtenerLineasPesos(idMascota), margen, y);
                }
            }

            // Guardamos directamente donde el usuario eligió con el
            // FileChooser — ya no calculamos nosotros ninguna carpeta.
            documento.save(destino.getAbsolutePath());
            AppLogger.logInfo("PDF generado correctamente: " + destino.getAbsolutePath());

        } catch (IOException e) {
            AppLogger.logSevere("Error al generar el PDF: " + e.getMessage());
        }
    }

    static float escribirSeccion(PDPageContentStream contenido, String titulo,
                                 ArrayList<String> lineas, float margen, float y) throws IOException {

        // Línea separadora fina antes del título, para que cada sección
        // se distinga claramente de la anterior de un vistazo
        dibujarSeparador(contenido, margen, y + 6, 545);

        escribirLinea(contenido, titulo, margen, y - 6, 13, true); // negrita
        y -= 24;

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