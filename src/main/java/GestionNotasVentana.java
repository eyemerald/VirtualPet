import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.ArrayList;
import java.util.Optional;

public class GestionNotasVentana {

    public static void abrir(int idMascota, Runnable alGuardar) {
        Navegador.navegarA("A tener en cuenta", () -> crearVista(idMascota, alGuardar));
    }

    public static Node crearVista(int idMascota, Runnable alGuardar) {
        ArrayList<String[]> notas = GestorMascotas.obtenerNotasConId(idMascota);

        ListView<String> lista = new ListView<>();
        for (String[] n : notas) {
            // n = [id, texto]
            lista.getItems().add(n[1]);
        }

        TextArea campoTexto = new TextArea();
        campoTexto.setWrapText(true);
        campoTexto.setPrefRowCount(3);
        campoTexto.setPromptText("Ej: es agresiva con otros perros machos");

        Button botonGuardar = new Button("Guardar cambios");
        FontIcon iconoGuardar = new FontIcon(Feather.CHECK);
        iconoGuardar.setIconSize(16);
        botonGuardar.setGraphic(iconoGuardar);
        botonGuardar.setDisable(true);

        Button botonBorrar = new Button("Borrar nota");
        FontIcon iconoBorrar = new FontIcon(Feather.TRASH_2);
        iconoBorrar.setIconSize(16);
        botonBorrar.setGraphic(iconoBorrar);
        botonBorrar.getStyleClass().add("btn-danger");
        botonBorrar.setDisable(true);

        Button botonAñadirNuevo = new Button("Añadir nota");
        FontIcon iconoAñadir = new FontIcon(Feather.PLUS_CIRCLE);
        iconoAñadir.setIconSize(16);
        botonAñadirNuevo.setGraphic(iconoAñadir);
        botonAñadirNuevo.getStyleClass().add("btn-primary");

        lista.getSelectionModel().selectedItemProperty().addListener((observable, valorAntiguo, valorNuevo) -> {
            int indice = lista.getSelectionModel().getSelectedIndex();
            if (indice < 0) return;

            String[] n = notas.get(indice);
            campoTexto.setText(n[1]);

            botonGuardar.setDisable(false);
            botonBorrar.setDisable(false);
        });

        // Guardar cambios: edita la nota actualmente seleccionada de la lista
        botonGuardar.setOnAction(evento -> {
            int indice = lista.getSelectionModel().getSelectedIndex();
            if (indice < 0) return;

            if (campoTexto.getText().isBlank()) {
                mostrarAviso("El texto no puede estar vacío.");
                return;
            }

            int idNota = Integer.parseInt(notas.get(indice)[0]);

            botonGuardar.setDisable(true);
            new Thread(() -> {
                try {
                    GestorMascotas.actualizarNota(idNota, campoTexto.getText());
                    javafx.application.Platform.runLater(() -> {
                        if (alGuardar != null) alGuardar.run();
                        Navegador.volverAtras();
                    });
                } catch (Exception e) {
                    javafx.application.Platform.runLater(() -> mostrarAviso("Error al actualizar: " + e.getMessage()));
                    botonGuardar.setDisable(false);
                }
            }, "actualizar-nota-thread").start();
        });

        // Añadir nota: crea una nueva, no depende de tener nada seleccionado
        botonAñadirNuevo.setOnAction(evento -> {
            if (campoTexto.getText().isBlank()) {
                mostrarAviso("Escribe el texto de la nota antes de añadirla.");
                return;
            }

            botonAñadirNuevo.setDisable(true);
            new Thread(() -> {
                try {
                    GestorMascotas.guardarNota(idMascota, campoTexto.getText());
                    javafx.application.Platform.runLater(() -> {
                        if (alGuardar != null) alGuardar.run();
                        Navegador.volverAtras();
                    });
                } catch (Exception e) {
                    javafx.application.Platform.runLater(() -> mostrarAviso("Error al guardar: " + e.getMessage()));
                    botonAñadirNuevo.setDisable(false);
                }
            }, "guardar-nota-thread").start();
        });

        botonBorrar.setOnAction(evento -> {
            int indice = lista.getSelectionModel().getSelectedIndex();
            if (indice < 0) return;

            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION,
                    "¿Seguro que quieres borrar esta nota?",
                    ButtonType.YES, ButtonType.NO);
            Optional<ButtonType> respuesta = confirmacion.showAndWait();

            if (respuesta.isPresent() && respuesta.get() == ButtonType.YES) {
                int idNota = Integer.parseInt(notas.get(indice)[0]);
                botonBorrar.setDisable(true);
                new Thread(() -> {
                    try {
                        GestorMascotas.borrarNota(idNota);
                        javafx.application.Platform.runLater(() -> {
                            if (alGuardar != null) alGuardar.run();
                            Navegador.volverAtras();
                        });
                    } catch (Exception e) {
                        javafx.application.Platform.runLater(() -> mostrarAviso("Error al borrar: " + e.getMessage()));
                        botonBorrar.setDisable(false);
                    }
                }, "borrar-nota-thread").start();
            }
        });

        HBox botones = new HBox(10, botonGuardar, botonBorrar, botonAñadirNuevo);

        BorderPane layout = new BorderPane();
        layout.setPadding(new Insets(15));

        Label tituloInstruccion = new Label("Selecciona una nota para editarla, o escribe una nueva abajo:");
        tituloInstruccion.setWrapText(true);
        tituloInstruccion.setPadding(new Insets(0, 0, 6, 0));
        layout.setTop(tituloInstruccion);

        VBox.setVgrow(lista, Priority.ALWAYS);
        layout.setCenter(lista);

        VBox seccionBottom = new VBox(10, campoTexto, botones);
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
