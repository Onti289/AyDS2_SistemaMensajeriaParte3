package dto;

import java.io.Serializable;
import java.util.List;

public class RespuestaListaMensajes implements Serializable{
	private List<MensajeDTO> lista ;
	public RespuestaListaMensajes(List<MensajeDTO>  lista) {
		super();
		this.lista = lista;
	}
	public List<MensajeDTO>  getLista() {
		return lista;
	}
	public void setLista(List<MensajeDTO>  lista) {
		this.lista = lista;
	}

	
}
