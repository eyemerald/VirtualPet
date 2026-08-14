import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Optional;

public class FichaMascotaVentana {

    public static void abrir(int idMascota) {
        String[] datos = GestorMascotas.obtenerDatosBasicos(idMascota);
        if (datos == null) {
            mostrarAviso(AlertType.ERROR, "No se encontró esa mascota.");
            return;
        }
        Navegador.navegarA("Ficha de " + datos[0], () -> crearVista(idMascota));
    }

    public static Node crearVista(int idMascota) {
        String[] datos = GestorMascotas.obtenerDatosBasicos(idMascota);
        if (datos == null) {
            return new Label("No se encontró esa mascota.");
        }

        Label titulo = new Label(datos[0]);
        titulo.getStyleClass().add("pet-name");

        Label infoBasica = new Label(
                "Especie: " + datos[1] + "   |   Raza: " + datos[2] + "\n"
                        + "Nacimiento: " + datos[3] + " (" + GestorMascotas.calcularEdad(datos[3]) + ")   |   Sexo: " + datos[4] + "\n"
                        + "Color: " + datos[5] + "   |   Microchip: " + datos[6]
        );
        infoBasica.getStyleClass().add("info-text");

        Label avisoVacunas = new Label(construirAvisoVacunas(idMascota));
        avisoVacunas.getStyleClass().add("aviso-vacunas");
        avisoVacunas.setWrapText(true);

        // Avatar circular con inicial
        Label avatar = new Label(datos[0].isEmpty() ? "?" : datos[0].substring(0, 1).toUpperCase());
        avatar.getStyleClass().add("avatar-circle");
        avatar.setAlignment(Pos.CENTER);

        VBox textoCabecera = new VBox(4, titulo, infoBasica);

        HBox headerContent = new HBox(14, avatar, textoCabecera);
        headerContent.setAlignment(Pos.CENTER_LEFT);
        headerContent.getStyleClass().add("card");
        headerContent.setPadding(new Insets(14));

        // --- Tarjetas de resumen: VACUNAS / TRATAMIENTOS / PESOS / INFORMES ---
        Label contenidoVacunas = crearContenidoTarjeta();
        Label contenidoTratamientos = crearContenidoTarjeta();
        Label contenidoPesos = crearContenidoTarjeta();
        Label contenidoInformes = crearContenidoTarjeta();

        Runnable refrescarTarjetas = () -> {
            contenidoVacunas.setText(obtenerProximaVacuna(idMascota));
            contenidoTratamientos.setText(obtenerTratamientosResumen(idMascota));
            contenidoPesos.setText(obtenerUltimoPesoResumen(idMascota));
            contenidoInformes.setText(obtenerInformesResumen(idMascota));
            avisoVacunas.setText(construirAvisoVacunas(idMascota));
        };

        VBox cardVacunas = crearTarjetaClicable("Vacunas", Feather.SHIELD, "#4A9B7F", contenidoVacunas,
                () -> GestionVacunasVentana.abrir(idMascota, refrescarTarjetas));

        VBox cardTratamientos = crearTarjetaClicable("Tratamientos", Feather.CLIPBOARD, "#5B7C99", contenidoTratamientos,
                () -> GestionTratamientosVentana.abrir(idMascota, refrescarTarjetas));

        VBox cardPesos = crearTarjetaClicable("Pesos", Feather.TRENDING_UP, "#4A9B7F", contenidoPesos,
                () -> GestionPesosVentana.abrir(idMascota, refrescarTarjetas));

        VBox cardInformes = crearTarjetaClicable("Informes", Feather.FILE_TEXT, "#5B7C99", contenidoInformes,
                () -> GestionInformesVentana.abrir(idMascota, refrescarTarjetas));

        FlowPane filaTarjetas = new FlowPane(12, 12, cardVacunas, cardTratamientos, cardPesos, cardInformes);

        refrescarTarjetas.run(); // relleno inicial

        Button botonModificar = new Button("Modificar datos");
        FontIcon iconoModificar = new FontIcon(Feather.EDIT_2);
        iconoModificar.setIconSize(16);
        botonModificar.setGraphic(iconoModificar);
        botonModificar.setOnAction(e -> {
            FormularioMascota.abrirEdicion(idMascota, datos);
        });

        Button botonExportar = new Button("Exportar a PDF");
        FontIcon iconoExportar = new FontIcon(Feather.DOWNLOAD);
        iconoExportar.setIconSize(16);
        botonExportar.setGraphic(iconoExportar);
        botonExportar.getStyleClass().add("btn-primary");

        botonExportar.setOnAction(evento -> {
            ButtonType btnCompleto = new ButtonType("Completo (Veterinario)");
            ButtonType btnResumen = new ButtonType("Resumen (Cuidador)");
            ButtonType btnCancelar = ButtonType.CANCEL;

            Alert dialogo = new Alert(AlertType.CONFIRMATION,
                    "¿Qué tipo de informe deseas generar?",
                    btnCompleto, btnResumen, btnCancelar);
            dialogo.setHeaderText("Opciones de exportación PDF");
            dialogo.setTitle("Exportar informe");

            Optional<ButtonType> seleccion = dialogo.showAndWait();
            if (!seleccion.isPresent() || seleccion.get() == btnCancelar) {
                return;
            }

            boolean esResumen = (seleccion.get() == btnResumen);

            botonExportar.setDisable(true);
            new Thread(() -> {
                try {
                    ExportadorMascota.exportarFicha(idMascota, esResumen);
                    javafx.application.Platform.runLater(() -> {
                        String tipoTexto = esResumen ? "Resumen" : "Completo";
                        mostrarAviso(AlertType.INFORMATION, "PDF (" + tipoTexto + ") generado correctamente.");
                        botonExportar.setDisable(false);
                    });
                } catch (Exception e) {
                    javafx.application.Platform.runLater(() -> mostrarAviso(AlertType.ERROR, "Error exportando PDF: " + e.getMessage()));
                    botonExportar.setDisable(false);
                }
            }, "exportar-pdf-thread").start();
        });

        Button botonBorrar = new Button("Borrar mascota");
        FontIcon iconoBorrar = new FontIcon(Feather.TRASH_2);
        iconoBorrar.setIconSize(16);
        botonBorrar.setGraphic(iconoBorrar);
        botonBorrar.getStyleClass().add("btn-danger");
        botonBorrar.setOnAction(evento -> {
            ButtonType conservar = new ButtonType("Conservar");
            ButtonType borrar = new ButtonType("Borrar");
            ButtonType cancelar = ButtonType.CANCEL;

            Alert confirmacion = new Alert(AlertType.CONFIRMATION,
                    "¿Qué deseas hacer con la documentación asociada a " + datos[0] + "?",
                    conservar, borrar, cancelar);
            confirmacion.setHeaderText("¿Borrar mascota y datos?");
            Optional<ButtonType> respuesta = confirmacion.showAndWait();

            if (!respuesta.isPresent() || respuesta.get() == cancelar) return;

            GestorMascotas.borrarMascota(idMascota);

            if (respuesta.get() == borrar) {
                try {
                    GestorArchivos.borrarCarpetaMascota(idMascota, datos[0]);
                } catch (Exception e) {
                    System.out.println("Aviso: no se pudo borrar la carpeta de la mascota: " + e.getMessage());
                }
            }

            VirtuaPetApp.cargarListaMascotas();
            Navegador.volverAlInicio();
        });

        FlowPane filaBotonesMascota = new FlowPane(8, 8, botonModificar, botonExportar, botonBorrar);
        filaBotonesMascota.getStyleClass().add("flow-pane-buttons");

        VBox contenido = new VBox(14, headerContent, avisoVacunas, filaTarjetas, filaBotonesMascota);
        contenido.setPadding(new Insets(18));
        contenido.getStyleClass().add("root");

        ScrollPane scroll = new ScrollPane(contenido);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        return scroll;
    }

    private static Label crearContenidoTarjeta() {
        Label contenido = new Label();
        contenido.getStyleClass().add("card-content");
        contenido.setWrapText(true);
        contenido.setMaxWidth(190);
        return contenido;
    }

    private static VBox crearTarjetaClicable(String titulo, Feather icono, String colorHex, Label contenido, Runnable alHacer) {
        Label tituloLabel = new Label(titulo);
        tituloLabel.getStyleClass().add("card-title");

        FontIcon fontIcon = new FontIcon(icono);
        fontIcon.setIconSize(24);
        fontIcon.setIconColor(Color.web(colorHex));

        VBox tarjeta = new VBox(6, fontIcon, tituloLabel, contenido);
        tarjeta.getStyleClass().add("card");
        tarjeta.getStyleClass().add("clickable-card");
        tarjeta.setPadding(new Insets(12));
        tarjeta.setPrefWidth(200);
        tarjeta.setMinHeight(90);
        tarjeta.setAlignment(Pos.TOP_CENTER);

        tarjeta.setOnMouseClicked(evento -> {
            if (evento.getButton() == MouseButton.PRIMARY && evento.getClickCount() == 1) {
                alHacer.run();
            }
        });

        return tarjeta;
    }

    static String construirAvisoVacunas(int idMascota) {
        ArrayList<String> vacunas = GestorMascotas.obtenerLineasVacunas(idMascota);
        int contador = 0;
        for (String linea : vacunas) {
            if (linea.contains("⚠")) contador++;
        }

        if (contador == 0) return "";
        return "⚠ " + contador + " vacuna(s) vencida(s) o próxima(s) — revisa el detalle en Gestionar vacunas";
    }

    private static String obtenerProximaVacuna(int idMascota) {
        ArrayList<String[]> vacunas = GestorMascotas.obtenerVacunasConId(idMascota);
        if (vacunas.isEmpty()) return "Sin vacunas";

        LocalDate hoy = LocalDate.now();
        LocalDate fechaMasProxima = null;
        String nombreVacuna = "";

        for (String[] v : vacunas) {
            LocalDate fechaProxima = LocalDate.parse(v[3]);
            if (fechaMasProxima == null || fechaProxima.isBefore(fechaMasProxima)) {
                fechaMasProxima = fechaProxima;
                nombreVacuna = v[1];
            }
        }

        if (fechaMasProxima == null) return "Sin vacunas";

        if (fechaMasProxima.isBefore(hoy)) {
            return nombreVacuna + "\nVencida";
        } else {
            long diasFaltantes = ChronoUnit.DAYS.between(hoy, fechaMasProxima);
            return nombreVacuna + "\nPróxima en " + diasFaltantes + " días";
        }
    }

    private static String obtenerTratamientosResumen(int idMascota) {
        ArrayList<String[]> tratamientos = GestorMascotas.obtenerTratamientosConId(idMascota);
        if (tratamientos.isEmpty()) return "Sin tratamientos";

        LocalDate hoy = LocalDate.now();
        int activos = 0;

        for (String[] t : tratamientos) {
            LocalDate inicio = LocalDate.parse(t[4]);
            if (inicio.isBefore(hoy) || inicio.isEqual(hoy)) {
                if (t[5].isEmpty()) {
                    activos++;
                } else {
                    LocalDate fin = LocalDate.parse(t[5]);
                    if (fin.isAfter(hoy) || fin.isEqual(hoy)) {
                        activos++;
                    }
                }
            }
        }

        if (activos == 0) return "Sin tratamientos";
        return activos + " activo" + (activos > 1 ? "s" : "");
    }

    private static String obtenerUltimoPesoResumen(int idMascota) {
        ArrayList<String[]> pesos = GestorMascotas.obtenerPesosConId(idMascota);
        if (pesos.isEmpty()) return "Sin registros";

        String[] pesoMasReciente = pesos.get(0);
        LocalDate fechaMasReciente = LocalDate.parse(pesoMasReciente[1]);

        for (String[] p : pesos) {
            LocalDate fecha = LocalDate.parse(p[1]);
            if (fecha.isAfter(fechaMasReciente)) {
                fechaMasReciente = fecha;
                pesoMasReciente = p;
            }
        }

        return pesoMasReciente[2] + " kg";
    }

    private static String obtenerInformesResumen(int idMascota) {
        ArrayList<String[]> informes = GestorMascotas.obtenerInformesConId(idMascota);
        if (informes.isEmpty()) return "Sin documentos";

        return informes.size() + " documento" + (informes.size() > 1 ? "s" : "");
    }

    static void mostrarAviso(AlertType tipo, String mensaje) {
        Alert alerta = new Alert(tipo, mensaje);
        alerta.showAndWait();
    }
}