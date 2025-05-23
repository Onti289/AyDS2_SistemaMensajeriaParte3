package dto;

import java.io.Serializable;
import java.util.List;

public class RespuestaLista implements Serializable {
	private List<UsuarioDTO> lista ;
	private boolean conectado;
	public RespuestaLista(List<UsuarioDTO>  lista) {
		this.lista = lista;
		conectado=true;//no usar esto si entro por este constructor
	}
	public RespuestaLista(List<UsuarioDTO>  lista, boolean conectado) {
		this.lista=lista;
		this.conectado=conectado;
	}
	public boolean isConectado() {
		return conectado;
	}
	public List<UsuarioDTO>  getLista() {
		return lista;
	}
	public void setLista(List<UsuarioDTO>  lista) {
		this.lista = lista;
	}

}

