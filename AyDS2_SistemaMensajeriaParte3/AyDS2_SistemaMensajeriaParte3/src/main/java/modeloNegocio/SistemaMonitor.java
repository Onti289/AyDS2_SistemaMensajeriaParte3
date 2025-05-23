package modeloNegocio;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Observable;

import dto.ServidorDTO;
import dto.UsuarioDTO;
import util.Util;

public class SistemaMonitor extends Observable {
	private ArrayList<ServidorDTO> listaServidores = new ArrayList<ServidorDTO>();
	private static SistemaMonitor monitor_instancia = null;
	private HashMap<String, ConexionServidor> conexiones = new HashMap<>();
	private ServerSocket serverSocketMonitor; 
	
	private SistemaMonitor() {

	}

	public static SistemaMonitor get_Instancia() {
		if (monitor_instancia == null)
			monitor_instancia = new SistemaMonitor();
		return monitor_instancia;
	}

	public void agregaServidor(ServidorDTO servidor) {
		this.listaServidores.add(servidor);
	}// primer parametro siempre es true ya que si sea agrega servidor es por que
		// esta en linea

	// este metodo no lo saca del arreglo solo cambia el estado a fuera de linea
	public void eliminaServidor(int puerto, String ip) {
		int i = 0;
		// ip llega como parametro pero no se trabaja por que es todo local, lo dejamos
		// por que es escalable y si
		// en un futuro se trabaja con ip distintas se analizara
		while (i < this.listaServidores.size() && this.listaServidores.get(i).getPuerto() != puerto) {
			i++;
		}
		if (i < this.listaServidores.size()) {
			this.listaServidores.get(i).setEnLinea(false);
			if ((this.listaServidores.get(i).isPrincipal())) {
				int pos = buscaPrimerServidorRedundante();
				if (pos < this.listaServidores.size()) {
					this.listaServidores.get(pos).setPrincipal(true);
				}
			}
			this.listaServidores.get(i).setPrincipal(false);
		}

	}

	private int buscaPrimerServidorRedundante() {
		int i = 0;
		// cuando se llama a este metodo todos los servidores son redundantes ya que
		// primero se cambiaEstadoTipoServidor osea
		// saca server por lo solo basta con fijarse el primer server que este en linea
		while (i < this.listaServidores.size() && !this.listaServidores.get(i).isEnLinea())
			i++;
		return i;
	}

	private boolean hayServidorEnlinea() {
		int i = 0;
		while (i < this.listaServidores.size() && !this.listaServidores.get(i).isEnLinea())
			i++;
		if (i < this.listaServidores.size())
			return true;
		else
			return false;
	}

	public void inicia() {
		Thread serverThread = new Thread(() -> {
			try {
				serverSocketMonitor = new ServerSocket(Util.PUERTO_MONITOR);
				controlaPulsos(); // esto debería ir en un hilo separado si es bloqueante
			
				while (true) {
					Socket servidorSocket = serverSocketMonitor.accept();
					Socket finalServidorSocket = servidorSocket;
			
					Thread handlerThread = new Thread(() -> {
						try {
							
							ObjectOutputStream oos = new ObjectOutputStream(finalServidorSocket.getOutputStream());
							oos.flush();
							ObjectInputStream ois = new ObjectInputStream(finalServidorSocket.getInputStream());
							while (true) {
								Object recibido = ois.readObject();
								
								if (recibido instanceof ServidorDTO) {
									ServidorDTO servidorDTO = (ServidorDTO) recibido;
									
									int pos = this.posServidor(servidorDTO);
									servidorDTO.setEnLinea(true);
									
									if (pos == -1) {// si esta vacio es el primer servidor
									
										servidorDTO.setPrincipal(true);
										servidorDTO.setNro(1);
										this.agregaServidor(servidorDTO);
										System.out.println("Primero");
										oos.writeObject(Util.SOS_PRINCIPAL);
										System.out.println("se envio");
										oos.flush();
									} else {
										if (!hayServidorEnlinea()) { // si no hay servidores en linea pasas a ser el principal
											
											servidorDTO.setPrincipal(true);
											if(pos<this.listaServidores.size()) {
												this.listaServidores.get(pos).setPrincipal(true);	
											}
											else {
												this.agregaServidor(servidorDTO);
											}
											oos.flush();
										}
										if (pos < this.listaServidores.size()) { // servidor que llega se agrego al
																				// menos una vez
											if (!this.listaServidores.get(pos).isEnLinea()) {	//si no esta en linea
												this.listaServidores.get(pos).setEnLinea(true);
												servidorDTO.setEnLinea(true);
												ServidorDTO s=buscaServerPrincipal();
												oos.writeObject(s);
												oos.flush();
											} else { //entra si le manda pulso
												servidorDTO.setPulso(true);
												this.listaServidores.get(pos).setPulso(true);
												if(this.listaServidores.get(pos).isPrincipal()) {
													servidorDTO.setPrincipal(true);
												}
												
											}
											servidorDTO.setNro(pos + 1);
										} else { // si llega aca es un nuevo servidor
											servidorDTO.setEnLinea(true);
											servidorDTO.setNro(this.listaServidores.size() + 1);
											this.agregaServidor(servidorDTO);
											ServidorDTO s=buscaServerPrincipal();
											oos.writeObject(s);
											oos.flush();
										}
									}
									if(pos==-1 || pos > this.listaServidores.size() || !this.listaServidores.get(pos).isEnLinea()){ //si esta vacio o no esta en linea es no esta en lista es un nuevo server por lo cual nueva conexion
										String clave = generarClave(servidorDTO.getIp(), servidorDTO.getPuerto());
										ConexionServidor conexion = new ConexionServidor(servidorDTO, oos, ois,finalServidorSocket);
										conexiones.put(clave, conexion);	
									}
									setChanged(); // importante
									notifyObservers(servidorDTO);
								} else {
									if (recibido instanceof Solicitud) {
										Solicitud solicitud = (Solicitud) recibido;
										if (solicitud.getTipoSolicitud().equalsIgnoreCase(Util.SOLICITA_PUERTO_SERVIDOR)) {
											oos.writeObject(buscaPuertoServidorPrincipal());
											oos.flush();
										}
										finalServidorSocket.close();//como el cliente solo consulta con monitor no necesita mantener ese socket abierto
									}
								}

							}

						} catch (Exception e) {
							
						} 
					});
					handlerThread.start();
				}
			} catch (Exception e) {
				System.err.println("Error en el monitor: " + e.getMessage());
			}
		});
		serverThread.start();
	}
	private ServidorDTO buscaServerPrincipal() {
		String ipPrincipal=buscaIpServidorPrincipal();
		int puertoPrincipal=buscaPuertoServidorPrincipal();
		
		
		return new ServidorDTO(puertoPrincipal,ipPrincipal);
	}

	private String generarClave(String ip, int puerto) {
	    return ip + ":" + puerto;
	}

	private int buscaPuertoServidorPrincipal() {
		int puerto;
		int i = 0;
		while (i < this.listaServidores.size() && !this.listaServidores.get(i).isPrincipal()) {
			i++;
		}
		if (i < this.listaServidores.size()) {
			puerto = this.listaServidores.get(i).getPuerto();
		} else {
			puerto = -1;
		}
		return puerto;
	}
	private String buscaIpServidorPrincipal() {
		String ip;
		int i = 0;
		while (i < this.listaServidores.size() && !this.listaServidores.get(i).isPrincipal()) {
			i++;
		}
		if (i < this.listaServidores.size()) {
			ip = this.listaServidores.get(i).getIp();
		} else {
			ip =null;
		}
		return ip;
	}
	private void controlaPulsos() {
		// posible mejora que chequee 2 pulsos
		Thread thread = new Thread(() -> {
			while (true) {
				synchronized (listaServidores) { // Sincronizar acceso concurrente
					Iterator<ServidorDTO> it = listaServidores.iterator();
					while (it.hasNext()) {
						ServidorDTO s = it.next();
						
						if (s.isEnLinea()) {
							System.out.println("entraaaw");
							if (!s.isPulso()) {				
								int puerto = s.getPuerto();
								String ip = s.getIp();
								eliminaServidor(puerto, ip);
								String clave = generarClave(ip, puerto);
								conexiones.remove(clave);
								setChanged();
								notifyObservers(s);
							} else {
								s.setPulso(false); // Resetear para próximo ciclo
							}
						}
					}
				}
				try {
					Thread.sleep(300); // Verifica cada 3 segundos
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					break;
				}
			}
		});
		thread.start();
	}

	private int posServidor(ServidorDTO servidorDTO) {
		int i = 0;
		if (this.listaServidores.isEmpty()) {
			i = -1; // esta vacio
		} else {
			while (i < this.listaServidores.size() && (!this.listaServidores.get(i).getIp().equalsIgnoreCase(servidorDTO.getIp())
							|| this.listaServidores.get(i).getPuerto() != servidorDTO.getPuerto())) {
				i++;
			}
		}
		return i;
	}

}
