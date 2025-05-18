package modeloNegocio;

import java.io.Serializable;

import dto.UsuarioDTO;

public class Solicitud implements Serializable {
    private String tipoSolicitud; // Ej: "OBTENER_USUARIOS"
    private UsuarioDTO usuariodto;

    public Solicitud(UsuarioDTO u,String tipo) {
        this.tipoSolicitud = tipo;
        this.usuariodto = u;
    }
    public Solicitud(String tipo) {
        this.tipoSolicitud = tipo;
    }
    public String getNombre() {
        return this.usuariodto.getNombre();
    }
    public int getPuerto() {
        return this.usuariodto.getPuerto();
    }

    public String getIp() {
        return this.usuariodto.getIp();
    }

    public UsuarioDTO getUsuarioDTO(){
        return this.usuariodto;
    }

    public String getTipoSolicitud() {
        return tipoSolicitud;
    }
    public void setTipoSolicitud(String solicitud) {
        this.tipoSolicitud=solicitud;
    }
}