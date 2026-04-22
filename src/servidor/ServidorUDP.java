package servidor;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.BindException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ServidorUDP {
    public static final int PUERTO = 5052;
    private static final int BUFFER = 2048;

    private final List<Usuario> usuarios = new ArrayList<>();
    private final Map<String, Tarjeta> tarjetasPorCedula = new HashMap<>();

    public static void main(String[] args) {
        int puerto = obtenerPuertoDesdeArgs(args, PUERTO);
        new ServidorUDP().iniciar(puerto);
    }

    public void iniciar() {
        iniciar(PUERTO);
    }

    public void iniciar(int puerto) {
        try (DatagramSocket socket = new DatagramSocket(puerto)) {
            System.out.println("Servidor UDP escuchando en puerto " + puerto);
            while (true) {
                DatagramPacket request = new DatagramPacket(new byte[BUFFER], BUFFER);
                socket.receive(request);

                String mensaje = new String(request.getData(), 0, request.getLength(), StandardCharsets.UTF_8);
                String respuesta = procesarSolicitud(mensaje);

                byte[] responseBytes = respuesta.getBytes(StandardCharsets.UTF_8);
                InetAddress address = request.getAddress();
                int port = request.getPort();
                DatagramPacket response = new DatagramPacket(responseBytes, responseBytes.length, address, port);
                socket.send(response);
            }
        } catch (BindException e) {
            throw new RuntimeException("Puerto " + puerto + " en uso. Cierre la instancia previa o use otro puerto.", e);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo iniciar el servidor UDP", e);
        }
    }

    public static int obtenerPuertoDesdeArgs(String[] args, int puertoDefault) {
        if (args == null || args.length == 0 || args[0].isBlank()) {
            return puertoDefault;
        }
        try {
            return Integer.parseInt(args[0].trim());
        } catch (NumberFormatException e) {
            return puertoDefault;
        }
    }

    public synchronized String procesarSolicitud(String mensaje) {
        String[] partes = mensaje.split("\\|");
        if (partes.length == 0) {
            return "ERROR|Solicitud vacia";
        }

        String comando = partes[0].trim().toUpperCase(Locale.ROOT);
        return switch (comando) {
            case "BUSCAR" -> buscarUsuario(partes);
            case "REGISTRAR" -> registrarUsuario(partes);
            case "RECARGAR" -> recargarSaldo(partes);
            case "PAGAR" -> pagarPasaje(partes);
            default -> "ERROR|Comando no soportado";
        };
    }

    private String buscarUsuario(String[] partes) {
        if (partes.length < 2) {
            return "ERROR|Cedula requerida";
        }
        String cedula = partes[1].trim();
        Usuario usuario = encontrarUsuario(cedula);
        if (usuario == null) {
            return "NO_ENCONTRADO|Usuario no existe";
        }
        Tarjeta tarjeta = tarjetasPorCedula.get(cedula);
        return "OK|" + usuario.getCedula() + "|" + usuario.getNombre() + "|" + usuario.getCorreo() + "|"
                + usuario.getTelefono() + "|" + (usuario.isPreferencial() ? "SI" : "NO") + "|"
                + String.format(Locale.US, "%.2f", tarjeta.getSaldo());
    }

    private String registrarUsuario(String[] partes) {
        if (partes.length < 6) {
            return "ERROR|Datos incompletos";
        }
        String cedula = partes[1].trim();
        String correo = partes[2].trim();
        String telefono = partes[3].trim();
        String nombre = partes[4].trim();
        boolean preferencial = "SI".equalsIgnoreCase(partes[5].trim()) || "TRUE".equalsIgnoreCase(partes[5].trim());

        if (encontrarUsuario(cedula) != null) {
            return "ERROR|La cedula ya existe";
        }

        Usuario usuario = new Usuario(cedula, correo, telefono, nombre, preferencial);
        usuarios.add(usuario);
        tarjetasPorCedula.put(cedula, Tarjeta.asignarTarjeta(usuario));
        return "OK|Usuario creado";
    }

    private String recargarSaldo(String[] partes) {
        if (partes.length < 3) {
            return "ERROR|Cedula y valor requeridos";
        }
        String cedula = partes[1].trim();
        Tarjeta tarjeta = tarjetasPorCedula.get(cedula);
        if (tarjeta == null) {
            return "ERROR|Usuario no encontrado";
        }
        try {
            double monto = Double.parseDouble(partes[2].trim());
            tarjeta.cargarSaldo(monto);
            return "OK|Saldo actual|" + String.format(Locale.US, "%.2f", tarjeta.getSaldo());
        } catch (NumberFormatException e) {
            return "ERROR|Valor invalido";
        } catch (IllegalArgumentException e) {
            return "ERROR|" + e.getMessage();
        }
    }

    private String pagarPasaje(String[] partes) {
        if (partes.length < 2) {
            return "ERROR|Cedula requerida";
        }
        String cedula = partes[1].trim();
        Tarjeta tarjeta = tarjetasPorCedula.get(cedula);
        if (tarjeta == null) {
            return "ERROR|Usuario no encontrado";
        }
        if (!tarjeta.pagarPasaje()) {
            return "ERROR|Saldo insuficiente";
        }
        return "OK|Saldo actual|" + String.format(Locale.US, "%.2f", tarjeta.getSaldo());
    }

    private Usuario encontrarUsuario(String cedula) {
        for (Usuario usuario : usuarios) {
            if (usuario.getCedula().equals(cedula)) {
                return usuario;
            }
        }
        return null;
    }
}
