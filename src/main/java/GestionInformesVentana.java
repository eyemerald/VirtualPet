import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

public class GestionInformesVentana {

    public static void abrir(int idMascota, Runnable alGuardar) {
        Navegador.navegarA("Gestionar informes", () -> crearVista(idMascota, alGuardar));
    }

    public static Node crearVista(int idMascota, Runnable alGuardar) {
        ArrayList<String[]> informes = GestorMascotas.obtenerInformesConId(idMascota);

        ListView<String> lista = new ListView<>();
        for (String[] i : informes) {
            lista.getItems().add(i[1] + " - " + i[3] + " (" + i[2] + ")");
        }

        TextField campoTipo = new TextField();
        TextField campoDescripcion = new TextField();
        DatePicker campoFecha = new DatePicker();
        TextField campoArchivo = new TextField();
        campoArchivo.setEditable(false);

        Button botonSeleccionar = new Button("Seleccionar archivo");
        FontIcon iconoFolder = new FontIcon(Feather.FOLDER);
        iconoFolder.setIconSize(16);
        botonSeleccionar.setGraphic(iconoFolder);

        Button botonGuardar = new Button("+ Añadir nuevo informe");
        FontIcon iconoAñadir = new FontIcon(Feather.PLUS_CIRCLE);
        iconoAñadir.setIconSize(16);
        botonGuardar.setGraphic(iconoAñadir);
        botonGuardar.getStyleClass().add("btn-primary");
        botonGuardar.setDisable(false);

        Button botonBorrar = new Button("Borrar");
        FontIcon iconoBorrar = new FontIcon(Feather.TRASH_2);
        iconoBorrar.setIconSize(16);
        botonBorrar.setGraphic(iconoBorrar);
        botonBorrar.getStyleClass().add("btn-danger");
        botonBorrar.setDisable(true);

        Button botonAbrir = new Button("Abrir documento");
        FontIcon iconoAbrir = new FontIcon(Feather.EXTERNAL_LINK);
        iconoAbrir.setIconSize(16);
        botonAbrir.setGraphic(iconoAbrir);
        botonAbrir.setDisable(true);

        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Documentos y fotos", "*.pdf", "*.jpg", "*.jpeg", "*.png")
        );

        final File[] seleccionado = new File[1];

        botonSeleccionar.setOnAction(e -> {
            File f = chooser.showOpenDialog(Navegador.getEscenario());
            if (f != null) {
                seleccionado[0] = f;
                campoArchivo.setText(f.getName());
            }
        });

        lista.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            int idx = lista.getSelectionModel().getSelectedIndex();
            if (idx < 0) return;
            String[] datos = informes.get(idx);
            campoTipo.setText(datos[1]);
            campoDescripcion.setText(datos[2]);
            if (datos[3] != null && !datos[3].isEmpty()) campoFecha.setValue(LocalDate.parse(datos[3]));
            campoArchivo.setText(datos[4]);
            botonBorrar.setDisable(false);
            botonAbrir.setDisable(false);
        });

        lista.setOnMouseClicked(evento -> {
            if (evento.getClickCount() == 2) {
                int idx = lista.getSelectionModel().getSelectedIndex();
                if (idx >= 0) abrirArchivo(informes.get(idx));
            }
        });

        botonAbrir.setOnAction(ev -> {
            int idx = lista.getSelectionModel().getSelectedIndex();
            if (idx < 0) return;
            abrirArchivo(informes.get(idx));
        });

        botonGuardar.setOnAction(ev -> {
            if (campoTipo.getText().isBlank()) {
                mostrarAviso("Tipo es obligatorio.");
                return;
            }
            if (campoFecha.getValue() == null) {
                mostrarAviso("Fecha es obligatoria.");
                return;
            }
            if (seleccionado[0] == null) {
                mostrarAviso("Selecciona un archivo.");
                return;
            }

            botonGuardar.setDisable(true);
            new Thread(() -> {
                GestorMascotas.guardarInforme(idMascota, campoTipo.getText(), campoDescripcion.getText(), campoFecha.getValue().toString(), seleccionado[0]);
                javafx.application.Platform.runLater(() -> {
                    if (alGuardar != null) alGuardar.run();
                    Navegador.volverAtras();
                });
            }, "guardar-informe-thread").start();
        });

        botonBorrar.setOnAction(ev -> {
            int idx = lista.getSelectionModel().getSelectedIndex();
            if (idx < 0) return;
            String[] datos = informes.get(idx);
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "¿Borrar este informe?", ButtonType.YES, ButtonType.NO);
            Optional<ButtonType> res = confirm.showAndWait();
            if (res.isPresent() && res.get() == ButtonType.YES) {
                GestorMascotas.borrarInforme(Integer.parseInt(datos[0]));
                if (alGuardar != null) alGuardar.run();
                Navegador.volverAtras();
            }
        });

        Button botonLimpiar = new Button("Limpiar formulario");
        FontIcon iconoLimpiar = new FontIcon(Feather.ROTATE_CCW);
        iconoLimpiar.setIconSize(16);
        botonLimpiar.setGraphic(iconoLimpiar);
        botonLimpiar.setOnAction(e -> {
            campoTipo.clear();
            campoDescripcion.clear();
            campoFecha.setValue(null);
            campoArchivo.clear();
            seleccionado[0] = null;
            lista.getSelectionModel().clearSelection();
            botonBorrar.setDisable(true);
            botonAbrir.setDisable(true);
        });

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(10));

        grid.add(new Label("Tipo:"), 0, 0);
        grid.add(campoTipo, 1, 0);
        grid.add(new Label("Descripción:"), 0, 1);
        grid.add(campoDescripcion, 1, 1);
        grid.add(new Label("Fecha:"), 0, 2);
        grid.add(campoFecha, 1, 2);
        grid.add(new Label("Archivo:"), 0, 3);
        grid.add(campoArchivo, 1, 3);
        grid.add(botonSeleccionar, 1, 4);
        grid.add(botonGuardar, 1, 5);
        grid.add(botonAbrir, 1, 6);
        grid.add(botonBorrar, 1, 7);
        grid.add(botonLimpiar, 1, 8);

        VBox contenidoFormulario = new VBox(10,
                new Label("Añadir o editar informe:"),
                grid
        );
        contenidoFormulario.setPadding(new Insets(10, 0, 0, 0));

        BorderPane layout = new BorderPane();
        layout.setPadding(new Insets(15));

        Label tituloInstruccion = new Label("Documentos e informes asociados:");
        tituloInstruccion.setPadding(new Insets(0, 0, 6, 0));
        layout.setTop(tituloInstruccion);

        VBox.setVgrow(lista, Priority.ALWAYS);
        layout.setCenter(lista);

        layout.setBottom(contenidoFormulario);

        ScrollPane scroll = new ScrollPane(layout);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        return scroll;
    }

    private static void abrirArchivo(String[] datos) {
        if (datos.length < 6 || datos[5] == null || datos[5].isBlank()) {
            mostrarAviso("No se encontró la ruta del archivo para este informe.");
            return;
        }

        File archivo = new File(datos[5]);
        if (!archivo.exists()) {
            mostrarAviso("El archivo ya no existe en disco:\n" + archivo.getAbsolutePath());
            return;
        }

        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                Desktop.getDesktop().open(archivo);
            } else {
                mostrarAviso("Este sistema no permite abrir archivos automáticamente.");
            }
        } catch (IOException e) {
            mostrarAviso("No se pudo abrir el archivo: " + e.getMessage());
        }
    }

    static void mostrarAviso(String m) {
        Alert a = new Alert(Alert.AlertType.WARNING, m);
        a.showAndWait();
    }
}
