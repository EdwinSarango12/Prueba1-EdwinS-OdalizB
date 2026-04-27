package servidor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseConnection {
    private static final String URL =
            "jdbc:mysql://127.0.0.1:3306/transporte_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Guayaquil";
    private static final String USER = "root";
    private static final String PASSWORD = "Robtop12";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("No se encontro el driver de MySQL. Agrega mysql-connector-j al proyecto.", e);
        }
    }

    private DatabaseConnection() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
