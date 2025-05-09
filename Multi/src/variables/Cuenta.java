package variables;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import login.LoginSocket;
import estaticos.GestorSQL;

public class Cuenta {
	private final int _ID;
	private final String _nombre;
	private LoginSocket _entradaGeneral;
	private Map<Integer, Integer> _personajes = new TreeMap<Integer, Integer>();
	
	public Cuenta(final int ID, final String nombre, final String apodo) {
		_ID = ID;
		_nombre = nombre;
	}
	
	public int getID() {
		return _ID;
	}
	
	public String getNombre() {
		return _nombre;
	}
	
	public long getTiempoAbono() {
		return Math.max(0, GestorSQL.GET_ABONO(_nombre) - System.currentTimeMillis());
	}
	
	public byte getActualizar() {
		return GestorSQL.GET_ACTUALIZAR(_nombre);
	}
	
	public String getContraseña() {
		return GestorSQL.GET_CONTRASEÑA_CUENTA(_nombre);
	}
	
	public String getApodo() {
		return GestorSQL.GET_APODO(_nombre);
	}
	
	public String getIdioma() {
		return GestorSQL.GET_IDIOMA(_nombre);
	}
	
	public String getPregunta() {
		return GestorSQL.GET_PREGUNTA_SECRETA(_nombre);
	}
	
	public String getRespuesta() {
		return GestorSQL.GET_RESPUESTA_SECRETA(_nombre);
	}
	
	public int getRango() {
		return GestorSQL.GET_RANGO(_nombre);
	}
	
	public long getAbono() {
		return Math.max(0, GestorSQL.GET_ABONO(_nombre) - System.currentTimeMillis());
	}
	
	public LoginSocket getSocket() {
		return _entradaGeneral;
	}
	
	public void pararTimer() {
		try {
			_entradaGeneral.pararTimer();
		} catch (final Exception e) {}
	}
	
	public void setSocket(final LoginSocket socket) {
		_entradaGeneral = socket;
	}
	
	public Map<Integer, Integer> getPersonajes() {
		return _personajes;
	}
	
	public void setPersonajes(int servidor, int cantidad) {
		_personajes.put(servidor, cantidad);
	}
	
	public String getStringPersonajes() {
		try {
			final StringBuilder str = new StringBuilder();
			for (final Entry<Integer, Integer> entry : _personajes.entrySet()) {
				if (entry.getValue() < 1) {
					continue;
				}
				str.append("|" + entry.getKey() + "," + entry.getValue());
			}
			return str.toString();
		} catch (final NullPointerException e) {
			return "";
		} catch (final Exception e) {
			return getStringPersonajes();
		}
	}
}
