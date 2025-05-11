package dto;

import java.io.Serializable;
import java.time.LocalDateTime;

import modeloNegocio.Usuario;

public class MensajeDTO implements Serializable {
    private String contenido;
    private LocalDateTime fechayhora;
    private UsuarioDTO emisor;
    private UsuarioDTO receptor;

    public MensajeDTO(String contenido, LocalDateTime fechayhora, UsuarioDTO emisor, UsuarioDTO receptor) {
        super();
        this.contenido = contenido;
        this.fechayhora = fechayhora;
        this.emisor = emisor;
        this.receptor = receptor;
    }

    public String getContenido() {
        return contenido;
    }

    public LocalDateTime getFechayhora() {
        return fechayhora;
    }

    public UsuarioDTO getEmisor() {
        return emisor;
    }

    public UsuarioDTO getReceptor() {
        return receptor;
    }

}