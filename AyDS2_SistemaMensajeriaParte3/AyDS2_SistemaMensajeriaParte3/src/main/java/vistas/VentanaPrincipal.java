package vistas;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import controlador.ControladorUsuario;
import dto.UsuarioDTO;
import modeloNegocio.Usuario;
import util.Util;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JSplitPane;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.JTextArea;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.AbstractListModel;
import javax.swing.DefaultListModel;
import javax.swing.SwingConstants;
import java.awt.event.ActionEvent;

public class VentanaPrincipal extends JFrame implements IVista, ActionListener, KeyListener, MouseListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField textFieldMensaje;
	private ControladorUsuario controlador;
	private JButton botonAgregarContacto;
	private JButton botonNuevaConversacion;
	private JButton botonEnviar;
	private JList<UsuarioDTO> listaConversacionesActivas;
	private JTextArea textAreaChat;
	private JLabel textFieldNameContacto;
	private Set<UsuarioDTO> notificacionesPendientes = new HashSet<>();

	/**
	 * Launch the application.
	 */

	/**
	 * Create the frame.
	 */
	public VentanaPrincipal(ControladorUsuario controlador) {
		this.controlador = controlador;
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		contentPane.setLayout(new GridLayout(1, 2, 0, 0));

		JPanel panel = new JPanel();
		contentPane.add(panel);
		panel.setLayout(new BorderLayout(0, 0));

		JPanel panel_1 = new JPanel();
		panel.add(panel_1, BorderLayout.SOUTH);
		panel_1.setLayout(new GridLayout(1, 2, 0, 0));

		this.botonAgregarContacto = new JButton("Agregar contacto");
		this.botonAgregarContacto.setActionCommand(Util.CTEAGREGARCONTACTO);
		panel_1.add(this.botonAgregarContacto);

		this.botonNuevaConversacion = new JButton("Nueva conversacion");
		this.botonNuevaConversacion.setActionCommand(Util.CTENUEVACONVER);
		panel_1.add(this.botonNuevaConversacion);

		JScrollPane scrollPane_1 = new JScrollPane();
		panel.add(scrollPane_1, BorderLayout.CENTER);

		listaConversacionesActivas = new JList();
		listaConversacionesActivas.setModel(new AbstractListModel() {
			String[] values = new String[] {};

			public int getSize() {
				return values.length;
			}

			public Object getElementAt(int index) {
				return values[index];
			}
		});
		listaConversacionesActivas.setCellRenderer(new UsuarioRenderer(notificacionesPendientes));
		scrollPane_1.setViewportView(listaConversacionesActivas);
		this.listaConversacionesActivas.addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting()) {
				UsuarioDTO seleccionado = listaConversacionesActivas.getSelectedValue();
				if (seleccionado != null) {
					vaciarTextFieldMensajes();
					botonEnviar.setEnabled(false);
					limpiarNotificacion(seleccionado);
					controlador.contactoSeleccionadoDesdeLista(seleccionado);
				}
			}
		});

		JPanel panel_2 = new JPanel();
		contentPane.add(panel_2);
		panel_2.setLayout(new BorderLayout(0, 0));

		textFieldNameContacto = new JLabel();
		textFieldNameContacto.setHorizontalAlignment(SwingConstants.CENTER);
		panel_2.add(textFieldNameContacto, BorderLayout.NORTH);

		JPanel panel_3 = new JPanel();
		panel_2.add(panel_3, BorderLayout.SOUTH);
		panel_3.setLayout(new GridLayout(1, 2, 0, 0));

		textFieldMensaje = new JTextField();
		this.textFieldMensaje.addKeyListener(this);
		this.textFieldMensaje.setToolTipText("Mensaje");
		panel_3.add(textFieldMensaje);
		textFieldMensaje.setColumns(10);

		this.botonEnviar = new JButton("Enviar");
		this.botonEnviar.setEnabled(false);
		this.botonEnviar.setActionCommand(Util.CTEENVIAR);
		panel_3.add(this.botonEnviar);

		JScrollPane scrollPane = new JScrollPane();
		panel_2.add(scrollPane, BorderLayout.CENTER);

		textAreaChat = new JTextArea();
		textAreaChat.setWrapStyleWord(true);
		textAreaChat.setLineWrap(true);
		textAreaChat.setEditable(false);
		textAreaChat.setText("");
		scrollPane.setViewportView(textAreaChat);
		this.addWindowListener(new java.awt.event.WindowAdapter() {
		
		});

	}

	@Override
	public void setActionListener(ActionListener controlador) {
		// TODO Auto-generated method stub
		this.botonAgregarContacto.addActionListener(controlador);
		this.botonNuevaConversacion.addActionListener(controlador);
		this.botonEnviar.addActionListener(controlador);
	}

	// Este metodo actualiza la lista de conversaciones de la izquierda
	// de la ventana principal
	public void actualizarListaChats(List<UsuarioDTO> contactos) {
		DefaultListModel<UsuarioDTO> modelo = new DefaultListModel<>();
		for (UsuarioDTO u : contactos) {
			modelo.addElement(u);
		}
		this.listaConversacionesActivas.setModel(modelo);

	}

	public void notificarNuevoMensaje(UsuarioDTO usuario) {
		notificacionesPendientes.add(usuario);
		this.listaConversacionesActivas.repaint(); // Para refrescar la lista visualmente
	}

	public void limpiarNotificacion(UsuarioDTO usuario) {
		notificacionesPendientes.remove(usuario);
		this.listaConversacionesActivas.repaint();
	}

	public boolean hayConversaciones() {
		return listaConversacionesActivas.getModel().getSize() != 0;
	}

	public UsuarioDTO getContactoConversacionActual() {
		return this.listaConversacionesActivas.getSelectedValue();
	}

	public void setTextFieldNameContacto(String name) {
		this.textFieldNameContacto.setText(name);
	}

	public void TitulonameUsuario(String nombre) {
		this.setTitle(nombre);
	}

	public void setDejarSeleccionadoContactoNuevaConversacion(UsuarioDTO contacto) {
		this.listaConversacionesActivas.setSelectedValue(contacto, true);
	}

	public void agregarMensajeAchat(String contenido, LocalDateTime fechayhora, String emisor) {
		this.textAreaChat.append(String.format("%s (%s): %s\n", emisor,
				fechayhora.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), contenido));
	}

	public String getTextFieldMensaje() {
		return this.textFieldMensaje.getText();
	}

	public void actionPerformed(ActionEvent e) {

	}

	public void mostrarErrorEnvioMensaje(String error) {
		JOptionPane.showMessageDialog(this, error, "Error", JOptionPane.ERROR_MESSAGE);
	}

	public void limpiarChat() {
		this.textAreaChat.setText("");
	}

	public void vaciarTextFieldMensajes() {
		this.textFieldMensaje.setText("");
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		this.controlador.contactoSeleccionadoDesdeLista(getContactoConversacionActual());
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

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
		this.botonEnviar.setEnabled(
				!(textFieldMensaje.getText().isEmpty()) && (listaConversacionesActivas.getSelectedValue() != null));
	}

	public void limpiarBuffer() {
		this.textFieldMensaje.setText("");
		this.botonEnviar.setEnabled(false);
	}
}
