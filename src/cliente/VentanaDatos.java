package cliente;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;

import java.io.IOException;
import java.util.Locale;

public class VentanaDatos {
    private Usuario usuario;

    @FXML
    private Label lblCedula;
    @FXML
    private Label lblNombre;
    @FXML
    private Label lblCorreo;
    @FXML
    private Label lblPreferencial;
    @FXML
    private Label lblSaldo;

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
        actualizarVista();
    }

    @FXML
    private void recargar() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Recargar tarjeta");
        dialog.setHeaderText(null);
        dialog.setContentText("Monto a recargar:");

        String valor = dialog.showAndWait().orElse("").trim();
        if (valor.isBlank()) {
            return;
        }

        try {
            double monto = Double.parseDouble(valor);
            String respuesta = ClienteApp.getClienteUDP().recargarSaldo(usuario.getCedula(), monto);
            if (!respuesta.startsWith("OK|")) {
                mostrarError(respuesta.replace("|", ": "));
                return;
            }
            refrescarUsuario();
        } catch (NumberFormatException ex) {
            mostrarError("Ingrese un valor numerico valido.");
        } catch (IOException ex) {
            mostrarError("No hay conexion con el servidor UDP.");
        }
    }

    @FXML
    private void pagar() {
        try {
            String respuesta = ClienteApp.getClienteUDP().pagarPasaje(usuario.getCedula());
            if (!respuesta.startsWith("OK|")) {
                mostrarError(respuesta.replace("|", ": "));
                return;
            }
            refrescarUsuario();
        } catch (IOException ex) {
            mostrarError("No hay conexion con el servidor UDP.");
        }
    }

    private void refrescarUsuario() throws IOException {
        Usuario actualizado = ClienteApp.getClienteUDP().buscarUsuario(usuario.getCedula());
        if (actualizado == null) {
            mostrarError("No fue posible actualizar los datos.");
            return;
        }
        this.usuario = actualizado;
        actualizarVista();
    }

    private void actualizarVista() {
        if (usuario == null) {
            return;
        }
        lblCedula.setText("Cedula: " + usuario.getCedula());
        lblNombre.setText("Nombre: " + usuario.getNombre());
        lblCorreo.setText("Correo: " + usuario.getCorreo());
        lblPreferencial.setText("Preferencial: " + (usuario.isPreferencial() ? "SI" : "NO"));
        lblSaldo.setText("Saldo: " + String.format(Locale.US, "%.2f", usuario.getSaldo()) + " USD");
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
