package servidor;

public class Test {
    public static void main(String[] args) {
        int puerto = ServidorUDP.obtenerPuertoDesdeArgs(args, ServidorUDP.PUERTO);
        System.out.println("Iniciando prueba de servidor UDP...");
        new ServidorUDP().iniciar(puerto);
    }
}
