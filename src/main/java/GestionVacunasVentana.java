import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
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

public class GestionVacunasVentana {

    public static void abrir(int idMascota, Runnable alGuardar) {
        Navegador.navegarA("Gestionar vacunas", () -> crearVista(idMascota, alGuardar));
    }

    public static Node crearVista(int idMascota, Runnable alGuardar) {
        ArrayList<String[]> vacunas = GestorMascotas.obtenerVacunasConId(idMascota);

        Button botonAñadirNuevo = new Button("Añadir nuevo");
        FontIcon iconoAñadir = new FontIcon(Feather.PLUS_CIRCLE);
        iconoAñadir.setIconSize(16);
        botonAñadirNuevo.setGraphic(iconoAñadir);
        botonAñadirNuevo.getStyleClass().add("btn-primary");
        botonAñadirNuevo.setOnAction(e -> FormularioRegistro.abrirVacuna(idMascota, alGuardar));

        if (vacunas.isEmpty()) {
            VBox vacio = new VBox(14,
                    new Label("Esta mascota no tiene vacunas registradas."),
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
        for (String[] v : vacunas) {
            lista.getItems().add(v[1] + " (" + UtilidadesFecha.formatearFechaES(v[2]) + ") - Próxima: " + UtilidadesFecha.formatearFechaES(v[3]));
        }

        // Color según la fecha límite de la vacuna (Vencida: Rojo | Próxima <=30 días: Naranja)
        lista.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    try {
                        String fechaStr = item.substring(item.lastIndexOf("Próxima: ") + 9).trim();
                        LocalDate proxima = LocalDate.parse(fechaStr, java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                        LocalDate hoy = LocalDate.now();

                        if (proxima.isBefore(hoy)) {
                            // Vencida -> Rojo / Terracota
                            setStyle("-fx-text-fill: #D9534F; -fx-font-weight: bold;");
                        } else if (!proxima.isAfter(hoy.plusDays(30))) {
                            // Próxima en 30 días o menos -> Ámbar / Naranja
                            setStyle("-fx-text-fill: #E67E22; -fx-font-weight: bold;");
                        } else {
                            setStyle("");
                        }
                    } catch (Exception e) {
                        setStyle("");
                    }
                }
            }
        });

        TextField campoNombre = new TextField();
        DatePicker campoFechaAplicacion = new DatePicker();
        DatePicker campoFechaProxima = new DatePicker();
        TextField campoVeterinario = new TextField();
        TextField campoLote = new TextField();

        Button botonGuardar = new Button("Guardar cambios");
        FontIcon iconoGuardar = new FontIcon(Feather.CHECK);
        iconoGuardar.setIconSize(16);
        botonGuardar.setGraphic(iconoGuardar);
        botonGuardar.setDisable(true);

        Button botonBorrar = new Button("Borrar vacuna");
        FontIcon iconoBorrar = new FontIcon(Feather.TRASH_2);
        iconoBorrar.setIconSize(16);
        botonBorrar.setGraphic(iconoBorrar);
        botonBorrar.getStyleClass().add("btn-danger");
        botonBorrar.setDisable(true);

        lista.getSelectionModel().selectedItemProperty().addListener((observable, valorAntiguo, valorNuevo) -> {
            int indice = lista.getSelectionModel().getSelectedIndex();
            if (indice < 0) return;

            String[] v = vacunas.get(indice);
            campoNombre.setText(v[1]);
            campoFechaAplicacion.setValue(LocalDate.parse(v[2]));
            campoFechaProxima.setValue(LocalDate.parse(v[3]));
            campoVeterinario.setText(v[4]);
            campoLote.setText(v[5]);

            botonGuardar.setDisable(false);
            botonBorrar.setDisable(false);
        });

        botonGuardar.setOnAction(evento -> {
            int indice = lista.getSelectionModel().getSelectedIndex();
            if (indice < 0) return;

            if (campoNombre.getText().isBlank() || campoFechaAplicacion.getValue() == null
                    || campoFechaProxima.getValue() == null) {
                mostrarAviso("Nombre, fecha de aplicación y próxima dosis son obligatorios.");
                return;
            }

            int idVacuna = Integer.parseInt(vacunas.get(indice)[0]);

            botonGuardar.setDisable(true);
            new Thread(() -> {
                try {
                    GestorMascotas.actualizarVacuna(idVacuna, campoNombre.getText(),
                            campoFechaAplicacion.getValue(), campoFechaProxima.getValue(),
                            campoVeterinario.getText(), campoLote.getText());
                    javafx.application.Platform.runLater(() -> {
                        if (alGuardar != null) alGuardar.run();
                        Navegador.volverAtras();
                    });
                } catch (Exception e) {
                    javafx.application.Platform.runLater(() -> mostrarAviso("Error al actualizar: " + e.getMessage()));
                    botonGuardar.setDisable(false);
                }
            }, "actualizar-vacuna-thread").start();
        });

        botonBorrar.setOnAction(evento -> {
            int indice = lista.getSelectionModel().getSelectedIndex();
            if (indice < 0) return;

            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION,
                    "¿Seguro que quieres borrar esta vacuna?",
                    ButtonType.YES, ButtonType.NO);
            Optional<ButtonType> respuesta = confirmacion.showAndWait();

            if (respuesta.isPresent() && respuesta.get() == ButtonType.YES) {
                int idVacuna = Integer.parseInt(vacunas.get(indice)[0]);
                botonBorrar.setDisable(true);
                new Thread(() -> {
                    try {
                        GestorMascotas.borrarVacuna(idVacuna);
                        javafx.application.Platform.runLater(() -> {
                            if (alGuardar != null) alGuardar.run();
                            Navegador.volverAtras();
                        });
                    } catch (Exception e) {
                        javafx.application.Platform.runLater(() -> mostrarAviso("Error al borrar: " + e.getMessage()));
                        botonBorrar.setDisable(false);
                    }
                }, "borrar-vacuna-thread").start();
            }
        });

        GridPane camposEdicion = new GridPane();
        camposEdicion.setHgap(10);
        camposEdicion.setVgap(8);
        camposEdicion.add(new Label("Nombre:"), 0, 0);
        camposEdicion.add(campoNombre, 1, 0);
        camposEdicion.add(new Label("Fecha aplicación:"), 0, 1);
        camposEdicion.add(campoFechaAplicacion, 1, 1);
        camposEdicion.add(new Label("Próxima dosis:"), 0, 2);
        camposEdicion.add(campoFechaProxima, 1, 2);
        camposEdicion.add(new Label("Veterinario:"), 0, 3);
        camposEdicion.add(campoVeterinario, 1, 3);
        camposEdicion.add(new Label("Lote:"), 0, 4);
        camposEdicion.add(campoLote, 1, 4);

        HBox botones = new HBox(10, botonGuardar, botonBorrar);

        BorderPane layout = new BorderPane();
        layout.setPadding(new Insets(15));

        Label tituloInstruccion = new Label("Selecciona la vacuna a modificar:");
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