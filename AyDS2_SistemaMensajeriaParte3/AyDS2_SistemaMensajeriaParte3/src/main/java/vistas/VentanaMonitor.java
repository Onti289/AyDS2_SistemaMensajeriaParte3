package vistas;

import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class VentanaMonitor extends JFrame{
	 private JLabel lblEstado1;
	    private JLabel lblTipo1;
	    private JLabel lblEstado2;
	    private JLabel lblTipo2;
	    private JTextArea areaTexto;

	    public VentanaMonitor() {
	        setTitle("Monitoreo de Servidores");
	        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	        setSize(600, 400);

	        // Panel izquierdo
	        JPanel panelIzquierdo = new JPanel();
	        panelIzquierdo.setLayout(new BoxLayout(panelIzquierdo, BoxLayout.Y_AXIS));

	        // Servidor 1
	        JPanel panelServidor1 = new JPanel(new GridLayout(2, 2, 5, 5));
	        panelServidor1.setBorder(BorderFactory.createTitledBorder("Servidor1"));
	        panelServidor1.add(new JLabel("Estado:"));
	        lblEstado1 = new JLabel("Desconocido");
	        panelServidor1.add(lblEstado1);
	        panelServidor1.add(new JLabel("Tipo:"));
	        lblTipo1 = new JLabel("Desconocido");
	        panelServidor1.add(lblTipo1);

	        // Servidor 2
	        JPanel panelServidor2 = new JPanel(new GridLayout(2, 2, 5, 5));
	        panelServidor2.setBorder(BorderFactory.createTitledBorder("Servidor2"));
	        panelServidor2.add(new JLabel("Estado:"));
	        lblEstado2 = new JLabel("Desconocido");
	        panelServidor2.add(lblEstado2);
	        panelServidor2.add(new JLabel("Tipo:"));
	        lblTipo2 = new JLabel("Desconocido");
	        panelServidor2.add(lblTipo2);

	        panelIzquierdo.add(panelServidor1);
	        panelIzquierdo.add(Box.createVerticalStrut(10));
	        panelIzquierdo.add(panelServidor2);

	        // Área de texto con scroll
	        areaTexto = new JTextArea();
	        areaTexto.setEditable(false);
	        JScrollPane scrollPane = new JScrollPane(areaTexto);

	        // Split pane
	        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelIzquierdo, scrollPane);
	        splitPane.setDividerLocation(200);
	        splitPane.setResizeWeight(0.3);

	        getContentPane().add(splitPane);
	    }

	    // Métodos públicos para actualizar desde el controlador
	    public void setEstadoServidor(int numServidor, String estado) {
	        if (numServidor == 1) {
	            lblEstado1.setText(estado);
	        } else if (numServidor == 2) {
	            lblEstado2.setText(estado);
	        }
	    }

	    public void setTipoServidor(int numServidor, String tipo) {
	        if (numServidor == 1) {
	            lblTipo1.setText(tipo);
	        } else if (numServidor == 2) {
	            lblTipo2.setText(tipo);
	        }
	    }

	    public void agregarPulso(String mensaje) {
	        areaTexto.append(mensaje + "\n");
	        areaTexto.setCaretPosition(areaTexto.getDocument().getLength()); // Scroll automático al final
	    }
	}