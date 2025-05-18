package modeloNegocio;

import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.time.LocalDateTime;

import dto.*;

import util.Util;

public class SistemaServidor {

	private ArrayList<Usuario> listaUsuarios = new ArrayList<Usuario>();
	
	private ArrayList<Usuario> listaConectados = new ArrayList<Usuario>();
	private static SistemaServidor servidor_instancia = null;
	private ArrayList<Mensaje> mensajesPendientes=new ArrayList<Mensaje>();
	private String ip;
	private int puerto;
	private Thread heartbeatThread;
	private volatile boolean heartbeatActivo = true;
	private Socket socketMonitor;
	private ObjectOutputStream oos;
	private ObjectInputStream ois;

	private volatile boolean servidorActivo = true;
	private ServerSocket serverSocket; // <-- lo haremos accesible
	private Thread serverThread;

	private SistemaServidor() {

	}

	public static SistemaServidor get_Instancia() {
		if (servidor_instancia == null)
			servidor_instancia = new SistemaServidor();
		return servidor_instancia;
	}

	private boolean existeUsuarioPorNombre(List<Usuario> lista, String nombreBuscado) {
		for (Usuario u : lista) {
			if (u.getNickName().equalsIgnoreCase(nombreBuscado)) {
				return true;
			}
		}
		return false;
	}

	private void enviaRespuestaUsuario(Solicitud usuario) {
		try (Socket socket = new Socket(usuario.getIp(), usuario.getPuerto())) {
			ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
			oos.writeObject(usuario);
			oos.flush();
			oos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void iniciaServidor(int puerto) {
		 serverThread = new Thread(() -> {
			try {
				serverSocket = new ServerSocket(puerto);
				while (servidorActivo) {
					
					Socket clienteSocket = serverSocket.accept();

					try (ObjectInputStream ois = new ObjectInputStream(clienteSocket.getInputStream())) {
						Object recibido = ois.readObject();
						
						if (recibido instanceof Solicitud) {
							Solicitud solicitud = (Solicitud) recibido;
							if (solicitud.getTipoSolicitud().equalsIgnoreCase(Util.SOLICITA_LISTA_USUARIO)) {
								retornaLista(solicitud.getIp(), solicitud.getPuerto());
							} else if (solicitud.getTipoSolicitud().equalsIgnoreCase(Util.CTEREGISTRAR)) {
								UsuarioDTO usuario = solicitud.getUsuarioDTO();
								if (registrarUsuario(usuario)) {
									solicitud.setTipoSolicitud(Util.CTEREGISTRO);
								} else {
									solicitud.setTipoSolicitud(Util.CTEUSUARIOLOGUEADO);
								}
								enviaRespuestaUsuario(solicitud);
							} else if (solicitud.getTipoSolicitud().equalsIgnoreCase(Util.CTELOGIN)) {
				
								UsuarioDTO usuario = solicitud.getUsuarioDTO();
								int tipo = loginUsuario(usuario);
								
								if (tipo == 1) {
									solicitud.setTipoSolicitud(Util.CTELOGIN);
								} else {
									// usuario Existe pero esta logueado
									if (tipo == 2) {
										solicitud.setTipoSolicitud(Util.CTEUSUARIOLOGUEADO);
									} else {
										solicitud.setTipoSolicitud(Util.CTEUSUERINEXISTENTE);
									}

								}
								enviaRespuestaUsuario(solicitud);
							}
							else {
								if(solicitud.getTipoSolicitud().equalsIgnoreCase(Util.CTEDESCONEXION)) {
									quitarUsuarioDesconectado(solicitud.getNombre());
								}else {
									if(solicitud.getTipoSolicitud().equalsIgnoreCase(Util.CTESOLICITARMENSAJES)) {
										UsuarioDTO usuario=solicitud.getUsuarioDTO();
										this.retornaListaMensajesPendientes(usuario.getIp(), usuario.getPuerto(),entregarMensajesPendientes(usuario.getNombre(),usuario.getIp(),usuario.getPuerto()));
									}
								}
							}
						}
						else {
							if(recibido instanceof Mensaje ) {
								Mensaje mensaje = (Mensaje) recibido;
								enviarMensaje(mensaje);

							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					clienteSocket.close();
				}
			} catch (Exception e) { //aca podriamos reintentar iniciar servidor
				e.printStackTrace();
				System.err.println("Error en el servidor central: " + e.getMessage());
			}
			finally {
	            detenerHeartbeat();
	        }
		});
		serverThread.start();
	}
	public boolean puertoDisponible(int puerto) {
		try (ServerSocket socket = new ServerSocket(puerto)) {
			return true; // El puerto esta  disponible
		} catch (IOException e) {
			return false; // El puerto ya esta  en uso
		}
	}
	private List<MensajeDTO> entregarMensajesPendientes(String nombre,String ip,int puerto) {
		int i=0;
		Mensaje m;
		MensajeDTO mdto;
		UsuarioDTO ureceptor;
		UsuarioDTO uemisor;
		List<MensajeDTO> listamsj=new ArrayList<MensajeDTO>();
		while(i<this.mensajesPendientes.size()) {
			m=this.mensajesPendientes.get(i);
			String nombreuser=m.getEmisor().getNickName();
			int puertouser=m.getEmisor().getPuerto();
			String ipuser=m.getEmisor().getIp();
			uemisor=new UsuarioDTO(nombreuser,puertouser,ipuser);
			nombreuser=m.getReceptor().getNickName();
			puertouser=m.getReceptor().getPuerto();
			ipuser=m.getReceptor().getIp();
			ureceptor=new UsuarioDTO(nombreuser,puertouser,ipuser);
			if(ureceptor.getNombre().equalsIgnoreCase(nombre) && ureceptor.getPuerto()==puerto && ureceptor.getIp().equalsIgnoreCase(ip)) {
				mdto=new MensajeDTO(m.getContenido(),m.getFechayhora(),uemisor,ureceptor);
				listamsj.add(mdto);
				this.mensajesPendientes.remove(i);
			}
			else {
				i++;
			}
		}
		return listamsj;
	}

	private void enviarMensaje(Mensaje mensaje) {
		try (Socket socket = new Socket(mensaje.getReceptor().getIp(), mensaje.getReceptor().getPuerto())) {
			ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
			oos.writeObject(mensaje);
			oos.flush();
			oos.close();
		} catch (IOException e) {
			this.mensajesPendientes.add(mensaje);
		}
		
	}

	private void quitarUsuarioDesconectado(String nombre) {
	    int nro = 0;
	    while (nro < this.listaConectados.size()) {
	        Usuario u = this.listaConectados.get(nro);
	        if (u.getNickName().equalsIgnoreCase(nombre)) {
	            this.listaConectados.remove(nro);
	            break; // Usuario encontrado y eliminado, salimos del bucle
	        }
	        nro++;
	    }
	}

	private void retornaListaMensajesPendientes(String ip, int puerto,List<MensajeDTO> msjPendientes) {
		try (Socket socket = new Socket(ip, puerto)) {
			ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
			oos.writeObject(msjPendientes);
			oos.flush();
			oos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private void retornaLista(String ip, int puerto) {
		try (Socket socket = new Socket(ip, puerto)) {
			ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
			oos.writeObject(obtenerListaUsuariosDTO());
			oos.flush();
			oos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private List<UsuarioDTO> obtenerListaUsuariosDTO() {
		List<UsuarioDTO> listaDTO = new ArrayList<>();
		for (Usuario u : this.listaUsuarios) {
			listaDTO.add(new UsuarioDTO(u.getNickName(), u.getPuerto(), u.getIp()));
		}
		return listaDTO;
	}

	public boolean existeUsuarioPorNombre(String nombreBuscado) {
		for (Usuario u : listaUsuarios) {
			if (u.getNickName().equalsIgnoreCase(nombreBuscado)) {
				return true;
			}
		}
		return false;
	}

	public boolean registrarUsuario(UsuarioDTO usuariodto) {
		boolean registro = true;
		if (!existeUsuarioPorNombre(usuariodto.getNombre()) && !puertoEIpEnUso(usuariodto.getIp(),usuariodto.getPuerto())) {
			Usuario usuario = new Usuario(usuariodto.getNombre(), usuariodto.getPuerto(), usuariodto.getIp());
			this.listaUsuarios.add(usuario);
			this.listaConectados.add(usuario);
		} else {
			registro = false;
		}
		return registro;
	}
	
	private boolean puertoEIpEnUso(String ip,int puerto) {
		for(Usuario u: this.listaUsuarios) {
			if(u.getIp().equalsIgnoreCase(ip) && u.getPuerto()==puerto) {
				return true;	
			}
		}
		return false;
	}

	public boolean estaConectado(String nombre) {
			for(Usuario u : this.listaConectados) {
				if(u.getNickName().equalsIgnoreCase(nombre)) {
					return true;
				}
			}
		return false;
	}

	public boolean puertoEIpCorrecto(String nickName, int puerto, String IP) {
		for (Usuario u : listaUsuarios) {
			if (u.getNickName().equalsIgnoreCase(nickName) && u.getPuerto() == puerto
					&& u.getIp().equalsIgnoreCase(IP)) {
				return true;
			}
		}
		return false;
	}

	public int loginUsuario(UsuarioDTO usuario) {
		int tipo = 1;// usuario existente pero no conectado
		String nombre = usuario.getNombre();
		String ip     = usuario.getIp();
		int puerto    = usuario.getPuerto();
		if (this.existeUsuarioPorNombre(nombre) && this.puertoEIpCorrecto(nombre, puerto,ip )) {
			if (this.estaConectado(nombre)) {
				tipo = 2;
				// usuario existe pero esta logueado
			}
		}
		else {// usuarioInexistente
			tipo = 3;
		}
		return tipo;
	}

	public void registraServidor(String ip, int puerto) {
			//obtener ip y puerto de monitor desde archivo
			try (Socket socket = new Socket(Util.IPLOCAL, Util.PUERTO_MONITOR)) { 
				this.socketMonitor=socket;
			    this.oos = new ObjectOutputStream(this.socketMonitor.getOutputStream());
			    this.oos.flush();
			    this.ois = new ObjectInputStream(this.socketMonitor.getInputStream());
			    this.puerto=puerto;
			    this.ip=ip;
			    ServidorDTO servidor = new ServidorDTO(puerto,ip);
			    System.out.println("Servidor conectado al monitor: IP remota = " + this.socketMonitor.getInetAddress().getHostAddress()
		                   + ", puerto remoto = " + this.socketMonitor.getPort()
		                   + ", puerto local = " + this.socketMonitor.getLocalPort());
			    oos.writeObject(servidor);
				oos.flush();
			    this.iniciaServidor(puerto);
			    Heartbeat();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}

	private void Heartbeat() {
		heartbeatThread = new Thread(() -> {
			while (heartbeatActivo) {
				try { 
					ServidorDTO servidor = new ServidorDTO(this.puerto, this.ip);
					oos.writeObject(servidor);
					oos.flush();
				} catch (IOException e) {
					e.printStackTrace();
					System.err.println("Error en Heartbeat: " + e.getMessage());
				}

				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					break;
				}
			}
		});
		heartbeatThread.start();
	}
	public void detenerHeartbeat() {
	    heartbeatActivo = false;
	    try {
	        if (ois != null) ois.close();
	        if (oos != null) oos.close();
	        if (socketMonitor != null) socketMonitor.close();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
	
	public void detenerServidor() {
	    servidorActivo = false;
	    try {
	        if (serverSocket != null && !serverSocket.isClosed()) {
	            serverSocket.close(); // Esto hará que serverSocket.accept() lance una excepción y termine el bucle
	        }
	        if (serverThread != null && serverThread.isAlive()) {
	            serverThread.join(); // Esperamos a que el hilo termine
	        }
	    } catch (IOException | InterruptedException e) {
	        e.printStackTrace();
	    }
	    detenerHeartbeat();
	}



	}

