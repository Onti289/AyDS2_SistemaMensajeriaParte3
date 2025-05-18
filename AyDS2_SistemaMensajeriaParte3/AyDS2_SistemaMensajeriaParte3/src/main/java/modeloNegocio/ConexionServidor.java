package modeloNegocio;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import dto.ServidorDTO;

public class ConexionServidor {
	private ServidorDTO servidor;
	private ObjectOutputStream oos;
	private ObjectInputStream ois;
	private Socket socket;
	public ConexionServidor(ServidorDTO servidor, ObjectOutputStream oos, ObjectInputStream ois, Socket socket) {
		super();
		this.servidor = servidor;
		this.oos = oos;
		this.ois = ois;
		this.socket = socket;
	}
	public ServidorDTO getServidor() {
		return servidor;
	}
	public ObjectOutputStream getOos() {
		return oos;
	}
	public ObjectInputStream getOis() {
		return ois;
	}
	public Socket getSocket() {
		return socket;
	}
	public void cerrar() {
	    try {
	        if (ois != null) {
	            ois.close();
	        }
	    } catch (IOException e) {
	        System.err.println("Error al cerrar ObjectInputStream: " + e.getMessage());
	    }

	    try {
	        if (oos != null) {
	            oos.close();
	        }
	    } catch (IOException e) {
	        System.err.println("Error al cerrar ObjectOutputStream: " + e.getMessage());
	    }

	    try {
	        if (socket != null && !socket.isClosed()) {
	            socket.close();
	        }
	    } catch (IOException e) {
	        System.err.println("Error al cerrar Socket: " + e.getMessage());
	    }
	}

}
