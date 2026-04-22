package cliente;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class ClienteUDP {
    private static final int BUFFER = 2048;
    private final String host;
    private final int puerto;

    public ClienteUDP(String host, int puerto) {
        this.host = host;
        this.puerto = puerto;
    }

    public Usuario buscarUsuario(String cedula) throws IOException {
        String respuesta = enviar("BUSCAR|" + cedula);
        String[] partes = respuesta.split("\\|");
        if (!"OK".equals(partes[0])) {
            return null;
        }
        return new Usuario(
                partes[1],
                partes[2],
                partes[3],
                partes[4],
                "SI".equalsIgnoreCase(partes[5]),
                Double.parseDouble(partes[6])
        );
    }

    public String registrarUsuario(String cedula, String correo, String telefono, String nombre, boolean preferencial)
            throws IOException {
        String pref = preferencial ? "SI" : "NO";
        return enviar("REGISTRAR|" + cedula + "|" + correo + "|" + telefono + "|" + nombre + "|" + pref);
    }

    public String recargarSaldo(String cedula, double monto) throws IOException {
        return enviar("RECARGAR|" + cedula + "|" + String.format(Locale.US, "%.2f", monto));
    }

    public String pagarPasaje(String cedula) throws IOException {
        return enviar("PAGAR|" + cedula);
    }

    private String enviar(String mensaje) throws IOException {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(3000);
            byte[] data = mensaje.getBytes(StandardCharsets.UTF_8);
            InetAddress address = InetAddress.getByName(host);
            DatagramPacket request = new DatagramPacket(data, data.length, address, puerto);
            socket.send(request);

            DatagramPacket response = new DatagramPacket(new byte[BUFFER], BUFFER);
            socket.receive(response);
            return new String(response.getData(), 0, response.getLength(), StandardCharsets.UTF_8);
        }
    }
}
