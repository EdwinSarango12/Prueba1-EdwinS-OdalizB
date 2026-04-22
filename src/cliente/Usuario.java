package cliente;

public class Usuario {
    private final String cedula;
    private final String nombre;
    private final String correo;
    private final String telefono;
    private final boolean preferencial;
    private final double saldo;

    public Usuario(String cedula, String nombre, String correo, String telefono, boolean preferencial, double saldo) {
        this.cedula = cedula;
        this.nombre = nombre;
        this.correo = correo;
        this.telefono = telefono;
        this.preferencial = preferencial;
        this.saldo = saldo;
    }

    public String getCedula() {
        return cedula;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public String getTelefono() {
        return telefono;
    }

    public boolean isPreferencial() {
        return preferencial;
    }

    public double getSaldo() {
        return saldo;
    }
}
