package dto;

import java.io.Serializable;

public class ContactoDTO implements Serializable {
    private String nombre;
    private int puerto;
    private String ip;

    public ContactoDTO(String nombre, int puerto, String ip) {
        super();
        this.nombre = nombre;
        this.puerto = puerto;
        this.ip = ip;
    }

    public String getNombre() {
        return nombre;
    }

    public String getIp() {
        return ip;
    }

    public int getPuerto() {
        return puerto;
    }
}