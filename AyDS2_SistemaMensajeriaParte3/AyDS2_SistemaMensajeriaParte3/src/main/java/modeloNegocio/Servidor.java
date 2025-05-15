package modeloNegocio;

public class Servidor {
	boolean enLinea;
	boolean principal ;
	int puerto;
	String ip;
	int nro;
	public Servidor(boolean enLinea, boolean principal,int puerto,String ip,int nro) {
		super();
		this.enLinea = enLinea;
		this.principal = principal;
		this.puerto=puerto;
		this.ip=ip;
		this.nro=nro;
	}
	public boolean isEnLinea() {
		return enLinea;
	}
	public boolean isPrincipal() {
		return principal;
	}
	public String getIp() {
		return ip;
	}
	public int getNro() {
		return nro;
	}
	public int getPuerto() {
		return puerto;
	}
	public void setEnLinea(boolean enLinea) {
		this.enLinea = enLinea;
	}
	public void setPrincipal(boolean principal) {
		this.principal = principal;
	}
	
	public void setPuerto(int puerto) {
		this.puerto = puerto;
	}
	
	public void setIp(String ip) {
		this.ip = ip;
	}
	public void setNro(int nro) {
		this.nro=nro;
	}
}
