package vistas;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JFrame;
import javax.swing.JPanel;
import util.Util;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.Box;
import javax.swing.BoxLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;

public class VentanaInicial extends JFrame implements IVista, ActionListener {

    private JButton botonRegistro;
    private JButton botonInicioSesion;

    public VentanaInicial() {
    	setResizable(false);

        setTitle("Sistema de Mensajeria");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(300, 250);
        setLocationRelativeTo(null);

        JPanel panelPrincipal = new JPanel();
        panelPrincipal.setLayout(new BoxLayout(panelPrincipal, BoxLayout.Y_AXIS));
        getContentPane().add(panelPrincipal, BorderLayout.CENTER);

        JLabel titulo = new JLabel("Sistema de Mensajeria", JLabel.CENTER);
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        titulo.setFont(new Font("Arial", Font.BOLD, 16));
        panelPrincipal.add(Box.createVerticalStrut(20)); // Espaciado superior
        panelPrincipal.add(titulo);
        panelPrincipal.add(Box.createVerticalStrut(20)); // Separación después del título

        // Panel para botón de Registro
        JPanel panelBotonRegistro = new JPanel();
        botonRegistro = new JButton(Util.CTEREGISTRO);
        botonRegistro.setPreferredSize(new Dimension(150, 30)); // Botón más chico
        panelBotonRegistro.add(botonRegistro);
        panelPrincipal.add(panelBotonRegistro);

        panelPrincipal.add(Box.createVerticalStrut(10)); // Separación entre botones

        // Panel para botón de Inicio de Sesión
        JPanel panelBotonInicioSesion = new JPanel();
        botonInicioSesion = new JButton(Util.CTEINICIARSESION);
        botonInicioSesion.setPreferredSize(new Dimension(150, 30)); // Botón más chico
        panelBotonInicioSesion.add(botonInicioSesion);
        panelPrincipal.add(panelBotonInicioSesion);

        panelPrincipal.add(Box.createVerticalGlue()); // Para que quede bien centrado todo
    }

    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
    }

    @Override
    public void setActionListener(ActionListener controlador) {
        botonRegistro.addActionListener(controlador);
        botonInicioSesion.addActionListener(controlador);
    }

    @Override
    public void dispose() {
        super.dispose();
    }
    public void mostrarErrorServidoresCaidos(String error) {

	}
    @Override
    public void actionPerformed(ActionEvent e) {
        // Lógica para manejar eventos si es necesario
    }
}