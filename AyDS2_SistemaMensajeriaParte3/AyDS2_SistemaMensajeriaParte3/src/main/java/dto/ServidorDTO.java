package dto;

import java.io.Serializable;

public class ServidorDTO implements Serializable {
	private int puerto;
	private String ip;
	public ServidorDTO(int puerto, String ip) {
		this.puerto = puerto;
		this.ip = ip;
	}
	public int getPuerto() {
		return puerto;
	}
	public String getIp() {
		return ip;
	}
	
}
