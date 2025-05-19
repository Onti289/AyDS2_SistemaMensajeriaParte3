package dto;

import java.io.Serializable;
import java.util.List;

public class RespuestaListaUsuarios implements Serializable {
	private List<UsuarioDTO> lista ;

	public RespuestaListaUsuarios(List<UsuarioDTO>  lista) {
		super();
		this.lista = lista;

	}
	public List<UsuarioDTO>  getLista() {
		return lista;
	}
	public void setLista(List<UsuarioDTO>  lista) {
		this.lista = lista;
	}

}

