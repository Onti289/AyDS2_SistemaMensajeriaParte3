package prueba;

import controlador.ControladorMonitor;
import controlador.ControladorServer;
import controlador.ControladorUsuario;
import modeloNegocio.SistemaMonitor;
import modeloNegocio.SistemaServidor;
import modeloNegocio.SistemaUsuario;

public class Prueba {
    public Prueba() {
        super();
    }

    public static void main(String[] args) {
        Prueba prueba = new Prueba();
        SistemaMonitor sistemaMonitor=SistemaMonitor.get_Instancia();
        sistemaMonitor.inicia();
        ControladorMonitor controladorMonitor= new ControladorMonitor(sistemaMonitor);

        SistemaServidor servidor1 = SistemaServidor.get_Instancia();
        ControladorServer controladorServidor1=new ControladorServer(servidor1);

        SistemaServidor servidor2 = SistemaServidor.get_Instancia();
        ControladorServer controladorServidor2=new ControladorServer(servidor2); 

        SistemaUsuario sMensajeria = SistemaUsuario.get_Instancia();
        ControladorUsuario controladorUsuario = new ControladorUsuario(sMensajeria);
    }
}