package sistemaMensajeria;

import controlador.ControladorMonitor;
import modeloNegocio.SistemaMonitor;

public class MainMonitor {

	public static void main(String[] args) {
		SistemaMonitor sistemaMonitor=SistemaMonitor.get_Instancia();
		sistemaMonitor.inicia();
		ControladorMonitor controladorMonitor= new ControladorMonitor(sistemaMonitor);
	}

}
