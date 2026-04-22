package cliente;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

import java.io.IOException;

public class VentanaBusqueda {
    @FXML
    private TextField txtCedula;

    @FXML
    private void buscarUsuario() {
        String cedula = txtCedula.getText().trim();
        if (cedula.isBlank()) {
            mostrarError("Ingrese una cedula.");
            return;
        }
        try {
            Usuario usuario = ClienteApp.getClienteUDP().buscarUsuario(cedula);
            if (usuario == null) {
                mostrarError("Usuario no encontrado.");
                return;
            }
            ClienteApp.mostrarDatos(usuario);
        } catch (IOException ex) {
            mostrarError("No hay conexion con el servidor UDP.");
        }
    }

    @FXML
    private void abrirRegistro() {
        ClienteApp.mostrarRegistro();
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
