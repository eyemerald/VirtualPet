import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
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
import javafx.util.StringConverter;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Optional;

public class GestionPesosVentana {

    public static void abrir(int idMascota, Runnable alGuardar) {
        Navegador.navegarA("Gestionar pesos", () -> crearVista(idMascota, alGuardar));
    }

    public static Node crearVista(int idMascota, Runnable alGuardar) {
        ArrayList<String[]> pesos = GestorMascotas.obtenerPesosConId(idMascota);

        Button botonAñadirNuevo = new Button("Añadir nuevo");
        FontIcon iconoAñadir = new FontIcon(Feather.PLUS_CIRCLE);
        iconoAñadir.setIconSize(16);
        botonAñadirNuevo.setGraphic(iconoAñadir);
        botonAñadirNuevo.getStyleClass().add("btn-primary");
        botonAñadirNuevo.setOnAction(e -> FormularioRegistro.abrirPeso(idMascota, alGuardar));

        if (pesos.isEmpty()) {
            VBox vacio = new VBox(14,
                    new Label("Esta mascota no tiene registros de peso."),
                    botonAñadirNuevo
            );
            vacio.setPadding(new Insets(15));
            ScrollPane scrollVacio = new ScrollPane(vacio);
            scrollVacio.setFitToWidth(true);
            scrollVacio.setFitToHeight(true);
            scrollVacio.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
            return scrollVacio;
        }

        LineChart<Number, Number> grafico = crearGraficoPeso(pesos);

        ListView<String> lista = new ListView<>();
        for (String[] p : pesos) {
            lista.getItems().add(p[1] + ": " + p[2] + " kg");
        }

        DatePicker campoFecha = new DatePicker();
        TextField campoPeso = new TextField();

        // Notas: solo se espera algo corto ("pesada en casa", "en el veterinario"...),
        // no un texto largo — reducido de 3 a 1 línea visible para dejar más
        // espacio real al gráfico y a la lista, que es lo importante aquí.
        TextArea campoNotas = new TextArea();
        campoNotas.setWrapText(true);
        campoNotas.setPrefRowCount(1);
        campoNotas.setPromptText("Ej: pesada en casa");
        campoNotas.setPrefWidth(200);
        campoNotas.setMaxWidth(200);

        Button botonGuardar = new Button("Guardar cambios");
        FontIcon iconoGuardar = new FontIcon(Feather.CHECK);
        iconoGuardar.setIconSize(16);
        botonGuardar.setGraphic(iconoGuardar);
        botonGuardar.setDisable(true);

        Button botonBorrar = new Button("Borrar registro");
        FontIcon iconoBorrar = new FontIcon(Feather.TRASH_2);
        iconoBorrar.setIconSize(16);
        botonBorrar.setGraphic(iconoBorrar);
        botonBorrar.getStyleClass().add("btn-danger");
        botonBorrar.setDisable(true);

        lista.getSelectionModel().selectedItemProperty().addListener((observable, valorAntiguo, valorNuevo) -> {
            int indice = lista.getSelectionModel().getSelectedIndex();
            if (indice < 0) return;

            String[] p = pesos.get(indice);
            campoFecha.setValue(LocalDate.parse(p[1]));
            campoPeso.setText(p[2]);
            campoNotas.setText(p[3]);

            botonGuardar.setDisable(false);
            botonBorrar.setDisable(false);
        });

        botonGuardar.setOnAction(evento -> {
            int indice = lista.getSelectionModel().getSelectedIndex();
            if (indice < 0) return;

            if (campoFecha.getValue() == null || campoPeso.getText().isBlank()) {
                mostrarAviso("Fecha y peso son obligatorios.");
                return;
            }

            double peso;
            try {
                peso = Double.parseDouble(campoPeso.getText());
                if (peso <= 0) {
                    mostrarAviso("El peso debe ser un número positivo.");
                    return;
                }
            } catch (NumberFormatException e) {
                mostrarAviso("El peso debe ser un número válido.");
                return;
            }

            int idPeso = Integer.parseInt(pesos.get(indice)[0]);

            botonGuardar.setDisable(true);
            new Thread(() -> {
                try {
                    GestorMascotas.actualizarPeso(idPeso, campoFecha.getValue(), peso, campoNotas.getText());
                    javafx.application.Platform.runLater(() -> {
                        if (alGuardar != null) alGuardar.run();
                        Navegador.volverAtras();
                    });
                } catch (Exception e) {
                    javafx.application.Platform.runLater(() -> mostrarAviso("Error al actualizar: " + e.getMessage()));
                    botonGuardar.setDisable(false);
                }
            }, "actualizar-peso-thread").start();
        });

        botonBorrar.setOnAction(evento -> {
            int indice = lista.getSelectionModel().getSelectedIndex();
            if (indice < 0) return;

            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION,
                    "¿Seguro que quieres borrar este registro de peso?",
                    ButtonType.YES, ButtonType.NO);
            Optional<ButtonType> respuesta = confirmacion.showAndWait();

            if (respuesta.isPresent() && respuesta.get() == ButtonType.YES) {
                int idPeso = Integer.parseInt(pesos.get(indice)[0]);
                botonBorrar.setDisable(true);
                new Thread(() -> {
                    try {
                        GestorMascotas.borrarPeso(idPeso);
                        javafx.application.Platform.runLater(() -> {
                            if (alGuardar != null) alGuardar.run();
                            Navegador.volverAtras();
                        });
                    } catch (Exception e) {
                        javafx.application.Platform.runLater(() -> mostrarAviso("Error al borrar: " + e.getMessage()));
                        botonBorrar.setDisable(false);
                    }
                }, "borrar-peso-thread").start();
            }
        });

        GridPane camposEdicion = new GridPane();
        camposEdicion.setHgap(10);
        camposEdicion.setVgap(8);
        camposEdicion.add(new Label("Fecha:"), 0, 0);
        camposEdicion.add(campoFecha, 1, 0);
        camposEdicion.add(new Label("Peso (kg):"), 0, 1);
        camposEdicion.add(campoPeso, 1, 1);
        camposEdicion.add(new Label("Notas:"), 0, 2);
        camposEdicion.add(campoNotas, 1, 2);

        HBox botones = new HBox(10, botonGuardar, botonBorrar);

        BorderPane layout = new BorderPane();
        layout.setPadding(new Insets(15));

        VBox.setVgrow(lista, Priority.ALWAYS);
        VBox centroContenido = new VBox(8,
                grafico,
                new Label("Selecciona el registro a modificar:"),
                lista
        );
        layout.setCenter(centroContenido);

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

    private static LineChart<Number, Number> crearGraficoPeso(ArrayList<String[]> pesos) {
        ArrayList<String[]> ordenados = new ArrayList<>(pesos);
        ordenados.sort(Comparator.comparing(p -> LocalDate.parse(p[1])));

        LocalDate fechaBase = LocalDate.parse(ordenados.get(0)[1]);

        NumberAxis ejeX = new NumberAxis();
        ejeX.setLabel("Fecha");
        ejeX.setForceZeroInRange(false);
        ejeX.setTickLabelFormatter(new StringConverter<Number>() {
            final DateTimeFormatter formato = DateTimeFormatter.ofPattern("MMM");

            @Override
            public String toString(Number dias) {
                LocalDate fecha = fechaBase.plusDays(dias.longValue());
                return formato.format(fecha).toUpperCase();
            }

            @Override
            public Number fromString(String string) {
                return 0;
            }
        });

        NumberAxis ejeY = new NumberAxis();
        ejeY.setLabel("Peso (kg)");
        ejeY.setForceZeroInRange(false);

        LineChart<Number, Number> grafico = new LineChart<>(ejeX, ejeY);
        grafico.setTitle("Evolución de peso");
        grafico.setLegendVisible(false);
        grafico.setPrefHeight(220);
        grafico.setCreateSymbols(true);
        grafico.setAnimated(false);

        XYChart.Series<Number, Number> serie = new XYChart.Series<>();
        for (String[] p : ordenados) {
            LocalDate fecha = LocalDate.parse(p[1]);
            long dias = ChronoUnit.DAYS.between(fechaBase, fecha);
            double peso = Double.parseDouble(p[2]);

            XYChart.Data<Number, Number> punto = new XYChart.Data<>(dias, peso);
            serie.getData().add(punto);
        }
        grafico.getData().add(serie);

        for (XYChart.Data<Number, Number> punto : serie.getData()) {
            javafx.scene.Node nodo = punto.getNode();
            if (nodo != null) {
                LocalDate fechaPunto = fechaBase.plusDays(punto.getXValue().longValue());
                javafx.scene.control.Tooltip.install(nodo, new javafx.scene.control.Tooltip(
                        fechaPunto.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + "  —  " + punto.getYValue() + " kg"
                ));
            }
        }

        return grafico;
    }

    private static void mostrarAviso(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.WARNING, mensaje);
        alerta.showAndWait();
    }
}
