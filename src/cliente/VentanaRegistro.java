package cliente;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;

import java.io.IOException;

public class VentanaRegistro {
    @FXML
    private TextField txtCedula;
    @FXML
    private TextField txtCorreo;
    @FXML
    private TextField txtTelefono;
    @FXML
    private TextField txtNombre;
    @FXML
    private CheckBox chkPreferencial;

    @FXML
    private void crearUsuario() {
        String cedula = txtCedula.getText().trim();
        String correo = txtCorreo.getText().trim();
        String telefono = txtTelefono.getText().trim();
        String nombre = txtNombre.getText().trim();
        boolean preferencial = chkPreferencial.isSelected();

        if (cedula.isBlank() || correo.isBlank() || telefono.isBlank() || nombre.isBlank()) {
            mostrarError("Complete todos los campos.");
            return;
        }

        try {
            String respuesta = ClienteApp.getClienteUDP().registrarUsuario(cedula, correo, telefono, nombre, preferencial);
            if (!respuesta.startsWith("OK|")) {
                mostrarError(respuesta.replace("|", ": "));
                return;
            }
            mostrarInfo("Usuario creado correctamente.");
            Usuario usuario = ClienteApp.getClienteUDP().buscarUsuario(cedula);
            if (usuario != null) {
                ClienteApp.mostrarDatos(usuario);
            }
        } catch (IOException ex) {
            mostrarError("No hay conexion con el servidor UDP.");
        }
    }

    @FXML
    private void volver() {
        ClienteApp.mostrarBusqueda();
    }

    private void mostrarInfo(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Informacion");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
