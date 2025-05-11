package modeloNegocio;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Mensaje implements Serializable {
    private String contenido;
    private LocalDateTime fechayhora;
    private Usuario emisor;
    private Usuario receptor;

    public Mensaje(String contenido, LocalDateTime fechayhora, Usuario emisor, Usuario receptor) {
        super();
        this.contenido = contenido;
        this.fechayhora = fechayhora;
        this.emisor = emisor;
        this.receptor = receptor;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public LocalDateTime getFechayhora() {
        return fechayhora;
    }

    public void setFechayhora(LocalDateTime fechayhora) {
        this.fechayhora = fechayhora;
    }

    public Usuario getEmisor() {
        return emisor;
    }

    public void setEmisor(Usuario emisor) {
        this.emisor = emisor;
    }

    public Usuario getReceptor() {
        return receptor;
    }

    public void setReceptor(Usuario receptor) {
        this.receptor = receptor;
    }

    @Override
    public String toString() {
        return String.format("%s (%s): %s", emisor, fechayhora.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                contenido);
    }

}