package modeloNegocio;

import java.util.ArrayList;
import java.util.HashMap;
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
	private HashMap<String, ConexionUsuario> conexionesUsuarios = new HashMap<>();
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
	
	private void enviaRespuestaUsuario(Solicitud usuario) {
		try {
			oos.writeObject(usuario);
			oos.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void iniciaServidor(int puerto) {
	    servidorActivo = true;
	    
	    serverThread = new Thread(() -> {
	        try {
	        	this.serverSocket=new ServerSocket(puerto);
	            while (servidorActivo) {
	                Socket clienteSocket = serverSocket.accept();
	                Thread clienteHandler = new Thread(() -> {
	                    try {
	                        oos = new ObjectOutputStream(clienteSocket.getOutputStream());
	                        oos.flush(); // importante para evitar bloqueos
	                        ois = new ObjectInputStream(clienteSocket.getInputStream());
	                        
	                        while (true) {
	                            Object recibido = ois.readObject();
	                            
	                            if (recibido instanceof Solicitud) {
	                                Solicitud solicitud = (Solicitud) recibido;
	                           
	                                switch (solicitud.getTipoSolicitud()) {
	                                    case Util.SOLICITA_LISTA_USUARIO:
	                                        retornaLista();
	                                        break;

	                                    case Util.CTEREGISTRAR:
	                                        UsuarioDTO usuarioReg = solicitud.getUsuarioDTO();
	                                        if (registrarUsuario(usuarioReg)) {
	                                            solicitud.setTipoSolicitud(Util.CTEREGISTRO);
	                                            String clave = usuarioReg.getNombre();
	                                            ConexionUsuario conexion = new ConexionUsuario(usuarioReg, oos, ois, clienteSocket);
	                                            conexionesUsuarios.put(clave, conexion);
	                                        } else {
	                                            solicitud.setTipoSolicitud(Util.CTEUSUARIOLOGUEADO);
	                                        }
	                                        enviaRespuestaUsuario(solicitud);
	                                        break;

	                                    case Util.CTELOGIN:
	                                        UsuarioDTO usuarioLogin = solicitud.getUsuarioDTO();
	                                        int tipo = loginUsuario(usuarioLogin);
	                                        if (tipo == 1) {
	                                            solicitud.setTipoSolicitud(Util.CTELOGIN);
	                                            String clave = usuarioLogin.getNombre();
	                                            ConexionUsuario conexion = new ConexionUsuario(usuarioLogin, oos, ois, clienteSocket);
	                                            conexionesUsuarios.put(clave, conexion);
	                                        } else if (tipo == 2) {
	                                            solicitud.setTipoSolicitud(Util.CTEUSUARIOLOGUEADO);
	                                        } else {
	                                            solicitud.setTipoSolicitud(Util.CTEUSUERINEXISTENTE);
	                                        }
	                                        enviaRespuestaUsuario(solicitud);
	                                        break;

	                                    case Util.CTEDESCONEXION:
	                                        quitarUsuarioDesconectado(solicitud.getNombre());
	                                        break;

	                                    case Util.CTESOLICITARMENSAJES:
	                                    	System.out.println("22"+ solicitud.getTipoSolicitud());
	                                        UsuarioDTO usuario = solicitud.getUsuarioDTO();
	                                        retornaListaMensajesPendientes(entregarMensajesPendientes(usuario.getNombre()));
	                                        break;

	                                    default:
	                                        System.out.println("Tipo de solicitud no reconocido: " + solicitud.getTipoSolicitud());
	                                        break;
	                                }
	                            } else if (recibido instanceof Mensaje) {
	                                Mensaje mensaje = (Mensaje) recibido;
	                                enviarMensaje(mensaje);
	                            }
	                        }

	                    } catch (Exception e) {
	                        e.printStackTrace(); // o cerrar recursos si se desea
	                    } finally {
	                        try {
	                            clienteSocket.close();
	                        } catch (IOException e) {
	                            e.printStackTrace();
	                        }
	                    }
	                });
	                clienteHandler.start();
	            }
	        } catch (IOException e) {
	        	e.printStackTrace();
	        	detenerServidor();
	            System.err.println("Error al iniciar el servidor: " + e.getMessage());
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
	private List<MensajeDTO> entregarMensajesPendientes(String nombre) {
		int i=0;
		Mensaje m;
		MensajeDTO mdto;
		UsuarioDTO ureceptor;
		UsuarioDTO uemisor;
		List<MensajeDTO> listamsj=new ArrayList<MensajeDTO>();
		while(i<this.mensajesPendientes.size()) {
			m=this.mensajesPendientes.get(i);
			String nombreuser=m.getEmisor().getNickName();
			uemisor=new UsuarioDTO(nombreuser);
			nombreuser=m.getReceptor().getNickName();
			ureceptor=new UsuarioDTO(nombreuser);
			if(ureceptor.getNombre().equalsIgnoreCase(nombre)) {
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
		try {
			oos.writeObject(mensaje);
			oos.flush();
		//	oos.close();
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
	            conexionesUsuarios.remove(nombre);
	            break; // Usuario encontrado y eliminado, salimos del bucle
	        }
	        nro++;
	    }
	}

	private void retornaListaMensajesPendientes(List<MensajeDTO> msjPendientes) {
		try{
			RespuestaListaMensajes listaRespuesta=new RespuestaListaMensajes(msjPendientes);
			oos.writeObject(listaRespuesta);
			oos.flush();
			//oos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private void retornaLista() {
		try {
			oos.writeObject(obtenerListaUsuariosDTO());
			oos.flush();
			//oos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private List<UsuarioDTO> obtenerListaUsuariosDTO() {
		List<UsuarioDTO> listaDTO = new ArrayList<>();
		for (Usuario u : this.listaUsuarios) {
			listaDTO.add(new UsuarioDTO(u.getNickName()));
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
		if (!existeUsuarioPorNombre(usuariodto.getNombre())) {
			Usuario usuario = new Usuario(usuariodto.getNombre());
			this.listaUsuarios.add(usuario);
			this.listaConectados.add(usuario);
		} else {
			registro = false;
		}
		return registro;
	}
	

	public boolean estaConectado(String nombre) {
			for(Usuario u : this.listaConectados) {
				if(u.getNickName().equalsIgnoreCase(nombre)) {
					return true;
				}
			}
		return false;
	}
	public int loginUsuario(UsuarioDTO usuario) {
		int tipo = 1;// usuario existente pero no conectado
		String nombre = usuario.getNombre();
		if (this.existeUsuarioPorNombre(nombre)) {
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
		
			try { 
				this.socketMonitor=new Socket(Util.IPLOCAL, Util.PUERTO_MONITOR);
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

