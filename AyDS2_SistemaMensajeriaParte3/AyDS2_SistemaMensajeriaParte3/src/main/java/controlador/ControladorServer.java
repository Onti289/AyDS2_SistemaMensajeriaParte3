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
	SistemaServidor sistemaServidor;
	IVista ventana;
	
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
	@Override
	public void actionPerformed(ActionEvent e) {
		switch (e.getActionCommand()) {
		case Util.CTEREGISTRO: //registra servidor
			VentanaRegistraServidor ventanaRegistraServidor= (VentanaRegistraServidor) this.ventana;
			System.out.println("prueba si llega bien puerto"+ventanaRegistraServidor.getCampoPuerto());
			boolean disponible=this.sistemaServidor.puertoDisponible(ventanaRegistraServidor.getCampoPuerto());
			if(disponible) {
				System.out.println("LEFAF");
				sistemaServidor.registraServidor(ventanaRegistraServidor.getCampoIP(),ventanaRegistraServidor.getCampoPuerto());
				this.ventana.setVisible(false);
				this.setVentana(new VentanaServidor(ventanaRegistraServidor.getCampoPuerto(),ventanaRegistraServidor.getCampoIP()));
			}
			else { 
				ventanaRegistraServidor.mostrarErrorServidorYaRegistrado();
			}
			
		break;
	}
		
	}

}
