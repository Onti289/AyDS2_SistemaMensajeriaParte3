package vistas;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import controlador.ControladorUsuario;
import dto.UsuarioDTO;
import util.Util;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.util.List;
import java.awt.event.ActionEvent;
import javax.swing.AbstractListModel;
import javax.swing.DefaultListModel;
import javax.swing.JScrollPane;

public class VentanaDirectorio extends JFrame implements IVista, ActionListener {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JPanel panelIniciar;
	private JButton btnAgregarContacto;
	private JScrollPane scrollPane;
	private JList<UsuarioDTO> list;
	private ControladorUsuario controlador;

	/**
	 * Launch the application.
	 */

	/**
	 * Create the frame.
	 */
	public VentanaDirectorio(ControladorUsuario controlador,  List<UsuarioDTO> lista) {
		this.controlador = controlador;
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		this.contentPane = new JPanel();
		this.contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(this.contentPane);
		this.contentPane.setLayout(new BorderLayout(0, 0));

		this.panelIniciar = new JPanel();
		this.contentPane.add(this.panelIniciar, BorderLayout.SOUTH);

		this.btnAgregarContacto = new JButton("Agregar contacto");
		this.btnAgregarContacto.setActionCommand(Util.CTEAGREGAR);
		this.btnAgregarContacto.setToolTipText("Agregar contacto");
		this.panelIniciar.add(this.btnAgregarContacto);
		this.btnAgregarContacto.setEnabled(false);
		this.scrollPane = new JScrollPane();
		this.contentPane.add(this.scrollPane, BorderLayout.CENTER);

		DefaultListModel<UsuarioDTO> modelo = new DefaultListModel<>();
		modelo.clear();

		for (UsuarioDTO c : lista) {
			modelo.addElement(c);
		}

		this.list = new JList<>(modelo);
		// Agrego listener para habilitar el bot n al seleccionar un contacto
		this.list.addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting()) {
				btnAgregarContacto.setEnabled(list.getSelectedIndex() != -1);
			}
		});

		this.scrollPane.setViewportView(this.list);

	}

	public UsuarioDTO getUsuario() {
		return this.list.getSelectedValue();
	}

	public void actionPerformed(ActionEvent e) {

	}

	@Override
	public void setActionListener(ActionListener controlador) {
		// TODO Auto-generated method stub
		this.btnAgregarContacto.addActionListener(controlador);
	}
	
	public void mostrarConfirmacionContactoAgregado() {
        JOptionPane.showMessageDialog(this, "Contacto agregado exitosamente!", "Registro exitoso",
                JOptionPane.INFORMATION_MESSAGE);
    }
	
	public void mostrarErrorContactoYaAgendado() {
		JOptionPane.showMessageDialog(this, "El contacto que intentas agregar ya esta en tu agenda.",
				"Contacto ya agendado", JOptionPane.ERROR_MESSAGE);
	}
	public void mostrarErrorServidoresCaidos(String error) {
		JOptionPane.showMessageDialog(this, error, "Error", JOptionPane.ERROR_MESSAGE);
	}

}