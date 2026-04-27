package servidor;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.BindException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;

public class ServidorUDP {
    public static final int PUERTO = 5052;
    private static final int BUFFER = 2048;
    private static final BigDecimal VALOR_PASAJE = new BigDecimal("0.45");
    private static final BigDecimal VALOR_PASAJE_PREFERENCIAL = new BigDecimal("0.25");

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
        String sql = """
                SELECT u.cedula, u.nombre, u.correo, u.telefono, u.preferencial, t.saldo
                FROM usuarios u
                INNER JOIN tarjetas t ON t.cedula = u.cedula
                WHERE u.cedula = ?
                """;
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, cedula);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return "NO_ENCONTRADO|Usuario no existe";
                }
                return "OK|" + rs.getString("cedula") + "|" + rs.getString("nombre") + "|"
                        + rs.getString("correo") + "|" + rs.getString("telefono") + "|"
                        + (rs.getBoolean("preferencial") ? "SI" : "NO") + "|"
                        + String.format(Locale.US, "%.2f", rs.getBigDecimal("saldo"));
            }
        } catch (SQLException e) {
            return "ERROR|No se pudo consultar la base de datos";
        }
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
        String insertUsuario = """
                INSERT INTO usuarios (cedula, nombre, correo, telefono, preferencial)
                VALUES (?, ?, ?, ?, ?)
                """;
        String insertTarjeta = """
                INSERT INTO tarjetas (cedula, saldo)
                VALUES (?, 0.00)
                """;

        try (Connection cn = DatabaseConnection.getConnection()) {
            cn.setAutoCommit(false);
            try (PreparedStatement psUsuario = cn.prepareStatement(insertUsuario);
                 PreparedStatement psTarjeta = cn.prepareStatement(insertTarjeta)) {
                psUsuario.setString(1, cedula);
                psUsuario.setString(2, nombre);
                psUsuario.setString(3, correo);
                psUsuario.setString(4, telefono);
                psUsuario.setBoolean(5, preferencial);
                psUsuario.executeUpdate();

                psTarjeta.setString(1, cedula);
                psTarjeta.executeUpdate();

                cn.commit();
                return "OK|Usuario creado";
            } catch (SQLException e) {
                cn.rollback();
                if (e.getSQLState() != null && e.getSQLState().startsWith("23")) {
                    return "ERROR|La cedula ya existe";
                }
                return "ERROR|No se pudo registrar el usuario";
            } finally {
                cn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            return "ERROR|No se pudo conectar con la base de datos";
        }
    }

    private String recargarSaldo(String[] partes) {
        if (partes.length < 3) {
            return "ERROR|Cedula y valor requeridos";
        }
        String cedula = partes[1].trim();
        try {
            BigDecimal monto = new BigDecimal(partes[2].trim()).setScale(2, RoundingMode.HALF_UP);
            if (monto.compareTo(BigDecimal.ZERO) <= 0) {
                return "ERROR|El valor de recarga debe ser mayor a 0";
            }
            String selectSaldo = "SELECT saldo FROM tarjetas WHERE cedula = ? FOR UPDATE";
            String updateSaldo = "UPDATE tarjetas SET saldo = ? WHERE cedula = ?";
            String insertMovimiento = """
                    INSERT INTO movimientos (cedula, tipo, monto, saldo_anterior, saldo_nuevo, descripcion)
                    VALUES (?, 'RECARGA', ?, ?, ?, ?)
                    """;

            try (Connection cn = DatabaseConnection.getConnection()) {
                cn.setAutoCommit(false);
                try (PreparedStatement psSelect = cn.prepareStatement(selectSaldo)) {
                    psSelect.setString(1, cedula);
                    try (ResultSet rs = psSelect.executeQuery()) {
                        if (!rs.next()) {
                            cn.rollback();
                            return "ERROR|Usuario no encontrado";
                        }
                        BigDecimal saldoAnterior = rs.getBigDecimal("saldo").setScale(2, RoundingMode.HALF_UP);
                        BigDecimal saldoNuevo = saldoAnterior.add(monto).setScale(2, RoundingMode.HALF_UP);

                        try (PreparedStatement psUpdate = cn.prepareStatement(updateSaldo);
                             PreparedStatement psMov = cn.prepareStatement(insertMovimiento)) {
                            psUpdate.setBigDecimal(1, saldoNuevo);
                            psUpdate.setString(2, cedula);
                            psUpdate.executeUpdate();

                            psMov.setString(1, cedula);
                            psMov.setBigDecimal(2, monto);
                            psMov.setBigDecimal(3, saldoAnterior);
                            psMov.setBigDecimal(4, saldoNuevo);
                            psMov.setString(5, "Recarga de saldo");
                            psMov.executeUpdate();
                        }

                        cn.commit();
                        return "OK|Saldo actual|" + String.format(Locale.US, "%.2f", saldoNuevo);
                    }
                } catch (SQLException e) {
                    cn.rollback();
                    return "ERROR|No se pudo registrar la recarga";
                } finally {
                    cn.setAutoCommit(true);
                }
            }
        } catch (NumberFormatException | ArithmeticException e) {
            return "ERROR|Valor invalido";
        } catch (SQLException e) {
            return "ERROR|No se pudo conectar con la base de datos";
        }
    }

    private String pagarPasaje(String[] partes) {
        if (partes.length < 2) {
            return "ERROR|Cedula requerida";
        }
        String cedula = partes[1].trim();
        String selectData = """
                SELECT t.saldo, u.preferencial
                FROM tarjetas t
                INNER JOIN usuarios u ON u.cedula = t.cedula
                WHERE t.cedula = ?
                FOR UPDATE
                """;
        String updateSaldo = "UPDATE tarjetas SET saldo = ? WHERE cedula = ?";
        String insertMovimiento = """
                INSERT INTO movimientos (cedula, tipo, monto, saldo_anterior, saldo_nuevo, descripcion)
                VALUES (?, 'PAGO', ?, ?, ?, ?)
                """;

        try (Connection cn = DatabaseConnection.getConnection()) {
            cn.setAutoCommit(false);
            try (PreparedStatement psSelect = cn.prepareStatement(selectData)) {
                psSelect.setString(1, cedula);
                try (ResultSet rs = psSelect.executeQuery()) {
                    if (!rs.next()) {
                        cn.rollback();
                        return "ERROR|Usuario no encontrado";
                    }
                    BigDecimal saldoAnterior = rs.getBigDecimal("saldo").setScale(2, RoundingMode.HALF_UP);
                    boolean preferencial = rs.getBoolean("preferencial");
                    BigDecimal valorPasaje = preferencial ? VALOR_PASAJE_PREFERENCIAL : VALOR_PASAJE;
                    if (saldoAnterior.compareTo(valorPasaje) < 0) {
                        cn.rollback();
                        return "ERROR|Saldo insuficiente";
                    }
                    BigDecimal saldoNuevo = saldoAnterior.subtract(valorPasaje).setScale(2, RoundingMode.HALF_UP);

                    try (PreparedStatement psUpdate = cn.prepareStatement(updateSaldo);
                         PreparedStatement psMov = cn.prepareStatement(insertMovimiento)) {
                        psUpdate.setBigDecimal(1, saldoNuevo);
                        psUpdate.setString(2, cedula);
                        psUpdate.executeUpdate();

                        psMov.setString(1, cedula);
                        psMov.setBigDecimal(2, valorPasaje);
                        psMov.setBigDecimal(3, saldoAnterior);
                        psMov.setBigDecimal(4, saldoNuevo);
                        psMov.setString(5, "Pago de pasaje");
                        psMov.executeUpdate();
                    }

                    cn.commit();
                    return "OK|Saldo actual|" + String.format(Locale.US, "%.2f", saldoNuevo);
                }
            } catch (SQLException e) {
                cn.rollback();
                return "ERROR|No se pudo registrar el pago";
            } finally {
                cn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            return "ERROR|No se pudo conectar con la base de datos";
        }
    }
}
