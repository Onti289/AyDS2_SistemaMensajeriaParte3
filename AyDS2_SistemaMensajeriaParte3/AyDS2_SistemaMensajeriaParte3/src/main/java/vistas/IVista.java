package vistas;

import java.awt.event.ActionListener;

import controlador.ControladorUsuario;

public interface IVista {

    void setVisible(boolean b);

    void setActionListener(ActionListener controlador);

    void dispose();

}