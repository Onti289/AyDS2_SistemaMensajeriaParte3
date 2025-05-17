package modeloNegocio;

import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Observable;

import dto.ServidorDTO;
import dto.UsuarioDTO;
import util.Util;

public class SistemaMonitor extends Observable{
	ArrayList<ServidorDTO> listaServidores=new ArrayList<ServidorDTO>();
	private static SistemaMonitor monitor_instancia = null;

	public SistemaMonitor() {
		
	}
	public static SistemaMonitor get_Instancia() {
		if (monitor_instancia == null)
			monitor_instancia = new SistemaMonitor();
		return monitor_instancia;
	}
	public void agregaServidor(ServidorDTO servidor) {
		this.listaServidores.add(servidor); 
	}//primer parametro siempre es true ya que si sea agrega servidor es por que esta en linea
	
	//este metodo no lo saca del arreglo solo cambia el estado a fuera de linea
	public void eliminaServidor(int puerto,String ip) {
		int i=0;
		// ip llega como parametro pero no se trabaja por que es todo local, lo dejamos por que es escalable y si
		//en un futuro se trabaja con ip distintas se analizara
		while(i<this.listaServidores.size() && this.listaServidores.get(i).getPuerto()!=puerto) {
			i++;
		}
		if(i<this.listaServidores.size()) {
			this.listaServidores.get(i).setEnLinea(false);
			if((this.listaServidores.get(i).isPrincipal() )) {
				int pos=buscaPrimerServidorRedundante();
				if(pos<this.listaServidores.size()){
					this.listaServidores.get(pos).setPrincipal(true);
				}
			}
			this.listaServidores.get(i).setPrincipal(false);
		}
		
	}
	private int buscaPrimerServidorRedundante() {
		int i=0;
		//cuando se llama a este metodo todos los servidores son redundantes  ya que primero se cambiaEstadoTipoServidor osea
		// saca server por lo solo basta con fijarse el primer server que este en linea
		while(i<this.listaServidores.size() && !this.listaServidores.get(i).isEnLinea())
			i++;
		return i;
	}
	private boolean hayServidorEnlinea() {
		int i=0;
		while(i<this.listaServidores.size() && !this.listaServidores.get(i).isEnLinea())
			i++;
		if(i<this.listaServidores.size())
			return true;
		else
			return false;
	}

	public void inicia() {
		Thread serverThread = new Thread(() -> {
			try (ServerSocket serverSocket = new ServerSocket(Util.PUERTO_MONITOR)) {
				controlaPulsos();
				while (true) {
					Socket servidorSocket = serverSocket.accept();
					try (ObjectInputStream ois = new ObjectInputStream(servidorSocket.getInputStream())) {
						Object recibido = ois.readObject();
						if(recibido instanceof ServidorDTO)	{
							ServidorDTO servidorDTO = (ServidorDTO) recibido;
							int pos=this.posServidor(servidorDTO);
							
							if(pos==-1){//si esta vacio es el primer servidor
								servidorDTO.setEnLinea(true);
								servidorDTO.setPrincipal(true);
								servidorDTO.setNro(1);
								this.agregaServidor(servidorDTO);
							}
							else {
								if(pos<this.listaServidores.size()) { //servidor que llega se agrego al menos una vez
									if(!this.listaServidores.get(pos).isEnLinea()) {	
										if(!hayServidorEnlinea()) { //si no hay servidores en linea pasas a ser el principal
											this.listaServidores.get(pos).setPrincipal(true);
										}
										this.listaServidores.get(pos).setEnLinea(true);
									}
									else {
										this.listaServidores.get(pos).setPulso(true);
									}
									servidorDTO.setEnLinea(true);
									servidorDTO.setPrincipal(this.listaServidores.get(pos).isPrincipal());
									servidorDTO.setNro(pos+1);
								}
								else { //si llega aca es un nuevo servidor 
									servidorDTO.setEnLinea(true);
									servidorDTO.setPrincipal(false);
									servidorDTO.setNro(this.listaServidores.size()+1);
									this.agregaServidor(servidorDTO);
								}
							}
							
							setChanged(); // importante
							notifyObservers(servidorDTO);
						}
							
					} catch (Exception e) {
						e.printStackTrace();
					}
					servidorSocket.close();
				}
			} catch (Exception e) {
				System.err.println("Error en el monitor: " + e.getMessage());
			}
		});
		serverThread.start();
	}
	private void controlaPulsos() {
		//posible mejora que chequee 2 pulsos
		Thread thread = new Thread(() -> {
			while (true) {
				synchronized (listaServidores) {  // Sincronizar acceso concurrente
					Iterator<ServidorDTO> it = listaServidores.iterator();
					while (it.hasNext()) {
						ServidorDTO s = it.next();
						if(s.isEnLinea()) {
							if ( !s.isPulso()) {
								int puerto = s.getPuerto();
								String ip = s.getIp();
								eliminaServidor(puerto, ip);
								setChanged();
								notifyObservers(s);
							} else {
								s.setPulso(false); // Resetear para próximo ciclo
							}
						}
					}
				}
				try {
					Thread.sleep(3000); // Verifica cada 3 segundos
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					break;
				}
			}
		});
		thread.start();
	}

	private int posServidor(ServidorDTO servidorDTO) {
		int i=0;
		if(this.listaServidores.isEmpty()) {
			i=-1; //esta vacio
		}
		else {
			while(i<this.listaServidores.size() && (!this.listaServidores.get(i).getIp().equalsIgnoreCase(servidorDTO.getIp()) || this.listaServidores.get(i).getPuerto()!=servidorDTO.getPuerto())) {
				i++;
			}
		}
		return i;
	}
	
	
}
