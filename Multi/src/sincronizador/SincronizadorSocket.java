package sincronizador;

import java.io.BufferedInputStream;
import java.io.PrintWriter;
import java.net.Socket;
import login.LoginServer;
import variables.Cuenta;
import variables.Servidor;
import estaticos.Encriptador;
import estaticos.MainMultiservidor;
import estaticos.Mundo;

public class SincronizadorSocket implements Runnable {
	private Socket _socket;
	private BufferedInputStream _in;
	private PrintWriter _out;
	private Thread _thread;
	private Servidor _servidor;
	private String _IP;
	
	public SincronizadorSocket(Socket socket) {
		try {
			_socket = socket;
			_IP = _socket.getInetAddress().getHostAddress();
			if (MainMultiservidor.MOSTRAR_SINCRONIZACION) {
				System.out.println("INTENTO SINCRONIZAR IP: " + _IP);
			}
			_in = new BufferedInputStream((_socket.getInputStream()));
			_out = new PrintWriter(_socket.getOutputStream());
			_thread = new Thread(this);
			_thread.setDaemon(true);
			_thread.start();
		} catch (final Exception e) {
			if (MainMultiservidor.MOSTRAR_SINCRONIZACION) {
				System.out.println("INTENTO FALLIDO -> " + e.toString());
			}
			desconectar();
		}
	}
	
	public void run() {
		try {
			try {
				Thread.sleep(1000);
			} catch (final Exception e) {}
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
						analizarPackets(packet);
					}
					lenght = -1;
				}
			}
		} catch (final Exception e) {
			// System.out.println("EXCEPTION RUN CONECTOR, IP: " + _IP + " PUERTO: " + _puerto);
		} finally {
			if (_servidor != null) {
				System.out.println("<<<--- CERRANDO CONEXION SERVIDOR ID " + _servidor.getID() + ", IP " + _IP + " --->>>");
			}
			desconectar();
		}
	}
	
	public void analizarPackets(final String packet) {
		try {
			switch (packet.charAt(0)) {
				case 'A' :// cuenta
					try {
						final String[] str = packet.substring(1).split(";");
						final int cuentaID = Integer.parseInt(str[0]);
						final int cantPersonajes = Integer.parseInt(str[1]);
						Cuenta cuenta = Mundo.getCuenta(cuentaID);
						cuenta.setPersonajes(_servidor.getID(), cantPersonajes);
					} catch (final Exception e) {}
					break;
				case 'C' :// cambiar conectados
					try {
						final int conectados = Integer.parseInt(packet.substring(1));
						if (_servidor != null) {
							_servidor.setConectados(conectados);
						}
					} catch (final Exception e) {
						e.printStackTrace();
					}
					break;
				case 'D' :// asignando servidor
					try {
						final String[] str = packet.substring(1).split(";");
						final int servidorID = Integer.parseInt(str[0]);
						final int puerto = Integer.parseInt(str[1]);
						final int prioridad = Integer.parseInt(str[2]);
						final int estado = Integer.parseInt(str[3]);
						if (str.length > 4) {
							_IP = str[4];
						}
						Servidor servidor = Mundo.Servidores.get(servidorID);
						if (servidor == null) {
							servidor = new Servidor(servidorID, puerto, Servidor.SERVIDOR_OFFLINE);
							Mundo.Servidores.put(servidorID, servidor);
						}
						_servidor = servidor;
						_servidor.setIP(_IP);
						_servidor.setPrioridad(prioridad);
						_servidor.setEstado(estado);
						_servidor.setConector(this);
						LoginServer.refreshServersEstado();
						LoginServer.enviarPacketConexionServidor(_servidor);


						final String GREEN = "\u001B[32m";
						final String YELLOW = "\u001B[33m";
						final String RESET = "\u001B[0m";
												
                        System.out.println(GREEN +"Servidor Id:" + RESET+ YELLOW + _servidor.getID() + RESET);
						System.out.println(GREEN +"Ip:" + YELLOW + _IP + RESET);
						
					} catch (final Exception e) {}
					break;
				case 'I' :
					try {
						if (_servidor != null) {
							final String[] str = packet.substring(1).split(";");
							final String ip = str[0];
							final int cantidad = Integer.parseInt(str[1]);
							_servidor.setCantidadIp(ip, cantidad);
						}
					} catch (final Exception e) {}
					break;
				case 'S' :// cambiar estado
					try {
						final int estado = Integer.parseInt(packet.substring(1));
						if (_servidor != null) {
							if (estado == Servidor.SERVIDOR_OFFLINE) {
								desconectar();
							} else {
								_servidor.setEstado(estado);
								LoginServer.refreshServersEstado();
							}
						}
					} catch (final Exception e) {}
					break;
			}
		} catch (final Exception e) {}
	}
	
	public boolean estaCerrado() {
		if (_socket == null || _socket.isClosed()) {
			return true;
		}
		return false;
	}
	
	public void desconectar() {
		try {
			if (_socket != null && !_socket.isClosed()) {
				_socket.close();
			}
			if (_servidor != null) {
				_servidor.setConector(null);
				_servidor.setEstado(Servidor.SERVIDOR_OFFLINE);
				LoginServer.refreshServersEstado();
			}
			if (_in != null) {
				_in.close();
			}
			if (_out != null) {
				_out.close();
			}
		} catch (final Exception e) {}
	}
	
	public void sendPacket(String packet) {
		if (_out != null && !packet.isEmpty() && !packet.equals("" + (char) 0x00)) {
			packet = Encriptador.aUTF(packet);
			try {
				System.out.println("ENVIAR PACKET SERVIDOR (" + _servidor.getID() + ") >> " + packet);
			} catch (final Exception e) {}
			_out.print(packet + (char) 0x00);
			_out.flush();
		}
	}
}
