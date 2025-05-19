package modeloNegocio;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.*;
import dto.ContactoDTO;
import dto.RespuestaListaMensajes;
import dto.RespuestaListaUsuarios;
import dto.MensajeDTO;
import dto.UsuarioDTO;
import excepciones.ErrorEnvioMensajeException;
import util.Util;

public class SistemaUsuario extends Observable {
	private Usuario usuario;
	private static SistemaUsuario sistema_instancia = null;
	private int puerto_servidor;
	// Socket y flujos para comunicarse con el servidor
	private Socket socketServidor;
	private ObjectOutputStream oos;
	private ObjectInputStream ois;

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
			try {
			Solicitud solicitud = new Solicitud(
					new UsuarioDTO(this.getnickName()),
					Util.SOLICITA_LISTA_USUARIO);
			oos.writeObject(solicitud);
			oos.flush();
			}
			catch (IOException e) {
		        e.printStackTrace();
		    }
	}

	public void setUsuario(String nickname) {
		this.usuario = new Usuario(nickname);
	}

	public boolean existeContactoPorNombre(PriorityQueue<Usuario> lista, String nombreBuscado) {
		for (Usuario u : lista) {
			if (u.getNickName().equalsIgnoreCase(nombreBuscado)) {
				return true;
			}
		}
		return false;
	}

	public int agregarContacto(String nickName) {
		int condicion = 1; // contacto ya agendado
		if (!existeContactoPorNombre(this.usuario.getAgenda(), nickName)) {
			this.usuario.agregaContacto(new Usuario(nickName));
			condicion = 2;
		}
		return condicion;
	}

	public PriorityQueue<Usuario> getAgenda() {
		return this.usuario.getAgenda();
	}

	public ArrayList<MensajeDTO> getChat(String nickname) {
		return usuario.getChat(nickname);
	}

	public ArrayList<Mensaje> getMensajes() {
		return usuario.getMensajes();
	}

	public void setContactoActual(String nickname) {
		Usuario contacto = usuario.getBuscaContacto(nickname);
		if (contacto != null) {
			this.usuario.agregarConversacion(contacto);
		}
	}
	public void estableceConexion() {
		obtienePuertoServidor();
		comunicacionServidor();
	}
	public void obtienePuertoServidor() {
		try (Socket socket = new Socket(Util.IPLOCAL, Util.PUERTO_MONITOR)) {
			ObjectOutputStream oosMonitor = null;
			oosMonitor = new ObjectOutputStream(socket.getOutputStream());
			ObjectInputStream oisMonitor = new ObjectInputStream(socket.getInputStream());
			Solicitud soli = new Solicitud(Util.SOLICITA_PUERTO_SERVIDOR);
			oosMonitor.writeObject(soli);
			oosMonitor.flush();
			try {
				System.out.println(soli.getTipoSolicitud());
				this.puerto_servidor = (int) oisMonitor.readObject();	
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			oosMonitor.close();
		} catch (IOException e) {
			System.out.println("error");
		}
	}

	public void comunicacionServidor() {
		try {
			socketServidor = new Socket(Util.IPLOCAL, this.puerto_servidor);
			oos = new ObjectOutputStream(socketServidor.getOutputStream());
			oos.flush();
			ois = new ObjectInputStream(socketServidor.getInputStream());
			// Lanzar hilo receptor de mensajes del servidor
			Thread escuchaServidor = new Thread(() -> {
				try {
					while (true) {

						Object recibido = ois.readObject();

						if (recibido instanceof Mensaje) {
							Mensaje mensaje = (Mensaje) recibido;
							System.out.println("1");
							this.usuario.recibirMensaje(mensaje);
							setChanged(); // importante
							notifyObservers(mensaje);

						} else {// si llega aca es por que el server lo pudo registrar o loguear

							if (recibido instanceof Solicitud) {
								Solicitud solicitud = (Solicitud) recibido;
								System.out.println("2");
								// Si registra o loguea lo tiene que crear igual por que inicio de 0 el sistema
								// usuario
								if (solicitud.getTipoSolicitud().equalsIgnoreCase(Util.CTEREGISTRO)
										|| solicitud.getTipoSolicitud().equalsIgnoreCase(Util.CTELOGIN)) {
									setUsuario(solicitud.getNombre());
								}
								setChanged(); // importante
								notifyObservers(solicitud);
							} else {
								if (recibido instanceof RespuestaListaMensajes) {
									RespuestaListaMensajes respuesta = (RespuestaListaMensajes) recibido;
									List<MensajeDTO> mensajes = respuesta.getLista();
									System.out.println("4");
									for (MensajeDTO m : mensajes) {
										String nick = m.getEmisor().getNombre();
										int puertoaux = m.getEmisor().getPuerto();
										String ip = m.getEmisor().getIp();
										Usuario emisor = new Usuario(nick, puertoaux, ip);
										nick = m.getReceptor().getNombre();
										puertoaux = m.getReceptor().getPuerto();
										ip = m.getReceptor().getIp();
										Usuario receptor = new Usuario(nick, puertoaux, ip);
										this.usuario.recibirMensaje(
												new Mensaje(m.getContenido(), m.getFechayhora(), emisor, receptor));
									}
									setChanged(); // importante
									notifyObservers(mensajes);
								} else {
									if (recibido instanceof RespuestaListaUsuarios) {
										RespuestaListaUsuarios respuesta=(RespuestaListaUsuarios) recibido;
										List<UsuarioDTO> usuarios = respuesta.getLista();
										System.out.println("3");
										setChanged(); // importante
										notifyObservers(usuarios);
									}
								}

							}

						}
					}
				} catch (Exception e) {
					e.printStackTrace(); // conexión caída
				}
			});
			escuchaServidor.start();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/*
	public void medesconecto() {
		try {
			Solicitud soli = new Solicitud(new UsuarioDTO(this.getnickName(), this.getPuerto(), this.getIp()),
					Util.CTEDESCONEXION);
			oos.writeObject(soli);
			oos.flush();
			oos.close();
		} catch (IOException e) {
		}
	}
	 */
	private String getIp() {
		return this.usuario.getIp();
	}

	public Usuario buscarUsuarioPorDTO(UsuarioDTO dto) {
		for (Usuario u : usuario.getAgenda()) {
			if (u.getNickName().equalsIgnoreCase(dto.getNombre())) {
				return u;
			}
		}
		return null; // o lanzar excepcion si queres asegurarte que este
	}

	public Usuario getUsuario() {
		return this.usuario;
	}

	public void enviarMensajeServidor(UsuarioDTO contacto, String mensaje) {
		try {
			Usuario ureceptor = this.buscarUsuarioPorDTO(contacto);
			Mensaje msg;
			if (ureceptor != null) {
				msg = new Mensaje(mensaje, LocalDateTime.now(), this.usuario, ureceptor);
				oos.writeObject(msg);
				oos.flush();
			//	oos.close();
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

	public void enviaSolicitudAServidor(String nickName, String tipo) {

		try  {
			Solicitud soli = new Solicitud(new UsuarioDTO(nickName), tipo);
			System.out.println("HHH");
			oos.writeObject(soli);
			oos.flush();
			//oos.close();

		} catch (IOException e) {
			System.out.println("error");
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
