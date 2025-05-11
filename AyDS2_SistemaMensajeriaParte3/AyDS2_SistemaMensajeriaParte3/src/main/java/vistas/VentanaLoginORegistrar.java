package vistas;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import controlador.*;
import util.Util;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import java.awt.FlowLayout;
import javax.swing.JButton;

public class VentanaLoginORegistrar extends JFrame implements IVista, ActionListener, KeyListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private ControladorUsuario controlador;
	private JTextField textFieldPuerto;
	private JTextField textFieldUsuario;
	private JTextField textFieldIP;
	private JButton boton;

	/**
	 * Launch the application.
	 */

	/**
	 * Create the frame.
	 */
	public VentanaLoginORegistrar(ControladorUsuario controlador,String titulo,String nombreBoton,String nombreAccion) {
		this.controlador = controlador;
		setTitle(titulo);
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 308, 227);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		contentPane.setLayout(new GridLayout(4, 2, 0, 0));

		JPanel panel_nickName = new JPanel();
		contentPane.add(panel_nickName);
		panel_nickName.setLayout(null);

		JLabel label_NickName = new JLabel("NickName:");
		label_NickName.setBounds(90, 13, 86, 20);
		panel_nickName.add(label_NickName);
		
		textFieldUsuario = new JTextField();
		textFieldUsuario.setBounds(150, 13, 86, 20);
		panel_nickName.add(textFieldUsuario);
		textFieldUsuario.setColumns(10);
		textFieldUsuario.addKeyListener(new KeyAdapter() {
		    @Override
		    public void keyReleased(KeyEvent e) {
		        validarCampos();
		    }
		});

		JPanel panel_IP = new JPanel();
		contentPane.add(panel_IP);
		panel_IP.setLayout(null);
		
		JLabel label_IP = new JLabel("IP:");
		label_IP.setBounds(125, 13, 86, 20);
		panel_IP.add(label_IP);
		
		textFieldIP = new JTextField();
		textFieldIP.setText(Util.IPLOCAL);
		textFieldIP.setBounds(150, 13, 86, 20);
		textFieldIP.setColumns(10);
		textFieldIP.addKeyListener(new KeyAdapter() {
		    @Override
		    public void keyReleased(KeyEvent e) {
		        validarCampos();
		    }
		});
		panel_IP.add(textFieldIP);
		
		JPanel panel_Puerto = new JPanel();
		contentPane.add(panel_Puerto);
		panel_Puerto.setLayout(null);
		
		JLabel label_Puerto = new JLabel("Puerto(1024<P<65536):");
		label_Puerto.setBounds(35, 13, 112, 20);
		panel_Puerto.add(label_Puerto);
		
		textFieldPuerto = new JTextField();
		textFieldPuerto.setBounds(150, 13, 86, 20);
		textFieldPuerto.setColumns(10);
		textFieldPuerto.addKeyListener(new KeyAdapter() {
		    @Override
		    public void keyTyped(KeyEvent e) {
		        char c = e.getKeyChar();
		        if (!Character.isDigit(c)) {
		            e.consume();
		        }
		    }
		    @Override
		    public void keyReleased(KeyEvent e) {
		        validarCampos();
		    }
		});
		panel_Puerto.add(textFieldPuerto);
		
		JPanel panel_Registrarse = new JPanel();
		contentPane.add(panel_Registrarse);
		
		this.boton = new JButton(nombreBoton);
		this.boton.setToolTipText(nombreBoton);
		this.boton.setEnabled(false);
		this.boton.setActionCommand(nombreAccion);
		panel_Registrarse.add(this.boton);
	}
	
	// Lo de abajo posiblemente se borra
	public String getUsuario() {
		return textFieldUsuario.getText();
	}

	public String getIP() {
		return textFieldIP.getText();
	}
	
	public String getPuerto() {
		return this.textFieldPuerto.getText();
	}
	
	public void muestraErrorPuertoEnUso() {
		JOptionPane.showMessageDialog(null,
				"El puerto ingresado ya esta  en uso.\nPor favor, elegi  otro puerto entre 1025 y 65535.",
				"Error: Puerto en uso", JOptionPane.ERROR_MESSAGE);
		refrescaPantalla();
	}

	public void vaciarTextFieldPuerto() {
		this.textFieldPuerto.setText("");
	}
	public void vaciarTextFieldNickName() {
		this.textFieldUsuario.setText("");
	}
	public void deshabilitarBoton() {
		this.boton.setEnabled(false);	
	}
	public void refrescaPantalla() {
		deshabilitarBoton();
	    vaciarTextFieldPuerto();
	    vaciarTextFieldNickName();
	}
	private void validarCampos() {
		String usuario = getUsuario();
		String IP = getIP();
		String puerto = getPuerto();
		boolean habilitar = !usuario.isEmpty() && !puerto.isEmpty() && !IP.isEmpty();

		try {
			int p = Integer.parseInt(puerto);
			habilitar = habilitar && p > 1024 && p < 65536;
		} catch (NumberFormatException e) {
			habilitar = false;
		}

		this.boton.setEnabled(habilitar);
	}
	//metodo para pantalla login o registro
	public void mostrarErrorUsuarioYaLogueado() {
	    JOptionPane.showMessageDialog(
	        this,
	        "El usuario ya se encuentra logueado. Intente con otro usuario.",
	        "Error de Login",
	        JOptionPane.ERROR_MESSAGE
	    );
	    refrescaPantalla(); 
	}

	//metodo para pantallaLogin
	public void mostrarErrorUsuarioInexistente() {
	    JOptionPane.showMessageDialog(
	        this,
	        "El usuario es Inexistente. Intente con otro usuario.",
	        "Error de Login",
	        JOptionPane.ERROR_MESSAGE
	    );
	    this.boton.setEnabled(false);
	    refrescaPantalla();
	}
	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyReleased(KeyEvent e) {

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setActionListener(ActionListener controlador) {
		// TODO Auto-generated method stub
		this.boton.addActionListener(controlador);
	}

}
