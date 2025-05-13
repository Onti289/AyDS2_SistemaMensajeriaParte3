package sistemaMensajeria;

import modeloNegocio.SistemaServidor;
import vistas.IVista;
import vistas.VentanaMonitor;
import vistas.VentanaServidor;

public class MainServidor {

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        SistemaServidor servidor = SistemaServidor.get_Instancia();
        servidor.iniciaServidor();
        VentanaServidor ventanaServidor= new VentanaServidor();
        VentanaMonitor ventanaMonitor = new VentanaMonitor();
    }

}