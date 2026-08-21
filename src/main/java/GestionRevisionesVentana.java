import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

public class GestionRevisionesVentana {

    public static void abrir(int idMascota, Runnable alGuardar) {
        Navegador.navegarA("Gestionar revisiones", () -> crearVista(idMascota, alGuardar));
    }

    public static Node crearVista(int idMascota, Runnable alGuardar) {
        ArrayList<String[]> revisiones = GestorMascotas.obtenerRevisionesConId(idMascota);

        Button botonAñadirNuevo = new Button("Añadir nuevo");
        FontIcon iconoAñadir = new FontIcon(Feather.PLUS_CIRCLE);
        iconoAñadir.setIconSize(16);
        botonAñadirNuevo.setGraphic(iconoAñadir);
        botonAñadirNuevo.getStyleClass().add("btn-primary");
        botonAñadirNuevo.setOnAction(e -> FormularioRegistro.abrirRevision(idMascota, alGuardar));

        if (revisiones.isEmpty()) {
            VBox vacio = new VBox(14,
                    new Label("Esta mascota no tiene revisiones registradas."),
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
        for (String[] r : revisiones) {
            lista.getItems().add(UtilidadesFecha.formatearFechaES(r[1]) + " - " + r[2]);
        }

        DatePicker campoFecha = new DatePicker();
        TextField campoMotivo = new TextField();
        TextArea campoDiagnostico = new TextArea();
        campoDiagnostico.setWrapText(true);
        campoDiagnostico.setPrefRowCount(3);
        TextArea campoNotas = new TextArea();
        campoNotas.setWrapText(true);
        campoNotas.setPrefRowCount(2);
        TextField campoVeterinario = new TextField();

        Button botonGuardar = new Button("Guardar cambios");
        FontIcon iconoGuardar = new FontIcon(Feather.CHECK);
        iconoGuardar.setIconSize(16);
        botonGuardar.setGraphic(iconoGuardar);
        botonGuardar.setDisable(true);

        Button botonBorrar = new Button("Borrar revisión");
        FontIcon iconoBorrar = new FontIcon(Feather.TRASH_2);
        iconoBorrar.setIconSize(16);
        botonBorrar.setGraphic(iconoBorrar);
        botonBorrar.getStyleClass().add("btn-danger");
        botonBorrar.setDisable(true);

        lista.getSelectionModel().selectedItemProperty().addListener((observable, valorAntiguo, valorNuevo) -> {
            int indice = lista.getSelectionModel().getSelectedIndex();
            if (indice < 0) return;

            String[] r = revisiones.get(indice);
            campoFecha.setValue(LocalDate.parse(r[1]));
            campoMotivo.setText(r[2]);
            campoDiagnostico.setText(r[3]);
            campoNotas.setText(r[4]);
            campoVeterinario.setText(r[5]);

            botonGuardar.setDisable(false);
            botonBorrar.setDisable(false);
        });

        botonGuardar.setOnAction(evento -> {
            int indice = lista.getSelectionModel().getSelectedIndex();
            if (indice < 0) return;

            if (campoFecha.getValue() == null || campoMotivo.getText().isBlank()) {
                mostrarAviso("Fecha y motivo de revisión son obligatorios.");
                return;
            }

            int idRevision = Integer.parseInt(revisiones.get(indice)[0]);

            botonGuardar.setDisable(true);
            new Thread(() -> {
                try {
                    GestorMascotas.actualizarRevision(idRevision, campoFecha.getValue(),
                            campoMotivo.getText(), campoDiagnostico.getText(),
                            campoNotas.getText(), campoVeterinario.getText());
                    javafx.application.Platform.runLater(() -> {
                        if (alGuardar != null) alGuardar.run();
                        Navegador.volverAtras();
                    });
                } catch (Exception e) {
                    javafx.application.Platform.runLater(() -> mostrarAviso("Error al actualizar: " + e.getMessage()));
                    botonGuardar.setDisable(false);
                }
            }, "actualizar-revision-thread").start();
        });

        botonBorrar.setOnAction(evento -> {
            int indice = lista.getSelectionModel().getSelectedIndex();
            if (indice < 0) return;

            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION,
                    "¿Seguro que quieres borrar esta revisión?",
                    ButtonType.YES, ButtonType.NO);
            Optional<ButtonType> respuesta = confirmacion.showAndWait();

            if (respuesta.isPresent() && respuesta.get() == ButtonType.YES) {
                int idRevision = Integer.parseInt(revisiones.get(indice)[0]);
                botonBorrar.setDisable(true);
                new Thread(() -> {
                    try {
                        GestorMascotas.borrarRevision(idRevision);
                        javafx.application.Platform.runLater(() -> {
                            if (alGuardar != null) alGuardar.run();
                            Navegador.volverAtras();
                        });
                    } catch (Exception e) {
                        javafx.application.Platform.runLater(() -> mostrarAviso("Error al borrar: " + e.getMessage()));
                        botonBorrar.setDisable(false);
                    }
                }, "borrar-revision-thread").start();
            }
        });

        GridPane camposEdicion = new GridPane();
        camposEdicion.setHgap(10);
        camposEdicion.setVgap(8);
        camposEdicion.add(new Label("Fecha:"), 0, 0);
        camposEdicion.add(campoFecha, 1, 0);
        camposEdicion.add(new Label("Motivo:"), 0, 1);
        camposEdicion.add(campoMotivo, 1, 1);
        camposEdicion.add(new Label("Diagnóstico:"), 0, 2);
        camposEdicion.add(campoDiagnostico, 1, 2);
        camposEdicion.add(new Label("Notas:"), 0, 3);
        camposEdicion.add(campoNotas, 1, 3);
        camposEdicion.add(new Label("Veterinario:"), 0, 4);
        camposEdicion.add(campoVeterinario, 1, 4);

        HBox botones = new HBox(10, botonGuardar, botonBorrar);

        BorderPane layout = new BorderPane();
        layout.setPadding(new Insets(15));

        Label tituloInstruccion = new Label("Selecciona la revisión a modificar:");
        tituloInstruccion.setPadding(new Insets(0, 0, 6, 0));
        layout.setTop(tituloInstruccion);

        VBox.setVgrow(lista, Priority.ALWAYS);
        layout.setCenter(lista);

        VBox seccionBottom = new VBox(10,
                new Label("Edita los datos y guarda:"),
                camposEdicion,
                botones,
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
