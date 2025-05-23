package vistas;

import javax.swing.*;

import controlador.ControladorServer;
import modeloNegocio.SistemaServidor;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class VentanaServidor extends JFrame implements IVista {

    private JLabel etiquetaEstado;
    private ControladorServer controlador;
    public VentanaServidor(int puerto,String ip,ControladorServer controlador) {
    	this.controlador=controlador;
        setTitle("Servidor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // No cerrar directamente
        setSize(300, 200);
        setLocationRelativeTo(null); // Centrar la ventana

        JPanel panelPrincipal = new JPanel();
        panelPrincipal.setLayout(new GridBagLayout());
        getContentPane().add(panelPrincipal);

        etiquetaEstado = new JLabel("Servidor con ip "+ip+" funcionando en puerto "+puerto);
        etiquetaEstado.setFont(new Font("Arial", Font.PLAIN, 14));
        panelPrincipal.add(etiquetaEstado);
        
        setVisible(true);
      /*  this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
            	VentanaServidor.this.controlador.detenerServidor();
            }
        });
*/
    }

    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
    }


    @Override
    public void dispose() {
        super.dispose();
    }

	@Override
	public void setActionListener(ActionListener controlador) {
		// TODO Auto-generated method stub
		
	}
	public void mostrarErrorServidoresCaidos(String error) {
		
	}
}