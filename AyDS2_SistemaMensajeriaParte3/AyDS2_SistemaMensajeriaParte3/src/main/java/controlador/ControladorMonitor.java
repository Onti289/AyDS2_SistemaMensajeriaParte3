package controlador;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

import dto.ServidorDTO;
import modeloNegocio.Mensaje;
import modeloNegocio.SistemaMonitor;
import util.Util;
import vistas.VentanaMonitor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ControladorMonitor implements Observer {
	VentanaMonitor ventana;
	SistemaMonitor sistemaMonitor;
	public ControladorMonitor(SistemaMonitor sistemaMonitor) {
		this.ventana = new VentanaMonitor();
		ventana.setVisible(true);
		this.sistemaMonitor = sistemaMonitor;	
		sistemaMonitor.addObserver(this);
	}
	@Override
	public void update(Observable o, Object arg) {
		if (arg instanceof ServidorDTO) {
			ServidorDTO servidor=(ServidorDTO) arg;
			LocalDateTime ahora = LocalDateTime.now();
			DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
			if(servidor.isEnLinea()){
				String fechaHora = ahora.format(formato);
				ventana.setEstadoServidor(servidor.getNro(),Util.CTE_EN_LINEA);
				String mensaje="Servidor "+servidor.getNro()+" ("+fechaHora+")";
				ventana.setPuerto(servidor.getNro(),servidor.getPuerto());
				ventana.agregarPulso(mensaje);
			}
			else {
				ventana.setEstadoServidor(servidor.getNro(),Util.CTESERVERDESCONECTADO);
			}
			if(servidor.isPrincipal()) {
				ventana.setTipoServidor(servidor.getNro(),Util.CTE_ACTIVO);
			}
			else {
				ventana.setTipoServidor(servidor.getNro(),Util.CTE_REDUNDANTE);
			}
		}
		
	}

	
}
