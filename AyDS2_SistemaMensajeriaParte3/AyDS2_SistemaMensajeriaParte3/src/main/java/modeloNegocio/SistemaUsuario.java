package modeloNegocio;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.*;
import dto.ContactoDTO;
import dto.MensajeDTO;
import dto.UsuarioDTO;
import excepciones.ErrorEnvioMensajeException;
import util.Util;

public class SistemaUsuario extends Observable {
	private Usuario usuario;
	private static SistemaUsuario sistema_instancia = null;
	private ServerSocket serverSocket;
	private volatile boolean servidorActivo;
	private int puerto_servidor;
	private SistemaUsuario() {

	}
	

	public static SistemaUsuario get_Instancia() {
		if (sistema_instancia == null)
			sistema_instancia = new SistemaUsuario();
		return sistema_instancia;
	}

	public String getnickName() {
		return this.usuario.getNickName();
	}

	public int getPuerto() {
		return this.usuario.getPuerto();
	}

	public void pedirListaUsuarios() {
		try (Socket socket = new Socket(Util.IPLOCAL, Util.PUERTO_SERVIDOR)) {
			ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
			Solicitud solicitud = new Solicitud(new UsuarioDTO(this.getnickName() ,this.getPuerto(),this.getUsuario().getIp()), Util.SOLICITA_LISTA_USUARIO);
			oos.writeObject(solicitud);
			oos.flush();
			oos.close();
		} catch (IOException e) {
		}
	}

	public void setUsuario(String nickname, int puerto, String ip) {
		this.usuario = new Usuario(nickname, puerto, ip);
	}

	public boolean existeContactoPorNombre(PriorityQueue<Usuario> lista, String nombreBuscado) {
		for (Usuario u : lista) {
			if (u.getNickName().equalsIgnoreCase(nombreBuscado)) {
				return true;
			}
		}
		return false;
	}

	public int agregarContacto(String nickName, String ip, int puerto) {
		int condicion = 1; // contacto ya agendado
		if (!existeContactoPorNombre(this.usuario.getAgenda(), nickName)) {
			this.usuario.agregaContacto(new Usuario(nickName, puerto, ip));
			condicion = 2;
		}
		return condicion;
	}

	public PriorityQueue<Usuario> getAgenda() {
		return this.usuario.getAgenda();
	}

	public ArrayList<MensajeDTO> getChat(int puerto, String ip) {
		return usuario.getChat(puerto, ip);
	}

	public ArrayList<Mensaje> getMensajes() {
		return usuario.getMensajes();
	}

	public void setContactoActual(int puerto, String ip) {
		Usuario contacto = usuario.getBuscaContacto(puerto);
		if (contacto != null) {
			this.usuario.agregarConversacion(contacto);
		}
	}

	public void iniciarServidor(int puerto) {
		Thread serverThread = new Thread(() -> {
			try {
				serverSocket = new ServerSocket(puerto);
	            servidorActivo = true;
				while (servidorActivo) {
					Socket socket = serverSocket.accept();
					try (ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())) {
						Object recibido = ois.readObject();
						if (recibido instanceof Mensaje) {
							Mensaje mensaje = (Mensaje) recibido;
							this.usuario.recibirMensaje(mensaje);
							setChanged(); // importante
							notifyObservers(mensaje);

						} else {// si llega aca es por que el server lo pudo registrar o loguear
							if (recibido instanceof Solicitud) {
								Solicitud solicitud = (Solicitud) recibido;
								//Si registra o loguea lo tiene que crear igual por que inicio de 0 el sistema usuario
								if(solicitud.getTipoSolicitud().equalsIgnoreCase(Util.CTEREGISTRO) ||solicitud.getTipoSolicitud().equalsIgnoreCase(Util.CTELOGIN)) {
									setUsuario(solicitud.getNombre(), solicitud.getPuerto(), solicitud.getIp());
								}
								setChanged(); // importante
								notifyObservers(solicitud);
							}else {
										if(recibido instanceof List<?>) {
											List<?> lista = (List<?>) recibido;
										    if (!lista.isEmpty() && lista.get(0) instanceof UsuarioDTO) {
										        List<UsuarioDTO> usuarios = (List<UsuarioDTO>) lista;
										        setChanged(); // importante
												notifyObservers(usuarios);
										    }else {
										    	if(!lista.isEmpty() && lista.get(0) instanceof MensajeDTO) {
											        List<MensajeDTO> mensajes = (List<MensajeDTO>) lista;
											        
											        for(MensajeDTO m:mensajes) {
											        	String nick= m.getEmisor().getNombre();
											        	int puertoaux=m.getEmisor().getPuerto();
											        	String ip=m.getEmisor().getIp();
											        	Usuario emisor=new Usuario(nick,puertoaux,ip);
											        	nick= m.getReceptor().getNombre();
											        	puertoaux=m.getReceptor().getPuerto();
											        	ip=m.getReceptor().getIp();
											        	Usuario receptor=new Usuario(nick,puertoaux,ip);
											        	this.usuario.recibirMensaje(new Mensaje(m.getContenido(),m.getFechayhora(),emisor,receptor));
											        }
											        setChanged(); // importante
													notifyObservers(mensajes);
										    	}
										    }
										    
										    
										}
									
								}
							
						}

					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}
					socket.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		serverThread.start();
	}

	public void medesconecto() {
		try (Socket socket = new Socket(Util.IPLOCAL, Util.PUERTO_SERVIDOR)) { 
		    ObjectOutputStream oos = null;
	
		    oos = new ObjectOutputStream(socket.getOutputStream());
		    Solicitud soli = new Solicitud (new UsuarioDTO(this.getnickName(), this.getPuerto(),this.getIp()),Util.CTEDESCONEXION);
		    oos.writeObject(soli);
		    oos.flush();
		    oos.close();
		}
		catch (IOException e) {
		}
	}
		
	

	private String getIp() {
		return this.usuario.getIp();
	}

	public Usuario buscarUsuarioPorDTO(UsuarioDTO dto) {
		for (Usuario u : usuario.getAgenda()) {
			if (u.getPuerto() == dto.getPuerto() && u.getIp() == dto.getIp()) {
				return u;
			}
		}
		return null; // o lanzar excepcion si queres asegurarte que este 
	}

	public Usuario getUsuario() {
		return this.usuario;
	}
	public void obtienePuertoServer() {
		try (Socket socket = new Socket(Util.IPLOCAL, Util.PUERTO_MONITOR)) { 
		    ObjectOutputStream oos = null;
		    oos = new ObjectOutputStream(socket.getOutputStream());
		    Solicitud soli = new Solicitud (new UsuarioDTO(nickName, puerto, ip), tipo);
		    oos.writeObject(soli);
		    oos.flush();
		    oos.close();
		    	
		}
		catch (IOException e) {
			System.out.println("error");
		}
	}
	public void enviarMensajeServidor(UsuarioDTO contacto, String mensaje) {
		try (Socket socket = new Socket(Util.IPLOCAL, this.puerto_servidor)) {
			ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
			Usuario ureceptor = this.buscarUsuarioPorDTO(contacto);
			Mensaje msg;
			if (ureceptor != null) {
				msg = new Mensaje(mensaje, LocalDateTime.now(), this.usuario, ureceptor);
				oos.writeObject(msg);
				oos.flush();
				oos.close();
				this.usuario.guardarMensaje(msg);
				setChanged(); // importante
				notifyObservers(msg);
			}

		} catch (IOException e) {
			ErrorEnvioMensajeException error = new ErrorEnvioMensajeException(
					"Error de conexion: el Servidor se encuentra desconectado");
			setChanged(); // importante
			notifyObservers(error);
		}
	}
	
	public void enviaSolicitudAServidor(String nickName, int puerto, String ip, String tipo) {
		
		try (Socket socket = new Socket(Util.IPLOCAL,this.puerto_servidor)) { 
		    ObjectOutputStream oos = null;
		    oos = new ObjectOutputStream(socket.getOutputStream());
		    Solicitud soli = new Solicitud (new UsuarioDTO(nickName, puerto, ip), tipo);
		    oos.writeObject(soli);
		    oos.flush();
		    oos.close();
		    	
		}
		catch (IOException e) {
			System.out.println("error");
		}
	}
	public void cerrarServidor() {
	    servidorActivo = false;
	    if (serverSocket != null && !serverSocket.isClosed()) {
	        try {
	            serverSocket.close();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
	}
	public int buscaPuerto() {
		int puerto=1027;
		while(!this.puertoDisponible(puerto)) {
			puerto++;
		}
		return puerto;
	}
	public boolean puertoDisponible(int puerto) {
		try (ServerSocket socket = new ServerSocket(puerto)) {
			socket.setReuseAddress(true);
			return true; // El puerto est  disponible
		} catch (IOException e) {
			return false; // El puerto ya est  en uso
		}
	}

	public List<Usuario> getListaConversaciones() {
		return this.usuario.getListaConversaciones();
	}

	public String getAlias(int puerto) {
		PriorityQueue<Usuario> lista = this.usuario.getAgenda();
		while (!lista.isEmpty()) {
			Usuario contacto = lista.poll();
			if (contacto.getPuerto() == puerto) {
				return contacto.getNickName();
			}
		}
		return null;
	}

}
