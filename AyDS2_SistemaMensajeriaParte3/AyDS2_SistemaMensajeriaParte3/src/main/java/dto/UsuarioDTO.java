package dto;

import java.io.Serializable;
import java.util.Objects;

public class UsuarioDTO implements Serializable {
    private String nombre;
    private int puerto;
    private String ip;
/*
    public UsuarioDTO(String nombre, int puerto, String ip) {
        super();
        this.nombre = nombre;
        this.puerto = puerto;
        this.ip = ip;
    }*/
    public UsuarioDTO(String nombre) {
    	this.nombre = nombre;
    	this.puerto = 0;
        this.ip = null;
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

    @Override
    public String toString() {
        return nombre;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        UsuarioDTO other = (UsuarioDTO) obj;
        return nombre.equalsIgnoreCase(other.nombre);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ip, puerto);
    }

}