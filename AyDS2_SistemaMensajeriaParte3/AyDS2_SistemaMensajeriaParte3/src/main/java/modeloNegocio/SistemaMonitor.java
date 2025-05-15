package modeloNegocio;

import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Observable;

import dto.ServidorDTO;
import dto.UsuarioDTO;
import util.Util;

public class SistemaMonitor extends Observable{
	ArrayList<Servidor> listaServidores=new ArrayList<Servidor>();
	private static SistemaMonitor monitor_instancia = null;
	/*redundancia pasiva
	heartbeat
	monitor
	resincronizacion de estados*/
	public SistemaMonitor() {
		
	}
	public static SistemaMonitor get_Instancia() {
		if (monitor_instancia == null)
			monitor_instancia = new SistemaMonitor();
		return monitor_instancia;
	}
	public void agregaServidor(Servidor servidor) {
		this.listaServidores.add(servidor); 
	}//primer parametro siempre es true ya que si sea agrega servidor es por que esta en linea
	
	//este metodo no lo saca del arreglo solo cambia el estado a fuera de linea
	public void eliminaServidor(int puerto,String ip) {
		int i=0;
		while(i<this.listaServidores.size() && this.listaServidores.get(i).puerto!=puerto && this.listaServidores.get(i).ip.equalsIgnoreCase(ip)) {
			i++;
		}
		if(i<this.listaServidores.size()) {
			this.cambiaEstadoTipoServidor(i, false, false);//siempre setea false en Esprincipal sea o no principal
		}
		if(buscaPrimerServidorRedundante()<this.listaServidores.size()) {
			this.cambiaEstadoTipoServidor(i, true, true);
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
	public void cambiaEstadoTipoServidor(int pos,boolean Enlinea,boolean Esprincipal) {
		this.listaServidores.get(pos).setEnLinea(Enlinea);
		this.listaServidores.get(pos).setPrincipal(Esprincipal);
	}
	public void inicia() {
		Thread serverThread = new Thread(() -> {
			try (ServerSocket serverSocket = new ServerSocket(Util.PUERTO_MONITOR)) {
				while (true) {
					Socket servidorSocket = serverSocket.accept();
					try (ObjectInputStream ois = new ObjectInputStream(servidorSocket.getInputStream())) {
						Object recibido = ois.readObject();
						if(recibido instanceof ServidorDTO)	{
							ServidorDTO servidorDTO = (ServidorDTO) recibido;
							Servidor servidor;
							int pos=this.posServidor(servidorDTO);
							
							if(pos==0){//si esta vacio es el primer servidor
								servidor=new Servidor(true,true,servidorDTO.getPuerto(),servidorDTO.getIp(),1);
								this.agregaServidor(servidor);
							}
							else {
								if(pos<this.listaServidores.size()) { //servidor que llega se agrego al menos una vez
									if(!this.listaServidores.get(pos).isEnLinea()) {	
										if(!hayServidorEnlinea()) { //si no hay servidores en linea pasas a ser el principal
											this.listaServidores.get(pos).setPrincipal(true);
										}
										this.listaServidores.get(pos).setEnLinea(true);
										
									}
									servidor=new Servidor(true,this.listaServidores.get(pos).principal,servidorDTO.getPuerto(),servidorDTO.getIp(),pos+1);					
								}
								else { //si llega aca es un nuevo servidor 
									System.out.println("Legooo");
									servidor=new Servidor(true,false,servidorDTO.getPuerto(),servidorDTO.getIp(),this.listaServidores.size()+1);
									this.agregaServidor(servidor);
								}
							}
							
							setChanged(); // importante
							notifyObservers(servidor);
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

	private int posServidor(ServidorDTO servidorDTO) {
		int i=0;
		while(i<this.listaServidores.size() && (!this.listaServidores.get(i).getIp().equalsIgnoreCase(servidorDTO.getIp()) || this.listaServidores.get(i).getPuerto()!=servidorDTO.getPuerto())) {
			i++;
		}
		return i;
	}

	
}
