import atlantafx.base.theme.PrimerLight;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.util.ArrayList;

public class VirtuaPetApp extends Application {

    static FlowPane contenedorMascotas = new FlowPane();
    static ArrayList<Integer> idsMascotas = new ArrayList<>();

    @Override
    public void start(Stage escenario) {

        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());

        CrearTablas.crearTablas();

        Button botonAtras = new Button();
        FontIcon iconoAtras = new FontIcon(Feather.ARROW_LEFT);
        iconoAtras.setIconSize(18);
        botonAtras.setGraphic(iconoAtras);
        botonAtras.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 4 8 4 8;");
        botonAtras.setOnAction(evento -> Navegador.volverAtras());

        Label tituloVentana = new Label("VirtuaPet");
        tituloVentana.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        HBox topBar = new HBox(10, botonAtras, tituloVentana);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(10, 14, 10, 14));
        topBar.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #E0E0E0; -fx-border-width: 0 0 1 0;");

        BorderPane raiz = new BorderPane();
        raiz.setTop(topBar);

        Navegador.inicializar(escenario, raiz, botonAtras, tituloVentana);
        Navegador.navegarA("VirtuaPet", VirtuaPetApp::crearVistaInicio);

        // Limitamos el tamaño al espacio real disponible en la pantalla
        // (dejando margen para la barra de tareas), en vez de forzar
        // siempre 680x680 aunque la pantalla sea más pequeña.
        double anchoDisponible = Screen.getPrimary().getVisualBounds().getWidth() - 100;
        double altoDisponible = Screen.getPrimary().getVisualBounds().getHeight() - 100;
        double ancho = Math.min(680, anchoDisponible);
        double alto = Math.min(680, altoDisponible);

        Scene escena = new Scene(raiz, ancho, alto);
        
        URL cssUrl = VirtuaPetApp.class.getResource("/styles.css");
        if (cssUrl != null) {
            escena.getStylesheets().add(cssUrl.toExternalForm());
        }

        escenario.setTitle("VirtuaPet");
        escenario.setScene(escena);
        escenario.centerOnScreen();
        escenario.show();
    }

    public static Node crearVistaInicio() {
        Button botonAñadir = new Button("+ Añadir mascota");
        FontIcon iconoAñadir = new FontIcon(Feather.PLUS_CIRCLE);
        iconoAñadir.setIconSize(16);
        botonAñadir.setGraphic(iconoAñadir);
        botonAñadir.getStyleClass().add("btn-primary");
        botonAñadir.setOnAction(evento -> abrirFormularioNuevaMascota());

        contenedorMascotas.setHgap(12);
        contenedorMascotas.setVgap(12);
        contenedorMascotas.setPadding(new Insets(12));
        contenedorMascotas.setStyle("-fx-background-color: #F5F5F5;");

        VBox contenido = new VBox(12, botonAñadir, contenedorMascotas);
        contenido.setPadding(new Insets(12));
        contenido.setStyle("-fx-background-color: #F5F5F5;");

        cargarListaMascotas();

        ScrollPane scroll = new ScrollPane(contenido);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        return scroll;
    }

    static void cargarListaMascotas() {
        contenedorMascotas.getChildren().clear();
        idsMascotas.clear();

        new Thread(() -> {
            try {
                ArrayList<String[]> mascotas = GestorMascotas.obtenerListaMascotas();
                Platform.runLater(() -> {
                    contenedorMascotas.getChildren().clear();
                    idsMascotas.clear();
                    for (String[] datos : mascotas) {
                        int idMascota = Integer.parseInt(datos[0]);
                        String nombre = datos[1];
                        
                        VBox tarjeta = crearTarjetaMascota(nombre, idMascota);
                        contenedorMascotas.getChildren().add(tarjeta);
                        idsMascotas.add(idMascota);
                    }
                });
            } catch (Exception e) {
                AppLogger.logSevere("Error cargando lista de mascotas: " + e.getMessage());
                Platform.runLater(() -> System.err.println("Error al cargar mascotas: " + e.getMessage()));
            }
        }, "cargar-lista-thread").start();
    }

    private static VBox crearTarjetaMascota(String nombre, int idMascota) {
        Label avatar = new Label(nombre.isEmpty() ? "?" : nombre.substring(0, 1).toUpperCase());
        avatar.getStyleClass().add("avatar-circle");
        avatar.setAlignment(Pos.CENTER);

        Label nombreLabel = new Label(nombre);
        nombreLabel.getStyleClass().add("pet-card-name");
        nombreLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-alignment: center;");
        nombreLabel.setMaxWidth(80);
        nombreLabel.setWrapText(true);

        VBox tarjeta = new VBox(8, avatar, nombreLabel);
        tarjeta.getStyleClass().add("card");
        tarjeta.getStyleClass().add("clickable-card");
        tarjeta.setAlignment(Pos.CENTER);
        tarjeta.setPadding(new Insets(12));
        tarjeta.setPrefWidth(110);
        tarjeta.setMinHeight(120);

        tarjeta.setOnMouseClicked(evento -> {
            if (evento.getButton() == MouseButton.PRIMARY && evento.getClickCount() == 1) {
                FichaMascotaVentana.abrir(idMascota);
            }
        });

        return tarjeta;
    }

    static void abrirFormularioNuevaMascota() {
        FormularioMascota.abrir();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
