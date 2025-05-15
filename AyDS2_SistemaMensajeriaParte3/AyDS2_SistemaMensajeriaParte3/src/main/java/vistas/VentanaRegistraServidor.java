package vistas;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;

import util.Util;

public class VentanaRegistraServidor extends JFrame implements IVista{
    private JTextField campoIP;
    private JTextField campoPuerto;
    private JButton botonIniciar;

    public VentanaRegistraServidor() {
        setTitle("Registro servidor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(300, 200);
        setLocationRelativeTo(null);

        JLabel lblIP = new JLabel("IP:");
        JLabel lblPuerto = new JLabel("Puerto (1025 o 1026):");

        campoIP = new JTextField(10);
        campoPuerto = new JTextField(10);
        botonIniciar = new JButton("Iniciar Servidor");
        botonIniciar.setActionCommand(Util.CTEREGISTRO);
        campoIP.setText(Util.IPLOCAL);
        campoIP.getDocument().addDocumentListener(new ValidacionListener());
        campoPuerto.getDocument().addDocumentListener(new ValidacionListener());

        JPanel panel = new JPanel();
        GroupLayout layout = new GroupLayout(panel);
        layout.setHorizontalGroup(
        	layout.createParallelGroup(Alignment.CENTER)
        		.addGroup(layout.createSequentialGroup()
        			.addGroup(layout.createParallelGroup(Alignment.LEADING)
        				.addGroup(layout.createSequentialGroup()
        					.addGap(93)
        					.addComponent(lblIP)
        					.addComponent(campoIP, 153, 153, 153))
        				.addGroup(layout.createSequentialGroup()
        					.addGap(75)
        					.addComponent(botonIniciar))
        				.addGroup(layout.createSequentialGroup()
        					.addContainerGap()
        					.addComponent(lblPuerto)
        					.addPreferredGap(ComponentPlacement.RELATED)
        					.addComponent(campoPuerto, 153, 153, 153)))
        			.addContainerGap(10, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
        	layout.createParallelGroup(Alignment.LEADING)
        		.addGroup(layout.createSequentialGroup()
        			.addGroup(layout.createParallelGroup(Alignment.BASELINE)
        				.addComponent(lblIP)
        				.addComponent(campoIP, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
        			.addGap(38)
        			.addGroup(layout.createParallelGroup(Alignment.BASELINE)
        				.addComponent(lblPuerto)
        				.addComponent(campoPuerto, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
        			.addGap(44)
        			.addComponent(botonIniciar)
        			.addContainerGap())
        );
        panel.setLayout(layout);

        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        getContentPane().add(panel);
        
    }
    private class ValidacionListener implements DocumentListener {
        @Override
        public void insertUpdate(DocumentEvent e) {
            validarCampos();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            validarCampos();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            validarCampos();
        }
    }
    public String getCampoIP() {
        return campoIP.getText();
    }

    public int getCampoPuerto() {
    	int valor = Integer.parseInt(campoPuerto.getText());
        return valor;
    }
    public void vaciarTextFieldPuerto() {
		this.campoPuerto.setText("");
	}
	public void deshabilitarBoton() {
		this.botonIniciar.setEnabled(false);	
	}
	 private void vaciarTextFieldIp() {
		this.campoIP.setText("");
	}
	public void refrescaPantalla() {
		deshabilitarBoton();
	    vaciarTextFieldPuerto();
	    vaciarTextFieldIp();
	}
   

	public void mostrarErrorServidorYaRegistrado() {
	    JOptionPane.showMessageDialog(
	        this,
	        "El Servidor ya se encuentra iniciado. Intente con otro puerto/ip.",
	        "Error de Registro",
	        JOptionPane.ERROR_MESSAGE
	    );
	    refrescaPantalla(); 
	}
	private void validarCampos() {
	    String ip = campoIP.getText().trim();
	    String puertoStr = campoPuerto.getText().trim();

	    boolean habilitar = false;

	    if (!ip.isEmpty() && !puertoStr.isEmpty()) {
	        try {
	            int puerto = Integer.parseInt(puertoStr);
	            if (puerto >= 1025 && puerto <= 65535) {
	                habilitar = true;
	            }
	        } catch (NumberFormatException e) {
	            // El campo puerto no es un número válido
	        }
	    }

	    botonIniciar.setEnabled(habilitar);
	}

	@Override
	public void setActionListener(ActionListener controlador) {
		this.botonIniciar.addActionListener(controlador);
		
	}
}
