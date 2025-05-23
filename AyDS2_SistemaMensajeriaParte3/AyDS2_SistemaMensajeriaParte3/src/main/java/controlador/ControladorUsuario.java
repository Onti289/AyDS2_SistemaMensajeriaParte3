package controlador;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.PriorityQueue;

import dto.MensajeDTO;
import dto.RespuestaListaMensajes;
import dto.RespuestaListaUsuarios;
import dto.UsuarioDTO;
import modeloNegocio.*;
import util.Util;
import vistas.*;

public class ControladorUsuario implements ActionListener, Observer {
	protected IVista ventana;
	protected IVista ventana2;
	protected SistemaUsuario sistemaUsuario;

	public ControladorUsuario(SistemaUsuario sistemaUsuario) {
		this.ventana = new VentanaInicial();
		ventana.setVisible(true);
		this.sistemaUsuario = sistemaUsuario;
		this.ventana.setActionListener(this);
		sistemaUsuario.addObserver(this);
	}

	public IVista getVentana() {
		return ventana;
	}

	public SistemaUsuario getSistemaUsuario() {
		return sistemaUsuario;
	}

	public void setVentana(IVista ventana) {
		this.ventana = ventana;
		this.ventana.setActionListener(this);
		this.ventana.setVisible(true);
	}

	public void setUser(String nickName, String tipo) {

		this.sistemaUsuario.enviaSolicitudAServidor(nickName, tipo);
	}

	public String getNickNamePuerto() {
		return "Nickname:" + sistemaUsuario.getnickName() + "\nPuerto:" + sistemaUsuario.getPuerto();
	}

	public List<UsuarioDTO> getAgenda() {
		List<UsuarioDTO> lista = new ArrayList<UsuarioDTO>();
		PriorityQueue<Usuario> copia = new PriorityQueue<>(sistemaUsuario.getAgenda());
		Usuario user;
		while (!copia.isEmpty()) {
			user = copia.poll();
			lista.add(new UsuarioDTO(user.getNickName()));
		}

		return lista;
	}

	public List<UsuarioDTO> getListaConversaciones() {
		List<UsuarioDTO> lista = new ArrayList<UsuarioDTO>();
		List<Usuario> copia = new ArrayList<>(sistemaUsuario.getListaConversaciones());
		for (Usuario user : copia) {
			lista.add(new UsuarioDTO(user.getNickName()));
		}

		return lista;
	}

	public IVista getVentana2() {
		return ventana2;
	}

	public void setVentana2(IVista ventana2) {
		this.ventana2 = ventana2;
		this.ventana2.setActionListener(this);
		this.ventana2.setVisible(true);
	}

	public int agregaContacto(String nickName) {
		int nroCondicionAgregado;
		// Si puerto esta disponible es por que no existe ningun usuario con ese puerto

		nroCondicionAgregado = this.sistemaUsuario.agregarContacto(nickName);

		return nroCondicionAgregado;
	}

	public void cargaChat( String nickname) {
		for (MensajeDTO msg : this.sistemaUsuario.getChat(nickname)) {
			if (ventana instanceof VentanaPrincipal) {
				((VentanaPrincipal) ventana).agregarMensajeAchat(msg.getContenido(), msg.getFechayhora(),
						msg.getEmisor().getNombre());
				
			}
		}
	}

	public void actualizaListaConversacion(String nickname) {
		this.sistemaUsuario.setContactoActual(nickname);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		switch (e.getActionCommand()) {

		// llega aca cuado apreta boton registro en VentanaInicial
		case Util.CTEREGISTRO:
			if (this.ventana instanceof VentanaInicial) {
				this.ventana.setVisible(false);
				this.setVentana(
						new VentanaLoginORegistrar(this, Util.CTEREGISTRO, Util.CTEREGRISTRARSE, Util.CTEREGISTRAR));
			}

			break;
		case Util.CTEINICIARSESION:
			if (this.ventana instanceof VentanaInicial) {
				this.ventana.setVisible(false);
				this.setVentana(
						new VentanaLoginORegistrar(this, Util.CTEINICIOSESION, Util.CTEINICIARSESION, Util.CTELOGIN));
			}
			break;
		// llega aca cuando apreta boton registrar en VentanaRegistrarse
		case Util.CTEREGISTRAR:

			if (this.ventana instanceof VentanaLoginORegistrar) {
				VentanaLoginORegistrar ventanaRegistrarse = (VentanaLoginORegistrar) this.ventana;
				this.sistemaUsuario.estableceConexion();
				setUser(ventanaRegistrarse.getUsuario(), Util.CTEREGISTRAR);
			}

			break;
		// llega aca cuando apreta boton iniciar sesion en VentanaLogin
		case Util.CTELOGIN:

			if (this.ventana instanceof VentanaLoginORegistrar) {
				VentanaLoginORegistrar ventanaLogin = (VentanaLoginORegistrar) this.ventana;
				this.sistemaUsuario.estableceConexion();
				setUser(ventanaLogin.getUsuario(), Util.CTELOGIN);
			}

			break;

		case Util.CTEAGREGARCONTACTO: // muestra pantalla de directorio
			this.sistemaUsuario.pedirListaUsuarios();
			break;
		case Util.CTENUEVACONVER:
			this.setVentana2(new VentanaContactos(this));

			break;
		case Util.CTEENVIAR:
			if (ventana instanceof VentanaPrincipal) {
				String contenidoMensaje;
				contenidoMensaje = ((VentanaPrincipal) ventana).getTextFieldMensaje();
				UsuarioDTO user = ((VentanaPrincipal) ventana).getContactoConversacionActual();
				this.sistemaUsuario.enviarMensajeServidor(user, contenidoMensaje);
				
			}
			break;
		case Util.CTEINICIARCONVERSACION:
			if (this.ventana2 instanceof VentanaContactos) {
				VentanaContactos ventanaContactos = (VentanaContactos) this.ventana2;
				UsuarioDTO contacto = ventanaContactos.getUsuario();
				this.cargaChat(contacto.getNombre());
				this.actualizaListaConversacion(contacto.getNombre());
				// ACTUALIZA la lista en la ventana principal
				if (ventana instanceof VentanaPrincipal) {
					((VentanaPrincipal) ventana).actualizarListaChats(this.getListaConversaciones());
					// pone nombre de user seleccionado en parte de chat

					((VentanaPrincipal) ventana).setTextFieldNameContacto(contacto.getNombre());
					((VentanaPrincipal) ventana).setDejarSeleccionadoContactoNuevaConversacion(contacto);
					((VentanaPrincipal) ventana).vaciarTextFieldMensajes();
				}

				this.ventana2.dispose();
			}
			break;
		case Util.CTEAGREGAR: // agrega el contacto a la lista de contactos
			if (this.ventana2 instanceof VentanaDirectorio) {
				VentanaDirectorio ventanaDirectorio = (VentanaDirectorio) this.ventana2;
				UsuarioDTO usuario = ventanaDirectorio.getUsuario();
				int nroCondicionAgregado = this.agregaContacto(usuario.getNombre());
				if (nroCondicionAgregado == 2) {
					((VentanaDirectorio) ventana2).mostrarConfirmacionContactoAgregado();
					ventana2.dispose(); // cerrar la ventana luego de agregar
				} else {
					if (nroCondicionAgregado == 1) {
						((VentanaDirectorio) ventana2).mostrarErrorContactoYaAgendado();
					}
				}
			}
			break;
		default:
			break;
		}

	}

	public void contactoSeleccionadoDesdeLista(UsuarioDTO contacto) {

		if (ventana instanceof VentanaPrincipal) {
			VentanaPrincipal vp = (VentanaPrincipal) ventana;
			vp.setTextFieldNameContacto(contacto.getNombre());
			vp.limpiarChat();
			this.cargaChat(contacto.getNombre()); // Mostr s historial
		}
	}

	@Override
	public void update(Observable o, Object arg) {
		// TODO Auto-generated method stub
		if (arg instanceof Mensaje) {
			Mensaje mensaje = (Mensaje) arg;
			if (mensaje.getEmisor().equals(this.sistemaUsuario.getUsuario())) {
				((VentanaPrincipal) ventana).agregarMensajeAchat(mensaje.getContenido(), LocalDateTime.now(),
						this.sistemaUsuario.getUsuario().getNickName());
				((VentanaPrincipal) ventana).limpiarBuffer();
			} else {
				if (ventana instanceof VentanaPrincipal) {
					VentanaPrincipal vp = (VentanaPrincipal) ventana;
					// chequeo si soy receptor
					if (!mensaje.getEmisor().getNickName().equalsIgnoreCase(this.sistemaUsuario.getUsuario().getNickName())){
						// Si emisor es el contacto con el que estoy hablando muestra en pantalla
						if ((vp.hayConversaciones()) && (!(vp.getContactoConversacionActual() == null))
								&& mensaje.getEmisor().getNickName().equalsIgnoreCase(vp.getContactoConversacionActual().getNombre())) {
							vp.limpiarChat();
							this.cargaChat(mensaje.getEmisor().getNickName()); // Mostrar 
																											// historial
						} // notifica llega cuando no hay conversaciones o no es contacto actual
						else {
							vp.actualizarListaChats(this.getListaConversaciones());
							UsuarioDTO usuarioDTO = new UsuarioDTO(mensaje.getEmisor().getNickName());
							vp.notificarNuevoMensaje(usuarioDTO);
						}
					}
				}
			}

		}  else {
			if (arg instanceof Solicitud) { // pudo registrar o loguear a usuario

				Solicitud solicitud = (Solicitud) arg;
				if (solicitud.getTipoSolicitud().equalsIgnoreCase(Util.CTEREGISTRO)
						|| solicitud.getTipoSolicitud().equalsIgnoreCase(Util.CTELOGIN)) {// Si se registro o logueo
					this.ventana.setVisible(false);
					this.setVentana(new VentanaPrincipal(this));
					((VentanaPrincipal) ventana).TitulonameUsuario(solicitud.getNombre());
					System.out.println("11"+ solicitud.getTipoSolicitud());
					this.sistemaUsuario.enviaSolicitudAServidor(solicitud.getNombre(), Util.CTESOLICITARMENSAJES);
				} else { // no se pudo ni registrar ni loguear
					if (solicitud.getTipoSolicitud().equalsIgnoreCase(Util.CTEUSUARIOLOGUEADO)) {
						((VentanaLoginORegistrar) ventana).mostrarErrorUsuarioYaLogueado();
					} else {
						if (solicitud.getTipoSolicitud().equalsIgnoreCase(Util.CTEUSUERINEXISTENTE)) {
							((VentanaLoginORegistrar) ventana).mostrarErrorUsuarioInexistente();
						}
					}
				}
			} else {
				if (arg instanceof RespuestaListaMensajes) {
					System.out.println("5");
					RespuestaListaMensajes respuesta = (RespuestaListaMensajes) arg;
					List<MensajeDTO> lista = respuesta.getLista();
					
					for (Object obj : lista) {
						System.out.println("6 ");
						MensajeDTO m = (MensajeDTO) obj;
						((VentanaPrincipal) ventana).actualizarListaChats(this.getListaConversaciones());
						((VentanaPrincipal) ventana).notificarNuevoMensaje(m.getEmisor());
					}
				}
				else {
					if (arg instanceof RespuestaListaUsuarios) {
						RespuestaListaUsuarios respuesta=(RespuestaListaUsuarios) arg;
						List<UsuarioDTO> lista = respuesta.getLista();
						List<UsuarioDTO> listaUsuarios = new ArrayList<>();
						String nombre = this.getSistemaUsuario().getnickName();
						for(Object obj : lista) {	
							UsuarioDTO u = (UsuarioDTO) obj;
							if (!u.getNombre().equalsIgnoreCase(nombre)) {
								listaUsuarios.add(u);
							}
						}
						this.setVentana2(new VentanaDirectorio(this, listaUsuarios));
					}
					else {
						if(arg instanceof String) {
							String respuesta=(String)arg;
							if(respuesta.equalsIgnoreCase(Util.SIN_SERVER_DISPONIBLE)) {
								ventana.mostrarErrorServidoresCaidos(respuesta);
							}
						}
					}
					// Aca hacer que vista con algun metodo tome esa lista
					// y lo muestre por pantalla
				}
			}
		}
	}
}
