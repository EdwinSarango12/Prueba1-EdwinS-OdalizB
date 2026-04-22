package servidor;

public class Tarjeta {
    private static final double VALOR_PASAJE = 0.45;
    private static final double VALOR_PASAJE_PREFERENCIAL = 0.25;
    private final Usuario usuario;
    private double saldo;

    private Tarjeta(Usuario usuario) {
        this.usuario = usuario;
        this.saldo = 0.0;
    }

    public static Tarjeta asignarTarjeta(Usuario usuario) {
        return new Tarjeta(usuario);
    }

    public void cargarSaldo(double saldo) {
        if (saldo <= 0) {
            throw new IllegalArgumentException("El valor de recarga debe ser mayor a 0");
        }
        this.saldo += saldo;
    }

    public boolean pagarPasaje() {
        if (saldo >= VALOR_PASAJE) {
            saldo -= VALOR_PASAJE;
            return true;
        }
        if (usuario.isPreferencial() && saldo >= VALOR_PASAJE_PREFERENCIAL) {
            saldo -= VALOR_PASAJE_PREFERENCIAL;
            return true;
        }

        return false;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public double getSaldo() {
        return saldo;
    }
}
