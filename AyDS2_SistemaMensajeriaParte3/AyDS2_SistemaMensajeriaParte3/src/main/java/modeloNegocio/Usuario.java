package modeloNegocio;

import java.io.Serializable;
import java.util.*;
import dto.*;
import util.*;

@SuppressWarnings("serial")
public class Usuario implements Serializable {
	private String nickName;
	private String ip;
	private int puerto;
	private transient PriorityQueue<Usuario> agenda = new PriorityQueue<>(Comparator.comparing(Usuario::getNickName));

	private transient List<Usuario> listaConversaciones = new LinkedList<>();

	private transient ArrayList<Mensaje> mensajes = new ArrayList<>();

	// constructor para usuario main
	public Usuario(String nickName, int puerto) {
		super();
		this.nickName = nickName;
		this.ip = Util.IPLOCAL;
		this.puerto = puerto;
	}

	// constructor para agregar contacto
	public Usuario(String nickName, int puerto, String ip) {
		super();
		this.nickName = nickName;
		this.ip = ip;
		this.puerto = puerto;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getPuerto() {
		return puerto;
	}

	public void setPuerto(int puerto) {
		this.puerto = puerto;
	}

	@Override
	public String toString() {
		return String.format("%s", nickName);
	}

	public void guardarMensaje(Mensaje msg) {
		this.mensajes.add(msg);
	}

	public void recibirMensaje(Mensaje m) {
		this.agregarConversacion(m.getEmisor());
		// Mensaje m = new Mensaje(contenido, LocalDateTime.now(), emisor, this);
		this.guardarMensaje(m);
	}

	// equals y hashCode ayudan a que el metodo contains en recibir y enviar msg
	// en la lista de conver compare solo por puerto e id y no el puntero del objeto
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		Usuario other = (Usuario) obj;
		return this.puerto == other.puerto && this.ip.equals(other.ip);
	}

	@Override
	public int hashCode() {
		return Integer.hashCode(puerto);
	}

	public ArrayList<Mensaje> getMensajes() {
		return this.mensajes;
	}

	public ArrayList<MensajeDTO> getChat(int puerto, String ip) {
		ArrayList<MensajeDTO> chat = new ArrayList<>();
		for (Mensaje m : mensajes) {
			// aca ves si sos emisor o receptor
			boolean yoSoyEmisor = m.getEmisor().equals(this);
			boolean yoSoyReceptor = m.getReceptor().equals(this);
			// aca se fija si CONTACTO que llega es el emisor o receptor
			boolean otroEsEmisor = m.getEmisor().getPuerto() == puerto && m.getEmisor().getIp().equals(ip);
			boolean otroEsReceptor = m.getReceptor().getPuerto() == puerto && m.getReceptor().getIp().equals(ip);
			// Ya que no pueden ambos ser emisores o receptores
			if ((yoSoyEmisor && otroEsReceptor) || (yoSoyReceptor && otroEsEmisor)) {
				String nombre=m.getEmisor().getNickName();
				int puertouser=m.getEmisor().getPuerto();
				String ipuser=m.getEmisor().getIp();
				UsuarioDTO emisor=new UsuarioDTO(nombre,puertouser,ipuser);
				nombre=m.getReceptor().getNickName();
				puertouser=m.getReceptor().getPuerto();
				ipuser=m.getReceptor().getIp();
				UsuarioDTO receptor=new UsuarioDTO(nombre,puertouser,ipuser);
				chat.add(new MensajeDTO(m.getContenido(), m.getFechayhora(),emisor,receptor));
			}
		}
		return chat;
	}

	public PriorityQueue<Usuario> getAgenda() {
		return new PriorityQueue<>(agenda);
	}

	public List<Usuario> getListaConversaciones() {
		return new ArrayList<>(listaConversaciones);
	}

	public void agregarConversacion(Usuario contacto) {
		if (!this.agenda.contains(contacto)) {
			this.agenda.add(contacto);
		}
		if (this.listaConversaciones.isEmpty() || !this.listaConversaciones.contains(contacto)) {
			this.listaConversaciones.add(contacto);
		}
	}

	public void setAgenda(PriorityQueue<Usuario> agenda) {
		this.agenda = agenda;
	}

	public void agregaContacto(Usuario contacto) {
		agenda.add(contacto);
		// this.muestraContactos();
	}

	public void muestraContactos() {
		PriorityQueue<Usuario> agendaCopia = new PriorityQueue<>(agenda);

		while (!agendaCopia.isEmpty()) {
			Usuario contacto = agendaCopia.poll();
			System.out.println(contacto.nickName);
		}
	}

	public Usuario getBuscaContacto(int puerto) {
		PriorityQueue<Usuario> agendaCopia = new PriorityQueue<>(agenda);
		while (!agendaCopia.isEmpty()) {
			Usuario contacto = agendaCopia.poll();
			if (contacto.getPuerto() == puerto) {
				return contacto;
			}
		}
		return null; // No se encontro
	}

	public boolean estaContacto(int puerto) {
		PriorityQueue<Usuario> agendaCopia = new PriorityQueue<>(agenda);
		boolean esta = false;
		while (!agendaCopia.isEmpty()) {
			Usuario contacto = agendaCopia.poll();
			if (contacto.getPuerto() == puerto) {
				esta = true;
			}
		}
		return esta;
	}
}
