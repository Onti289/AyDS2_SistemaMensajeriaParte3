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
import javax.swing.SwingConstants;

public class VentanaLoginORegistrar extends JFrame implements IVista, ActionListener, KeyListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private ControladorUsuario controlador;
	private JTextField textFieldUsuario;
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
		setBounds(100, 100, 200, 213);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		contentPane.setLayout(new GridLayout(2, 2, 0, 0));

		JPanel panel_nickName = new JPanel();
		contentPane.add(panel_nickName);
		panel_nickName.setLayout(null);

		JLabel label_NickName = new JLabel("NickName:");
		label_NickName.setHorizontalAlignment(SwingConstants.LEFT);
		label_NickName.setBounds(10, 42, 86, 20);
		panel_nickName.add(label_NickName);
		
		textFieldUsuario = new JTextField();
		textFieldUsuario.addKeyListener(this);
		textFieldUsuario.setBounds(90, 42, 80, 20);
		panel_nickName.add(textFieldUsuario);
		textFieldUsuario.setColumns(10);
		
		JPanel panel_Registrarse = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel_Registrarse.getLayout();
		flowLayout.setVgap(40);
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


	public void vaciarTextFieldNickName() {
		this.textFieldUsuario.setText("");
	}
	public void deshabilitarBoton() {
		this.boton.setEnabled(false);	
	}
	public void refrescaPantalla() {
		deshabilitarBoton();
	    vaciarTextFieldNickName();
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
	public void mostrarErrorServidoresCaidos(String error) {
		JOptionPane.showMessageDialog(this, error, "Error", JOptionPane.ERROR_MESSAGE);
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
		// TODO Auto-generated method stub
		this.boton.setEnabled(
				!(textFieldUsuario.getText().isEmpty()));
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
