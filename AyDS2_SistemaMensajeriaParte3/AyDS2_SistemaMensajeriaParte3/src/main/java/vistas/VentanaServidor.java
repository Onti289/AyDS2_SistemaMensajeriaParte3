package vistas;

import javax.swing.*;

import modeloNegocio.SistemaServidor;

import java.awt.*;
import java.awt.event.ActionListener;

public class VentanaServidor extends JFrame {

    private JLabel etiquetaEstado;
    public VentanaServidor() {
        setTitle("Servidor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // No cerrar directamente
        setSize(300, 200);
        setLocationRelativeTo(null); // Centrar la ventana

        JPanel panelPrincipal = new JPanel();
        panelPrincipal.setLayout(new GridBagLayout());
        getContentPane().add(panelPrincipal);

        etiquetaEstado = new JLabel("Servidor en funcionamiento");
        etiquetaEstado.setFont(new Font("Arial", Font.PLAIN, 14));
        panelPrincipal.add(etiquetaEstado);

        setVisible(true);
    }

    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
    }


    @Override
    public void dispose() {
        super.dispose();
    }
}