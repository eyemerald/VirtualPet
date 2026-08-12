import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.util.Stack;
import java.util.function.Supplier;

public class Navegador {

    public static class Pantalla {
        private final String titulo;
        private final Supplier<Node> constructor;

        public Pantalla(String titulo, Supplier<Node> constructor) {
            this.titulo = titulo;
            this.constructor = constructor;
        }

        public String getTitulo() {
            return titulo;
        }

        public Supplier<Node> getConstructor() {
            return constructor;
        }
    }

    private static final Stack<Pantalla> historial = new Stack<>();
    private static BorderPane contenedorPrincipal;
    private static Button botonAtras;
    private static Label tituloLabel;
    private static Stage escenarioPrincipal;

    public static void inicializar(Stage escenario, BorderPane contenedor, Button btnAtras, Label lblTitulo) {
        escenarioPrincipal = escenario;
        contenedorPrincipal = contenedor;
        botonAtras = btnAtras;
        tituloLabel = lblTitulo;
    }

    public static void navegarA(String titulo, Supplier<Node> constructor) {
        historial.push(new Pantalla(titulo, constructor));
        actualizarVista();
    }

    public static void volverAtras() {
        if (historial.size() > 1) {
            historial.pop();
            actualizarVista();
        }
    }

    public static void volverAlInicio() {
        if (!historial.isEmpty()) {
            Pantalla raiz = historial.firstElement();
            historial.clear();
            historial.push(raiz);
            actualizarVista();
        }
    }

    public static void reemplazarActual(String titulo, Supplier<Node> constructor) {
        if (!historial.isEmpty()) {
            historial.pop();
        }
        historial.push(new Pantalla(titulo, constructor));
        actualizarVista();
    }

    private static void actualizarVista() {
        if (historial.isEmpty()) return;
        Pantalla actual = historial.peek();
        tituloLabel.setText(actual.getTitulo());
        botonAtras.setVisible(historial.size() > 1);
        contenedorPrincipal.setCenter(actual.getConstructor().get());
    }

    public static Stage getEscenario() {
        return escenarioPrincipal;
    }
}
