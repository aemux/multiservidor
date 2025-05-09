package login;

import estaticos.Encriptador;
import estaticos.Mundo;
import estaticos.MainMultiservidor;
import estaticos.GestorSQL;
import estaticos.GestorSalida;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.regex.Pattern;
import javax.swing.Timer;
import variables.Cuenta;
import variables.Servidor;

public class LoginSocket implements Runnable {
	private Thread _thread;
	private BufferedInputStream _in;
	private PrintWriter _out;
	private Socket _socket;
	private String _codigoLlave, _IP, _nombreCuenta, _claveEscrita;
	private String _tipoPacket = "CLIENTE";
	private byte _intento = 0;
	private Cuenta _cuenta;
	public Timer _timer;
	
	public LoginSocket(final Socket socket) {
		try {
			_socket = socket;
			_IP = _socket.getInetAddress().getHostAddress();
			_in = new BufferedInputStream((_socket.getInputStream()));
			_out = new PrintWriter(_socket.getOutputStream()); 
			LoginServer.addCliente(this);
			_thread = new Thread(this);
			_thread.setDaemon(true);
			_thread.start();
		} catch (final IOException e) {
			desconectar();
		}
	}
	
	public void run() {
		try {
			if (MainMultiservidor.SEGUNDOS_ESPERA > 0) {
				_timer = new Timer(MainMultiservidor.SEGUNDOS_ESPERA * 1000, new ActionListener() {
					public void actionPerformed(final ActionEvent arg0) {
						GestorSQL.INSERT_BAN_IP(_IP);
						MainMultiservidor.escribirLog("LA IP " + _IP + " ESTA ATACANDO EL MULTISERVIDOR");
						desconectar();
					}
				});
			}
			GestorSalida.ENVIAR_XML_POLICY_FILE(_out);
			_codigoLlave = GestorSalida.ENVIAR_HC_CODIGO_LLAVE(_out);
			int c = -1;
			int lenght = -1;
			int index = 0;
			byte[] bytes = new byte[1];
			while ((c = _in.read()) != -1) {
				if (lenght == -1) {
					lenght = _in.available();
					bytes = new byte[lenght + 1];
					index = 0;
				}
				bytes[index++] = (byte) c;
				if (bytes.length == index) {
					String tempPacket = new String(bytes, "UTF-8");
					for (String packet : tempPacket.split("[\u0000\n\r]")) {
						if (packet.isEmpty()) {
							continue;
						}
						if (MainMultiservidor.MOSTRAR_RECIBIDOS) {
							System.out.println("<<RECIBIR GENERAL:  " + packet);
						}
						analizar_Packet_Real(packet);
					}
					lenght = -1;
				}
			}
		} catch (final Exception e) {
			// e.printStackTrace();
		} finally {
			try {
				limpiarCuenta();
				desconectar();
			} catch (final Exception e) {}
		}
	}
	
	public PrintWriter getOut() {
		return _out;
	}
	
	public Cuenta getCuenta() {
		return _cuenta;
	}
	
	private void limpiarCuenta() {
		if (_cuenta != null) {
			EnEspera.delEspera(_cuenta);
			if (_cuenta.getSocket() == this) {
				_cuenta.setSocket(null);
			}
		}
	}
	
	public String getPacketConexion() {
		if (_cuenta == null) {
			return "";
		}
		return "A" + _cuenta.getID() + ";" + _IP;
	}
	
	public String getActualIP() {
		return _IP;
	}
	
	public void desconectar() {
		try {
			LoginServer.borrarEscogerServer(this);
			LoginServer.borrarCliente(this);
			if (_socket != null && !_socket.isClosed()) {
				_socket.close();
			}
			if (_in != null) {
				_in.close();
			}
			if (_out != null) {
				_out.close();
			}
			pararTimer();
		} catch (final Exception e1) {
			e1.printStackTrace();
		}
	}
	
	public void pararTimer() {
		if (_timer != null) {
			try {
				_timer.stop();
			} catch (Exception e) {}
		}
	}
	
	private boolean necesitaCompletarDatos() {
		return _cuenta.getActualizar() == 3 || GestorSQL.GET_APELLIDO(_nombreCuenta).isEmpty();
	}
	
	private void analizar_Packet_Real(final String packet) {
		try {
			switch (_tipoPacket) {
				case "CLIENTE" :
					if (packet.equalsIgnoreCase("<policy-file-request/>")) {
						// GestorSalida.ENVIAR_XML_POLICY_FILE(_out);
						return;
					}
					if (packet.length() > 3 && packet.substring(0, 2).equals("##")) {
						final String[] param = Encriptador.filtro(packet.substring(3)).split(";");
						if (_intento < 10) {
							_intento++;
							GestorSalida.ENVIAR_HP_PASS_ENVIADA(_out, GestorSQL.GET_CONTRASEÑA_SI(param[0], param[2], param[1],
							param[3]));
						} else {
							GestorSQL.INSERT_BAN_IP(_IP);
							GestorSalida.ENVIAR_AlEb_CUENTA_BANEADA_DEFINITIVO(_out);
							desconectar();
							return;
						}
					} else if (packet.length() > 8 && packet.substring(0, 8).equalsIgnoreCase("BUSTOFUS")) {
						GestorSalida.ENVIAR_HR_RECUPERAR_CUENTA(_out, GestorSQL.GET_PREGUNTA_SECRETA(Encriptador.filtro(packet
						.substring(8))));
					} else if (MainMultiservidor.VERSION_CLIENTE.equals("ANY")
					|| packet.equalsIgnoreCase(MainMultiservidor.VERSION_CLIENTE)) {
						// da
						_tipoPacket = "CUENTA";
					} else {
						GestorSalida.ENVIAR_AlEv_ERROR_VERSION_DEL_CLIENTE(_out);
						desconectar();
						return;
					}
					break;
				case "CUENTA" :// recibe el nombre la cuenta
					_nombreCuenta = Encriptador.filtro(packet);
					_tipoPacket = "PASSWORD";
					break;
				case "PASSWORD" :// verifica si existe la cuenta
					if (packet.length() < 3 || !packet.substring(0, 2).equals("#1")) {
						GestorSalida.enviar(_out, "ATE");
						desconectar();
						return;
					}
					_claveEscrita = packet;
					_cuenta = Mundo.getCuenta(GestorSQL.GET_ID_CUENTA_NOMBRE(_nombreCuenta));
					if (_cuenta == null) {
						GestorSQL.CARGAR_CUENTA_POR_NOMBRE(_nombreCuenta);// cuenta nueva
						Thread.sleep(500);
						_cuenta = Mundo.getCuenta(GestorSQL.GET_ID_CUENTA_NOMBRE(_nombreCuenta));
						if (_cuenta == null) {
							GestorSalida.ENVIAR_AlEp_CUENTA_NO_VALIDA(_out);
							desconectar();
							return;
						}
					}
					String encriptada = Encriptador.encriptarContraseña(_codigoLlave, GestorSQL
					.GET_CONTRASEÑA_CUENTA(_nombreCuenta));
					if (_claveEscrita.equals(encriptada)) {
						if (GestorSQL.ES_IP_BANEADA(_IP)) {
							GestorSalida.ENVIAR_AlEb_CUENTA_BANEADA_DEFINITIVO(_out);
							desconectar();
							return;
						}
						final long tiempoBaneo = GestorSQL.GET_BANEADO(_nombreCuenta);
						if (tiempoBaneo != 0) {
							if (tiempoBaneo <= -1) {
								GestorSalida.ENVIAR_AlEb_CUENTA_BANEADA_DEFINITIVO(_out);
								desconectar();
								return;
							} else if (tiempoBaneo > System.currentTimeMillis()) {
								GestorSalida.ENVIAR_AlEk_CUENTA_BANEADA_TIEMPO(_out, tiempoBaneo);
								desconectar();
								return;
							} else {
								GestorSQL.SET_BANEADO(_nombreCuenta, 0);
							}
						}
						// int conectados = 0;// GestorSQL.GET_CUENTAS_CONECTADAS_TOTAL();
						// if (Bustemu.LIMITE_JUGADORES < conectados && _cuenta.getRango() == 0) {
						// GestorSalida.ENVIAR_AlEw_MUCHOS_JUG_ONLINE(_out);
						// return;
						// }
						if (MainMultiservidor.ACCESO_VIP) {
							if (_cuenta.getTiempoAbono() < 1) {
								GestorSalida.ENVIAR_M0_MENSAJE_SVR_MUESTRA_DISCONNECT(_out, "34",
								"Il faut être V.I.P. pour accéder à ce serveur", "");
								desconectar();
								return;
							}
						}
						Mundo.enviarCantidadIps(_IP);
						Thread.sleep(100);
						final int cuentasPorIP = LoginServer.getCantidadIps(_IP);
						if (MainMultiservidor.PERMITIR_MULTICUENTA) {
							if (cuentasPorIP > MainMultiservidor.MAX_CUENTAS_POR_IP) {
								GestorSalida
								.ENVIAR_M0_MENSAJE_SVR_MUESTRA_DISCONNECT(_out, "34", (cuentasPorIP - 1) + ";" + _IP, "");
								desconectar();
								return;
							}
						} else if (cuentasPorIP > 1) {
							GestorSalida.ENVIAR_M0_MENSAJE_SVR_MUESTRA_DISCONNECT(_out, "34", "1;" + _IP, "");
							desconectar();
							return;
						}
						if (_cuenta.getSocket() != null && _cuenta.getSocket() != this) {
							GestorSalida.ENVIAR_AlEd_DESCONECTAR_CUENTA_CONECTADA(_cuenta.getSocket()._out);
							_cuenta.getSocket().desconectar();
						}
						_tipoPacket = "DEFAULT";
						_cuenta.setSocket(this);
						EnEspera.addEspera(_cuenta);
						if (_timer != null) {
							_timer.start();
						}
						if (GestorSQL.GET_APODO(_nombreCuenta).isEmpty()) {
							String apodo = Encriptador.palabraAleatorio(12);
							while (GestorSQL.GET_APODO_EXISTE(apodo)) {
								apodo = Encriptador.palabraAleatorio(12);
							}
							GestorSQL.UPDATE_APODO(Encriptador.palabraAleatorio(12), _cuenta.getID());
						}
						Mundo.enviarPacketsAServidores(this);
						GestorSQL.UPDATE_ULTIMA_IP(_IP, _cuenta.getID());
						if (!MainMultiservidor.SONIDO_BIENVENIDA.isEmpty()) {
							GestorSalida.ENVIAR_Bv_SONAR_MP3(_out, MainMultiservidor.SONIDO_BIENVENIDA);
						}
					} else {
						GestorSalida.ENVIAR_AlEx_NOMBRE_O_PASS_INCORRECTA(_out);
						desconectar();
						return;
					}
					break;
				default :
					if (_cuenta == null) {
						GestorSalida.ENVIAR_AlEp_CUENTA_NO_VALIDA(_out);
						desconectar();
					} else if (packet.substring(0, 2).equals("Af")) {
						try {
							if (_timer != null) {
								_timer.restart();
							}
						} catch (final Exception e) {}
						if (MainMultiservidor.ACTIVAR_FILA_ESPERA) {
							final int pendiente = EnEspera.getIndexOf(_cuenta);
							if (pendiente < MainMultiservidor.MAX_CONEXION_POR_SEGUNDO) {
								EnEspera.suTurno(_cuenta, _out);
							} else if (pendiente == -1) {
								desconectar();
								return;
							} else {
								EnEspera.enEspera(pendiente, _out);
							}
						} else {
							EnEspera.suTurno(_cuenta, _out);
						}
					} else if (packet.substring(0, 2).equals("Ax")) {
						pararTimer();
						if (_cuenta.getSocket() != null && _cuenta.getSocket() != this) {
							GestorSalida.ENVIAR_AlEd_DESCONECTAR_CUENTA_CONECTADA(_cuenta.getSocket()._out);
							_cuenta.getSocket().desconectar();
							_cuenta.setSocket(this);
						}
						LoginServer.addEscogerServer(this);
						GestorSalida.ENVIAR_AxK_TIEMPO_ABONADO_NRO_PJS(_out, _cuenta);
						if (necesitaCompletarDatos()) {
							GestorSalida.ENVIAR_HU_ACTUALIZAR_DATOS(_out);
						}
					} else if (packet.substring(0, 2).equals("AC")) {// cambiar password
						pararTimer();
						if (necesitaCompletarDatos()) {
							GestorSalida.ENVIAR_HU_ACTUALIZAR_DATOS(_out);
						} else {
							GestorSalida.ENVIAR_HN_CAMBIAR_PASSWORD(_out, GestorSQL.GET_PREGUNTA_SECRETA(_nombreCuenta));
						}
					} else if (packet.substring(0, 2).equals("AF")) {// confirmar nueva password
						pararTimer();
						if (necesitaCompletarDatos()) {
							GestorSalida.ENVIAR_HU_ACTUALIZAR_DATOS(_out);
						} else {
							try {
								final String[] param = Encriptador.filtro(packet.substring(2)).split(Pattern.quote(";"));
								String nuevaPass = GestorSQL.CAMBIAR_CONTRASEÑA(_cuenta.getNombre(), param[1], param[2], param[3],
								_cuenta.getID());
								GestorSalida.ENVIAR_HF_CONFIRMAR_NUEVA_PASSWORD(_out, nuevaPass);
							} catch (final Exception e) {
								GestorSalida.ENVIAR_HF_CONFIRMAR_NUEVA_PASSWORD(_out, "");
							}
						}
					} else if (packet.substring(0, 2).equals("AX")) {// escoger server
						pararTimer();
						if (necesitaCompletarDatos()) {
							GestorSalida.ENVIAR_HU_ACTUALIZAR_DATOS(_out);
						} else {
							LoginServer.borrarEscogerServer(this);
							LoginServer.Tiempos.put(_IP, System.currentTimeMillis());
							int servidorID = Integer.parseInt(packet.substring(2));
							final Servidor servidor = Mundo.Servidores.get(servidorID);
							if (servidor == null || servidor.getEstado() == Servidor.SERVIDOR_OFFLINE) {
								GestorSalida.ENVIAR_AlEn_CONEXION_NO_TERMINADA(_out);
								return;
							}
							GestorSalida.ENVIAR_AXK_O_AYK_IP_SERVER(_out, _cuenta.getID(), servidor.getIP(), servidor.getPuerto());
						}
					} else if (packet.substring(0, 2).equals("UP")) {// actualizar datos
						pararTimer();
						final String[] param = Encriptador.filtro(packet.substring(2)).split(Pattern.quote("|"));
						if (GestorSQL.ACTUALIZAR_DATOS(param, _cuenta.getID(), _out)) {
							GestorSalida.ENVIAR_HV_CONFIRMADA_ACTUALIZACION_DATOS(_out);
						}
					}
			}
		} catch (final Exception e) {
			e.printStackTrace();
			GestorSalida.ENVIAR_AlEp_CUENTA_NO_VALIDA(_out);
			desconectar();
		}
	}
}
