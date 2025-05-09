package variables;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import sincronizador.SincronizadorSocket;

public class Servidor {
	public static final int SERVIDOR_OFFLINE = 0;
	public static final int SERVIDOR_ONLINE = 1;
	public static final int SERVIDOR_SAVING = 2;
	private int _estado;
	private final int _puerto;
	private final int _id;
	private String _IP = "127.0.0.1";
	private SincronizadorSocket _conector;
	private int _conectados;
	private int _prioridad;
	private boolean _puedeLoguear = true;
	private Map<String, Integer> _ips = new ConcurrentHashMap<>();
	
	public Servidor(final int id, final int puerto, final int estado) {
		_id = id;
		_puerto = puerto;
		_estado = estado;
	}
	
	public void setCantidadIp(String ip, int cant) {
		_ips.put(ip, cant);
	}
	
	public int getCantidadPorIP(String ip) {
		if (_ips.get(ip) == null) {
			return 0;
		}
		return _ips.get(ip);
	}
	
	public String getIP() {
		return _IP;
	}
	
	public void setIP(String ip) {
		_IP = ip;
	}
	
	public int getID() {
		return _id;
	}
	
	public int getPuerto() {
		return _puerto;
	}
	
	public int getEstado() {
		return _estado;
	}
	
	public SincronizadorSocket getConector() {
		return _conector;
	}
	
	public int getConectados() {
		return _conectados;
	}
	
	public void setConectados(final int conectados) {
		_conectados = conectados;
	}
	
	public void setEstado(final int estado) {
		_estado = estado;
	}
	
	public void setConector(final SincronizadorSocket con) {
		_conector = con;
	}
	
	public void setPrioridad(int prioridad) {
		_prioridad = prioridad;
	}
	
	public String getStringParaAH() {
		return _id + ";" + _estado + ";" + _prioridad + ";" + (_puedeLoguear ? 1 : 0);
	}
}
