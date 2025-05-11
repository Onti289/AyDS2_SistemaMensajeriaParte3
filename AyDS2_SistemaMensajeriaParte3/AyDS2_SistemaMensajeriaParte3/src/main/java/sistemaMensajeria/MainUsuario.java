package sistemaMensajeria;

import controlador.ControladorUsuario;
import modeloNegocio.SistemaUsuario;
import vistas.*;

public class MainUsuario {

    public static void main(String[] args) {
        SistemaUsuario sMensajeria = SistemaUsuario.get_Instancia();
        ControladorUsuario controlador = new ControladorUsuario(sMensajeria);

    }

}