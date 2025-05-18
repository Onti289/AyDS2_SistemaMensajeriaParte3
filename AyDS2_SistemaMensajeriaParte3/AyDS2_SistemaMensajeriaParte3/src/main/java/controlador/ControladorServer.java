package controlador;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

import modeloNegocio.SistemaServidor;
import util.Util;
import vistas.IVista;
import vistas.VentanaRegistraServidor;
import vistas.VentanaServidor;

public class ControladorServer implements ActionListener {
	private SistemaServidor sistemaServidor;
	private IVista ventana;
	
	public ControladorServer(SistemaServidor sistemaServidor) {
		this.ventana = new VentanaRegistraServidor();
		this.ventana.setVisible(true);
		this.sistemaServidor = sistemaServidor;
		this.ventana.setActionListener(this);
	}
	public void setVentana(IVista ventana) {
		this.ventana = ventana;
		this.ventana.setVisible(true);
	}
	public void detenerServidor() {
		this.sistemaServidor.detenerServidor();
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		switch (e.getActionCommand()) {
		case Util.CTEREGISTRO: //registra servidor
			VentanaRegistraServidor ventanaRegistraServidor= (VentanaRegistraServidor) this.ventana;
			boolean disponible=this.sistemaServidor.puertoDisponible(ventanaRegistraServidor.getCampoPuerto());
			if(disponible) {
				sistemaServidor.registraServidor(ventanaRegistraServidor.getCampoIP(),ventanaRegistraServidor.getCampoPuerto());
				this.ventana.setVisible(false);
				this.setVentana(new VentanaServidor(ventanaRegistraServidor.getCampoPuerto(),ventanaRegistraServidor.getCampoIP(),this));
			}
			else { 
				ventanaRegistraServidor.mostrarErrorServidorYaRegistrado();
			}
			
		break;
	}
		
	}

}
