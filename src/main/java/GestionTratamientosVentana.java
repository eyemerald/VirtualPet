import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.LocalDate;
import java.util.ArrayList;

public class GestionTratamientosVentana {

    public static void abrir(int idMascota, Runnable alGuardar) {
        Navegador.navegarA("Gestionar tratamientos", () -> crearVista(idMascota, alGuardar));
    }

    public static Node crearVista(int idMascota, Runnable alGuardar) {
        ArrayList<String[]> tratamientos = GestorMascotas.obtenerTratamientosConId(idMascota);

        Button botonAñadirNuevo = new Button("+ Añadir nuevo");
        FontIcon iconoAñadir = new FontIcon(Feather.PLUS_CIRCLE);
        iconoAñadir.setIconSize(16);
        botonAñadirNuevo.setGraphic(iconoAñadir);
        botonAñadirNuevo.getStyleClass().add("btn-primary");
        botonAñadirNuevo.setOnAction(e -> FormularioRegistro.abrirTratamiento(idMascota, alGuardar));

        if (tratamientos.isEmpty()) {
            VBox vacio = new VBox(14,
                    new Label("Esta mascota no tiene tratamientos registrados."),
                    botonAñadirNuevo
            );
            vacio.setPadding(new Insets(15));
            ScrollPane scrollVacio = new ScrollPane(vacio);
            scrollVacio.setFitToWidth(true);
            scrollVacio.setFitToHeight(true);
            scrollVacio.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
            return scrollVacio;
        }

        ListView<String> lista = new ListView<>();
        for (String[] t : tratamientos) {
            String textoFin = t[5].isEmpty() ? "crónico/indefinido" : "hasta " + t[5];
            lista.getItems().add(t[1] + " (" + t[2] + ") - " + textoFin);
        }

        TextField campoMedicamento = new TextField();
        TextField campoDosis = new TextField();
        TextField campoFrecuencia = new TextField();
        DatePicker campoInicio = new DatePicker();
        DatePicker campoFin = new DatePicker();
        CheckBox marcarCronico = new CheckBox("Sin fecha fin (crónico)");
        marcarCronico.setOnAction(e -> campoFin.setDisable(marcarCronico.isSelected()));

        Button botonGuardar = new Button("Guardar cambios");
        FontIcon iconoGuardar = new FontIcon(Feather.CHECK);
        iconoGuardar.setIconSize(16);
        botonGuardar.setGraphic(iconoGuardar);
        botonGuardar.setDisable(true);

        lista.getSelectionModel().selectedItemProperty().addListener((observable, valorAntiguo, valorNuevo) -> {
            int indice = lista.getSelectionModel().getSelectedIndex();
            if (indice < 0) return;

            String[] t = tratamientos.get(indice);
            campoMedicamento.setText(t[1]);
            campoDosis.setText(t[2]);
            campoFrecuencia.setText(t[3]);
            campoInicio.setValue(LocalDate.parse(t[4]));

            if (t[5].isEmpty()) {
                marcarCronico.setSelected(true);
                campoFin.setValue(null);
                campoFin.setDisable(true);
            } else {
                marcarCronico.setSelected(false);
                campoFin.setValue(LocalDate.parse(t[5]));
                campoFin.setDisable(false);
            }

            botonGuardar.setDisable(false);
        });

        botonGuardar.setOnAction(evento -> {
            int indice = lista.getSelectionModel().getSelectedIndex();
            if (indice < 0) return;

            if (campoMedicamento.getText().isBlank() || campoInicio.getValue() == null) {
                mostrarAviso("Medicamento y fecha de inicio son obligatorios.");
                return;
            }
            if (!marcarCronico.isSelected() && campoFin.getValue() != null
                    && campoFin.getValue().isBefore(campoInicio.getValue())) {
                mostrarAviso("La fecha fin no puede ser anterior a la fecha de inicio.");
                return;
            }

            int idTratamiento = Integer.parseInt(tratamientos.get(indice)[0]);
            LocalDate fechaFin = marcarCronico.isSelected() ? null : campoFin.getValue();

            botonGuardar.setDisable(true);
            new Thread(() -> {
                try {
                    GestorMascotas.actualizarTratamiento(idTratamiento, campoMedicamento.getText(),
                            campoDosis.getText(), campoFrecuencia.getText(), campoInicio.getValue(), fechaFin);
                    javafx.application.Platform.runLater(() -> {
                        if (alGuardar != null) alGuardar.run();
                        Navegador.volverAtras();
                    });
                } catch (Exception e) {
                    javafx.application.Platform.runLater(() -> mostrarAviso("Error al actualizar: " + e.getMessage()));
                    botonGuardar.setDisable(false);
                }
            }, "actualizar-tratamiento-thread").start();
        });

        GridPane camposEdicion = new GridPane();
        camposEdicion.setHgap(10);
        camposEdicion.setVgap(8);
        camposEdicion.add(new Label("Medicamento:"), 0, 0);
        camposEdicion.add(campoMedicamento, 1, 0);
        camposEdicion.add(new Label("Dosis:"), 0, 1);
        camposEdicion.add(campoDosis, 1, 1);
        camposEdicion.add(new Label("Frecuencia:"), 0, 2);
        camposEdicion.add(campoFrecuencia, 1, 2);
        camposEdicion.add(new Label("Fecha inicio:"), 0, 3);
        camposEdicion.add(campoInicio, 1, 3);
        camposEdicion.add(new Label("Fecha fin:"), 0, 4);
        camposEdicion.add(campoFin, 1, 4);
        camposEdicion.add(marcarCronico, 1, 5);

        BorderPane layout = new BorderPane();
        layout.setPadding(new Insets(15));

        Label tituloInstruccion = new Label("Selecciona el tratamiento a modificar:");
        tituloInstruccion.setPadding(new Insets(0, 0, 6, 0));
        layout.setTop(tituloInstruccion);

        VBox.setVgrow(lista, Priority.ALWAYS);
        layout.setCenter(lista);

        VBox seccionBottom = new VBox(10,
                new Label("Edita los datos y guarda:"),
                camposEdicion,
                botonGuardar,
                botonAñadirNuevo
        );
        seccionBottom.setPadding(new Insets(10, 0, 0, 0));
        layout.setBottom(seccionBottom);

        ScrollPane scroll = new ScrollPane(layout);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        return scroll;
    }

    private static void mostrarAviso(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.WARNING, mensaje);
        alerta.showAndWait();
    }
}
