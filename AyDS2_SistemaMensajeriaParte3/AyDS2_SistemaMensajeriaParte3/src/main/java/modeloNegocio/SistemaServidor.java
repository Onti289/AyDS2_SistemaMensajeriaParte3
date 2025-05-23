package modeloNegocio;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
	private boolean principal =false;
	private ArrayList<ServidorDTO> listaServidores = new ArrayList<ServidorDTO>();
	
	private Thread heartbeatThread;
	private volatile boolean heartbeatActivo = true;
	private Socket socketMonitor;
	private ObjectOutputStream oosMonitor;
	private ObjectInputStream oisMonitor;
	//private ObjectOutputStream oos;
	//private ObjectInputStream ois;
	private volatile boolean servidorActivo = true;
	private ServerSocket serverSocket; // <-- lo haremos accesible
	private Thread serverThread;
	private HashMap<String, ConexionUsuario> conexiones = new HashMap<>();
	private HashMap<String, ConexionServidor> conexionesServidores = new HashMap<>();
	
	private SistemaServidor() {

	}

	public static SistemaServidor get_Instancia() {
		if (servidor_instancia == null)
			servidor_instancia = new SistemaServidor();
		return servidor_instancia;
	}
	
	private void enviaRespuestaUsuario(Solicitud usuario,ObjectOutputStream oos) {
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
	                    	ObjectOutputStream oos = new ObjectOutputStream(clienteSocket.getOutputStream());
	                        oos.flush(); // importante para evitar bloqueos
	                        ObjectInputStream ois = new ObjectInputStream(clienteSocket.getInputStream());
	                        
	                        while (true) {
	                            Object recibido = ois.readObject();  
	                            System.out.println("OIS DELA LADO DEL SERVER");
	                            System.out.println(ois);
	                            System.out.println(recibido);
	                            if (recibido instanceof Solicitud) {
	                                Solicitud solicitud = (Solicitud) recibido;
	                           
	                                switch (solicitud.getTipoSolicitud()) {
	                                    case Util.SOLICITA_LISTA_USUARIO:
	                                        retornaLista(oos,this.listaUsuarios,false);
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
	                                        enviaRespuestaUsuario(solicitud,oos);
	                                        break;

	                                    case Util.CTELOGIN:
	                                        UsuarioDTO usuarioLogin = solicitud.getUsuarioDTO();
	                                        int tipo = loginUsuario(usuarioLogin);
	                                        System.out.println(tipo);
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
	                                        enviaRespuestaUsuario(solicitud,oos);
	                                        break;

	                                    case Util.CTEDESCONEXION:
	                                        quitarUsuarioDesconectado(solicitud.getNombre());
	                                        break;

	                                    case Util.CTESOLICITARMENSAJES:
	                                    	System.out.println("22"+ solicitud.getTipoSolicitud());
	                                        UsuarioDTO usuario = solicitud.getUsuarioDTO();
	                                        retornaListaMensajesPendientes(entregarMensajesPendientes(usuario.getNombre()),oos);
	                                        break;

	                                    default:
	                                        System.out.println("Tipo de solicitud no reconocido: " + solicitud.getTipoSolicitud());
	                                        break;
	                                }
	                            } else if (recibido instanceof Mensaje) {
	                                Mensaje mensaje = (Mensaje) recibido;
	                     
	                                enviarMensaje(mensaje);
	                            }             
	                            else if(recibido instanceof String) {
	                            	if(((String)recibido).equalsIgnoreCase(Util.RESINCRONIZAR)) {
	                            		this.retornaLista(oos,this.listaUsuarios,false); //lista de usuarios
	                            		this.retornaLista(oos,this.listaConectados,true); //lista de usuarios conectados
	                            		this.retornaListaMensajesAServer(oos);
	                            		
	                            	}
	                            }
	                        }

	                    } catch (Exception e) {
	                    	System.out.println("-l2");
	                    	 try {

								quitarUsuarioDesconectado(eliminaConexion(clienteSocket));
								clienteSocket.close();
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								//e1.printStackTrace();
							}
	                      //  e.printStackTrace(); // o cerrar recursos si se desea
	                    } 
	                });
	                clienteHandler.start();
	            }
	        } catch (IOException e) {
	        	System.out.println("-555");
	        	detenerServidor();
	            System.err.println("Error al iniciar el servidor: " + e.getMessage());
	        }
	    });
	    serverThread.start();
	}
	
	private void retornaListaMensajesAServer(ObjectOutputStream oos) {
		 
		try {
			oos.writeObject(mensajesPendientesDTO());
			oos.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	private RespuestaListaMensajes mensajesPendientesDTO() {
		
		List<MensajeDTO> mensajesPendientesDTO = new ArrayList<MensajeDTO>();
		for(Mensaje m :this.mensajesPendientes ) {
			UsuarioDTO emisor= new UsuarioDTO(m.getEmisor().getNickName());
			UsuarioDTO receptor=new UsuarioDTO(m.getReceptor().getNickName());
			mensajesPendientesDTO.add(new MensajeDTO(m.getContenido(),m.getFechayhora(),emisor,receptor));
		}
		
		return new RespuestaListaMensajes(mensajesPendientesDTO) ;
	}
	private void resincronizar(ServidorDTO servidor) {
		try {
			ServerSocket serverSocket = new ServerSocket(servidor.getPuerto());
			Socket socketConServerPrincipal;
			socketConServerPrincipal = serverSocket.accept();
			ObjectOutputStream oos = new ObjectOutputStream(socketConServerPrincipal.getOutputStream());
			ObjectInputStream ois = new ObjectInputStream(socketConServerPrincipal.getInputStream());
			oos.writeObject(Util.RESINCRONIZAR);
			oos.flush(); // importante para evitar bloqueos
			try {
				 Object recibido=ois.readObject();
				 if(recibido instanceof RespuestaLista) {
					 RespuestaLista respuesta=(RespuestaLista)recibido;
					 ArrayList<UsuarioDTO> listaDTO =(ArrayList<UsuarioDTO>) respuesta.getLista();
					 if(respuesta.isConectado()) {	 
						 for(UsuarioDTO usuarioDTO :listaDTO ) {
							 this.listaConectados.add(new Usuario(usuarioDTO.getNombre()));
						 }
					 }
					 else {
						 System.out.println("11");
						 for(UsuarioDTO usuarioDTO :listaDTO ) {
							 this.listaUsuarios.add(new Usuario(usuarioDTO.getNombre()));
						 }
					 }	
				 }
				 else {
					 if(recibido instanceof RespuestaListaMensajes ){
						 System.out.println("12");
						 ArrayList<MensajeDTO> listaDTO =(ArrayList<MensajeDTO>) ((RespuestaListaMensajes)recibido).getLista();
						 for(MensajeDTO msjDTO :listaDTO ) {
							 Usuario emisor= new Usuario(msjDTO.getEmisor().getNombre());
							 Usuario receptor= new Usuario(msjDTO.getReceptor().getNombre());
							 this.mensajesPendientes.add(new Mensaje(msjDTO.getContenido(),msjDTO.getFechayhora(),emisor,receptor));
						 }
					 }
				 }
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				System.out.println("13");
				e.printStackTrace();
			}
			System.out.println("Usuarios Conectados:");
			for(Usuario u :listaConectados ) {
				System.out.println(u.getNickName());
			 }
			System.out.println("Usuarios :");
			 for(Usuario u :listaUsuarios ) {
				 System.out.println(u.getNickName());
			 }
			 System.out.println("MSJ pendientes:");
			 for(Mensaje msj :mensajesPendientes ) {
				System.out.println(msj.toString());
			 }
		//	sincronizacionServerSecundario(ois);
	
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		

	}
	//Este metodo se establece luego de resincronizar y va recibiendo los msj/usuario una vez que este como secundario
	private void sincronizacionServerSecundario(ObjectInputStream ois) {
		while(!this.principal) {
			try {
				ois.readObject();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
	}

	private String eliminaConexion(Socket socketDesconectado) {
		    for (Map.Entry<String, ConexionUsuario> entry : conexionesUsuarios.entrySet()) {
		        Socket socketGuardado = entry.getValue().getSocket();

		        // Comparamos por instancia o por equals (ambos deberían funcionar bien con Socket)
		        if (socketGuardado == socketDesconectado) {
		            String nombre = entry.getKey();

		            // Cerramos conexión de manera segura
		            entry.getValue().cerrar();

		            // Removemos del mapa
		            conexionesUsuarios.remove(nombre);

		            return nombre;
		        }
		    }
		    return null; // No se encontró el socket
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
			ObjectOutputStream oosReceptor;
			System.out.println("DAtos de conexion");
			ConexionUsuario conexionUsuario=null;
			//aca recorre
			for (Map.Entry<String, ConexionUsuario> entry : conexionesUsuarios.entrySet()) {
			    String clave = entry.getKey();
			    ConexionUsuario conexion = entry.getValue();
			    if(clave.equalsIgnoreCase(mensaje.getReceptor().getNickName())) {
					 conexionUsuario=conexion;
			    }
			}

			if (conexionUsuario!=null) {
				oosReceptor=conexionUsuario.getOos();
				  try {	
					  oosReceptor.writeObject(mensaje);
					  oosReceptor.flush();
			        } catch (IOException e) {
			        	System.out.println("catch de enviar msj");
			            mensajesPendientes.add(mensaje);
			        }
			}
			else {
				System.out.println("else de enviar mensaje");
				 mensajesPendientes.add(mensaje);
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

	private void retornaListaMensajesPendientes(List<MensajeDTO> msjPendientes,ObjectOutputStream oos) {
		try{
			RespuestaListaMensajes listaRespuesta=new RespuestaListaMensajes(msjPendientes);
			oos.writeObject(listaRespuesta);
			oos.flush();
			//oos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private void retornaLista(ObjectOutputStream oos,ArrayList<Usuario> lista,boolean conectado) {
		try {
			RespuestaLista listarespuesta= new RespuestaLista(obtenerListaUsuariosDTO(lista),conectado);
			oos.writeObject(listarespuesta);
			oos.flush();
			//oos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private List<UsuarioDTO> obtenerListaUsuariosDTO(ArrayList<Usuario> lista) {
		List<UsuarioDTO> listaDTO = new ArrayList<>();
		for (Usuario u : lista) {
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
			    this.oosMonitor = new ObjectOutputStream(this.socketMonitor.getOutputStream());
			    this.oosMonitor.flush();
			    this.oisMonitor = new ObjectInputStream(this.socketMonitor.getInputStream());
			    this.puerto=puerto;
			    this.ip=ip;
			    ServidorDTO servidor = new ServidorDTO(puerto,ip);
			    System.out.println("Servidor conectado al monitor: IP remota = " + this.socketMonitor.getInetAddress().getHostAddress()
		                   + ", puerto remoto = " + this.socketMonitor.getPort()
		                   + ", puerto local = " + this.socketMonitor.getLocalPort());
			    oosMonitor.writeObject(servidor);
			    oosMonitor.flush();
			     try {
					Object recibido=oisMonitor.readObject();
					if(recibido instanceof String) { 
             	        this.principal=true; //Si llega aca  es principal
                     }
                     else if(recibido instanceof ServidorDTO) {
                     	System.out.println("9");
                     	this.principal=false;
                     	resincronizar(((ServidorDTO)recibido));
                     }
				} catch (ClassNotFoundException e) {
					System.out.println("Error aca");
					e.printStackTrace();
				}
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
					oosMonitor.writeObject(servidor);
					oosMonitor.flush();
				} catch (IOException e) {
					e.printStackTrace();
					System.err.println("Error en Heartbeat: " + e.getMessage());
				}

				try {
					Thread.sleep(200);
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
	    	System.out.println("ois "+oisMonitor==null);
	    	System.out.println("oos "+oosMonitor==null);
	    	System.out.println("Socket monitor "+socketMonitor==null);
	        if (oisMonitor != null) oisMonitor.close();
	        if (oosMonitor != null) oosMonitor.close();
	        if (socketMonitor != null) socketMonitor.close();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
	
	public void detenerServidor() {
	    servidorActivo = false;
	    System.out.println("tralalero");
	    try {
	        if (serverSocket != null && !serverSocket.isClosed()) {
	            serverSocket.close(); // Esto hará que serverSocket.accept() lance una excepción y termine el bucle
	        }
	        if (serverThread != null && serverThread.isAlive()) {
	            serverThread.join(); // Esperamos a que el hilo termine
	        }
	        detenerHeartbeat();
	    } catch (IOException | InterruptedException e) {
	        e.printStackTrace();
	    }
	    
	}



	}

