import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

public class FormularioRegistro {

    public static void abrirVacuna(int idMascota, Runnable alGuardar) {
        Navegador.navegarA("Añadir vacuna", () -> crearVistaVacuna(idMascota, alGuardar));
    }

    public static Node crearVistaVacuna(int idMascota, Runnable alGuardar) {
        TextField campoNombre = new TextField();
        DatePicker campoFechaAplicacion = new DatePicker();
        DatePicker campoFechaProxima = new DatePicker();
        TextField campoVeterinario = new TextField();
        TextField campoLote = new TextField();
        Button botonGuardar = new Button("Guardar");
        FontIcon iconoGuardar = new FontIcon(Feather.CHECK);
        iconoGuardar.setIconSize(16);
        botonGuardar.setGraphic(iconoGuardar);
        botonGuardar.getStyleClass().add("btn-primary");

        GridPane grid = crearGridBase();
        grid.add(new Label("Nombre:"), 0, 0);
        grid.add(campoNombre, 1, 0);
        grid.add(new Label("Fecha aplicación:"), 0, 1);
        grid.add(campoFechaAplicacion, 1, 1);
        grid.add(new Label("Próxima dosis:"), 0, 2);
        grid.add(campoFechaProxima, 1, 2);
        grid.add(new Label("Veterinario:"), 0, 3);
        grid.add(campoVeterinario, 1, 3);
        grid.add(new Label("Lote:"), 0, 4);
        grid.add(campoLote, 1, 4);
        grid.add(botonGuardar, 1, 5);

        botonGuardar.setOnAction(evento -> {
            if (campoNombre.getText().isBlank() || campoFechaAplicacion.getValue() == null
                    || campoFechaProxima.getValue() == null) {
                mostrarError("Nombre y ambas fechas son obligatorios.");
                return;
            }

            Vacuna v = new Vacuna(campoNombre.getText(), campoFechaAplicacion.getValue(),
                    campoFechaProxima.getValue(), campoVeterinario.getText(), campoLote.getText());
            botonGuardar.setDisable(true);
            new Thread(() -> {
                try {
                    GestorMascotas.guardarVacuna(idMascota, v);
                    javafx.application.Platform.runLater(() -> {
                        if (alGuardar != null) alGuardar.run();
                        Navegador.volverAtras();
                    });
                } catch (Exception e) {
                    javafx.application.Platform.runLater(() -> mostrarError("Error al guardar: " + e.getMessage()));
                    botonGuardar.setDisable(false);
                }
            }, "guardar-vacuna-thread").start();
        });

        return crearScroll(grid);
    }

    public static void abrirRevision(int idMascota, Runnable alGuardar) {
        Navegador.navegarA("Añadir revisión", () -> crearVistaRevision(idMascota, alGuardar));
    }

    public static Node crearVistaRevision(int idMascota, Runnable alGuardar) {
        DatePicker campoFecha = new DatePicker();
        TextField campoMotivo = new TextField();
        TextField campoDiagnostico = new TextField();
        TextField campoNotas = new TextField();
        TextField campoVeterinario = new TextField();
        Button botonGuardar = new Button("Guardar");
        FontIcon iconoGuardar = new FontIcon(Feather.CHECK);
        iconoGuardar.setIconSize(16);
        botonGuardar.setGraphic(iconoGuardar);
        botonGuardar.getStyleClass().add("btn-primary");

        GridPane grid = crearGridBase();
        grid.add(new Label("Fecha:"), 0, 0);
        grid.add(campoFecha, 1, 0);
        grid.add(new Label("Motivo:"), 0, 1);
        grid.add(campoMotivo, 1, 1);
        grid.add(new Label("Diagnóstico:"), 0, 2);
        grid.add(campoDiagnostico, 1, 2);
        grid.add(new Label("Notas:"), 0, 3);
        grid.add(campoNotas, 1, 3);
        grid.add(new Label("Veterinario:"), 0, 4);
        grid.add(campoVeterinario, 1, 4);
        grid.add(botonGuardar, 1, 5);

        botonGuardar.setOnAction(evento -> {
            if (campoFecha.getValue() == null || campoMotivo.getText().isBlank()) {
                mostrarError("Fecha y motivo son obligatorios.");
                return;
            }

            Revision r = new Revision(campoFecha.getValue(), campoMotivo.getText(),
                    campoDiagnostico.getText(), campoNotas.getText(), campoVeterinario.getText());
            botonGuardar.setDisable(true);
            new Thread(() -> {
                try {
                    GestorMascotas.guardarRevision(idMascota, r);
                    javafx.application.Platform.runLater(() -> {
                        if (alGuardar != null) alGuardar.run();
                        Navegador.volverAtras();
                    });
                } catch (Exception e) {
                    javafx.application.Platform.runLater(() -> mostrarError("Error al guardar: " + e.getMessage()));
                    botonGuardar.setDisable(false);
                }
            }, "guardar-revision-thread").start();
        });

        return crearScroll(grid);
    }

    public static void abrirTratamiento(int idMascota, Runnable alGuardar) {
        Navegador.navegarA("Añadir tratamiento", () -> crearVistaTratamiento(idMascota, alGuardar));
    }

    public static Node crearVistaTratamiento(int idMascota, Runnable alGuardar) {
        TextField campoMedicamento = new TextField();
        TextField campoDosis = new TextField();
        TextField campoFrecuencia = new TextField();
        DatePicker campoInicio = new DatePicker();
        DatePicker campoFin = new DatePicker();
        Label avisoFin = new Label("Déjalo vacío si es un tratamiento crónico/indefinido");
        avisoFin.setStyle("-fx-font-size: 10px; -fx-text-fill: gray;");
        Button botonGuardar = new Button("Guardar");
        FontIcon iconoGuardar = new FontIcon(Feather.CHECK);
        iconoGuardar.setIconSize(16);
        botonGuardar.setGraphic(iconoGuardar);
        botonGuardar.getStyleClass().add("btn-primary");

        GridPane grid = crearGridBase();
        grid.add(new Label("Medicamento:"), 0, 0);
        grid.add(campoMedicamento, 1, 0);
        grid.add(new Label("Dosis:"), 0, 1);
        grid.add(campoDosis, 1, 1);
        grid.add(new Label("Frecuencia:"), 0, 2);
        grid.add(campoFrecuencia, 1, 2);
        grid.add(new Label("Fecha inicio:"), 0, 3);
        grid.add(campoInicio, 1, 3);
        grid.add(new Label("Fecha fin:"), 0, 4);
        grid.add(campoFin, 1, 4);
        grid.add(avisoFin, 1, 5);
        grid.add(botonGuardar, 1, 6);

        botonGuardar.setOnAction(evento -> {
            if (campoMedicamento.getText().isBlank() || campoInicio.getValue() == null) {
                mostrarError("Medicamento y fecha de inicio son obligatorios.");
                return;
            }
            if (campoFin.getValue() != null && campoFin.getValue().isBefore(campoInicio.getValue())) {
                mostrarError("La fecha fin no puede ser anterior a la fecha de inicio.");
                return;
            }

            Tratamiento t = new Tratamiento(campoMedicamento.getText(), campoDosis.getText(),
                    campoFrecuencia.getText(), campoInicio.getValue(), campoFin.getValue());
            botonGuardar.setDisable(true);
            new Thread(() -> {
                try {
                    GestorMascotas.guardarTratamiento(idMascota, t);
                    javafx.application.Platform.runLater(() -> {
                        if (alGuardar != null) alGuardar.run();
                        Navegador.volverAtras();
                    });
                } catch (Exception e) {
                    javafx.application.Platform.runLater(() -> mostrarError("Error al guardar: " + e.getMessage()));
                    botonGuardar.setDisable(false);
                }
            }, "guardar-tratamiento-thread").start();
        });

        return crearScroll(grid);
    }

    public static void abrirPeso(int idMascota, Runnable alGuardar) {
        Navegador.navegarA("Añadir registro de peso", () -> crearVistaPeso(idMascota, alGuardar));
    }

    public static Node crearVistaPeso(int idMascota, Runnable alGuardar) {
        DatePicker campoFecha = new DatePicker();
        TextField campoPeso = new TextField();
        campoPeso.setPromptText("Ejemplo: 11.8");
        TextField campoNotas = new TextField();
        Button botonGuardar = new Button("Guardar");
        FontIcon iconoGuardar = new FontIcon(Feather.CHECK);
        iconoGuardar.setIconSize(16);
        botonGuardar.setGraphic(iconoGuardar);
        botonGuardar.getStyleClass().add("btn-primary");

        GridPane grid = crearGridBase();
        grid.add(new Label("Fecha:"), 0, 0);
        grid.add(campoFecha, 1, 0);
        grid.add(new Label("Peso (kg):"), 0, 1);
        grid.add(campoPeso, 1, 1);
        grid.add(new Label("Notas:"), 0, 2);
        grid.add(campoNotas, 1, 2);
        grid.add(botonGuardar, 1, 3);

        botonGuardar.setOnAction(evento -> {
            if (campoFecha.getValue() == null || campoPeso.getText().isBlank()) {
                mostrarError("Fecha y peso son obligatorios.");
                return;
            }

            double peso;
            try {
                peso = Double.parseDouble(campoPeso.getText().replace(",", "."));
            } catch (NumberFormatException e) {
                mostrarError("El peso debe ser un número, ejemplo: 11.8");
                return;
            }

            RegistroPeso p = new RegistroPeso(campoFecha.getValue(), peso, campoNotas.getText());
            botonGuardar.setDisable(true);
            new Thread(() -> {
                try {
                    GestorMascotas.guardarPeso(idMascota, p);
                    javafx.application.Platform.runLater(() -> {
                        if (alGuardar != null) alGuardar.run();
                        Navegador.volverAtras();
                    });
                } catch (Exception e) {
                    javafx.application.Platform.runLater(() -> mostrarError("Error al guardar: " + e.getMessage()));
                    botonGuardar.setDisable(false);
                }
            }, "guardar-peso-thread").start();
        });

        return crearScroll(grid);
    }

    private static GridPane crearGridBase() {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(15));
        grid.setHgap(10);
        grid.setVgap(10);
        return grid;
    }

    private static ScrollPane crearScroll(GridPane grid) {
        ScrollPane scroll = new ScrollPane(grid);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        return scroll;
    }

    private static void mostrarError(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.ERROR, mensaje);
        alerta.showAndWait();
    }
}
