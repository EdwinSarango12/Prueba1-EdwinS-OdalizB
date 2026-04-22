package cliente;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ClienteApp extends Application {
    private static final int PUERTO_DEFAULT = 5052;
    private static Stage primaryStage;
    private static ClienteUDP clienteUDP;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        stage.setResizable(false);
        mostrarBusqueda();
        stage.show();
    }

    public static void main(String[] args) {
        int puerto = obtenerPuertoDesdeArgs(args, PUERTO_DEFAULT);
        clienteUDP = new ClienteUDP("127.0.0.1", puerto);
        launch(args);
    }

    public static ClienteUDP getClienteUDP() {
        return clienteUDP;
    }

    public static void mostrarBusqueda() {
        cargarVista("VentanaBusqueda.fxml", "Aplicacion Cliente - Consulta");
    }

    public static void mostrarRegistro() {
        cargarVista("VentanaRegistro.fxml", "Aplicacion Cliente - Registro");
    }

    public static void mostrarDatos(Usuario usuario) {
        try {
            FXMLLoader loader = new FXMLLoader(ClienteApp.class.getResource("VentanaDatos.fxml"));
            Parent root = loader.load();
            VentanaDatos controller = loader.getController();
            controller.setUsuario(usuario);
            primaryStage.setTitle("Aplicacion Cliente - Datos");
            primaryStage.setScene(new Scene(root));
        } catch (IOException e) {
            throw new RuntimeException("No se pudo cargar la vista de datos", e);
        }
    }

    private static void cargarVista(String archivoFxml, String titulo) {
        try {
            FXMLLoader loader = new FXMLLoader(ClienteApp.class.getResource(archivoFxml));
            Parent root = loader.load();
            primaryStage.setTitle(titulo);
            primaryStage.setScene(new Scene(root));
        } catch (IOException e) {
            throw new RuntimeException("No se pudo cargar la vista " + archivoFxml, e);
        }
    }

    private static int obtenerPuertoDesdeArgs(String[] args, int puertoDefault) {
        if (args == null || args.length == 0 || args[0].isBlank()) {
            return puertoDefault;
        }
        try {
            return Integer.parseInt(args[0].trim());
        } catch (NumberFormatException e) {
            return puertoDefault;
        }
    }
}
