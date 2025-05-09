package estaticos;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.Timer;
import java.util.TimerTask;
//import java.util.Timer;
import java.sql.PreparedStatement;
//import java.util.TimerTask;
import variables.Cuenta;

public class GestorSQL {
	private static Connection bdCuentas;
	private static Timer timerComienzo;
	private static boolean necesitaHacerTransaccion;
	
	private static void cerrarResultado(final ResultSet resultado) {
		try {
			resultado.getStatement().close();
			resultado.close();
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void cerrarDeclaracion(final PreparedStatement declaracion) {
		try {
			declaracion.clearParameters();
			declaracion.close();
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}
	
	public static ResultSet consultaSQL(final String consultaSQL) throws Exception {
		final PreparedStatement declaracion = (PreparedStatement) bdCuentas.prepareStatement(consultaSQL);
		final ResultSet resultado = declaracion.executeQuery();
		declaracion.setQueryTimeout(300);
		return resultado;
	}
	
	public static ResultSet consultaSQL(final PreparedStatement declaracion) throws Exception {
		declaracion.execute();
		final ResultSet resultado = declaracion.executeQuery();
		declaracion.setQueryTimeout(300);
		return resultado;
	}
	
	public static PreparedStatement transaccionSQL(final String consultaSQL) throws Exception {
		final PreparedStatement declaracion = (PreparedStatement) bdCuentas.prepareStatement(consultaSQL);
		necesitaHacerTransaccion = true;
		return declaracion;
	}
	
	public static void TIMER(final boolean iniciar) {
		if (iniciar) {
			timerComienzo = new Timer();
			timerComienzo.schedule(new TimerTask() {
				public void run() {
					if (!necesitaHacerTransaccion) {
						return;
					}
					comenzarTransacciones();
					necesitaHacerTransaccion = false;
				}
			}, MainMultiservidor.SEGUNDOS_TRANSACCION_BD * 1000, MainMultiservidor.SEGUNDOS_TRANSACCION_BD * 1000);
		} else {
			timerComienzo.cancel();
		}
	}
	
	public static synchronized void comenzarTransacciones() {
		try {
			if (bdCuentas.isClosed()) {
				cerrarConexion();
				iniciarConexion();
			}
			bdCuentas.commit();
		} catch (final Exception e) {
			System.out.println("SQL ERROR:" + e.toString());
			e.printStackTrace();
			// comenzarTransacciones();
		}
	}
	
	public static synchronized void cerrarConexion() {
		try {
			try {
				bdCuentas.commit();
			} catch (final Exception e) {}
			bdCuentas.close();
		} catch (final Exception e) {
			MainMultiservidor.escribirLog("Error en la ventana de conexiones SQL:" + e.toString());
			e.printStackTrace();
		}
	}
	
	public static final boolean iniciarConexion() {
		try {

			System.out.print("\u001B[32mInicializando Módulo SQL: \u001B[0m");
			bdCuentas = DriverManager.getConnection("jdbc:mysql://" + MainMultiservidor.BD_HOST + "/"
			+ MainMultiservidor.BD_CUENTAS + "?autoReconnect=true", MainMultiservidor.BD_USUARIO, MainMultiservidor.BD_PASS);
			bdCuentas.setAutoCommit(false);
			if (!bdCuentas.isValid(1000)) {
				MainMultiservidor.escribirLog("SQLError : Conexion a la BDD invalida");
				return false;
			}
			necesitaHacerTransaccion = false;
			TIMER(true);
			return true;
		} catch (final Exception e) {
			MainMultiservidor.escribirLog("ERROR SQL: " + e.toString());
			e.printStackTrace();
		}
		return false;
	}
	
	// public static void UPDATE_CUENTAS_LOG_CERO() {
	// String consultaSQL = "UPDATE `cuentas` SET `logeado`= 0;";
	// try {
	// final PreparedStatement declaracion = transaccionSQL(consultaSQL);
	// declaracion.executeUpdate();
	// cerrarDeclaracion(declaracion);
	// } catch (final Exception e) {
	// System.out.println("ERROR SQL: " + e.toString());
	// MainMultiservidor.escribirLog("LINEA SQL: " + consultaSQL);
	// e.printStackTrace();
	// }
	// }
	//
	// public static int GET_CUENTAS_CONECTADAS_IP(final String ip) {
	// int i = 0;
	// try {
	// String consultaSQL = "SELECT * FROM `cuentas` WHERE `ultimaIP` = '" + ip +
	// "' AND `logeado` = '1' ;";
	// final ResultSet resultado = consultaSQL(consultaSQL);
	// while (resultado.next()) {
	// i++;
	// }
	// cerrarResultado(resultado);
	// } catch (final Exception e) {
	// MainMultiservidor.escribirLog("ERROR SQL: " + e.toString());
	// e.printStackTrace();
	// }
	// return i;
	// }
	
	public static boolean ACTUALIZAR_DATOS(final String[] infos, final int id, PrintWriter out) {
		String[] datos = new String[8];
		for (int s = 0; s < 8; s++) {
			try {
				String t = infos[s];
				if (t.length() > 40) {
					t = t.substring(0, 40);
				}
				if (s == 0) {
					if (!t.contains("@")) {
						GestorSalida.ENVIAR_M145_MENSAJE_PANEL_INFORMACION(out, "Email invalido");
						return false;
					}
				} else if (t.contains("@")) {
					GestorSalida.ENVIAR_M145_MENSAJE_PANEL_INFORMACION(out, "Campos con caracteres invalidos");
					return false;
				}
				if (t.isEmpty()) {
					GestorSalida.ENVIAR_M145_MENSAJE_PANEL_INFORMACION(out, "Campos vacios");
					return false;
				}
				datos[s] = t;
			} catch (Exception e) {
				return false;
			}
		}
		final String consultaSQL = "UPDATE `cuentas` SET `email`= ?, `apellido`= ?, `nombre`= ?, `cumpleaños`= ?, `pregunta`= ?, `respuesta`= ?, `actualizar`='1' WHERE `id`= ?";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL);
			declaracion.setString(1, datos[0]);
			declaracion.setString(2, datos[1]);
			declaracion.setString(3, datos[2]);
			declaracion.setString(4, datos[3] + "~" + datos[4] + "~" + datos[5]);
			declaracion.setString(5, datos[6]);
			declaracion.setString(6, datos[7]);
			declaracion.setInt(7, id);
			declaracion.executeUpdate();
			cerrarDeclaracion(declaracion);
			return true;
		} catch (final Exception e) {
			MainMultiservidor.escribirLog("ERROR SQL: " + e.toString());
			MainMultiservidor.escribirLog("LINEA SQL: " + consultaSQL);
			e.printStackTrace();
		}
		return false;
	}
	
	public static long GET_BANEADO(final String cuenta) {
		long i = 0;
		try {
			final String consultaSQL = "SELECT `baneado` FROM `cuentas` WHERE `cuenta` = ? ;";
			final PreparedStatement declaracion = transaccionSQL(consultaSQL);
			declaracion.setString(1, cuenta);
			final ResultSet resultado = consultaSQL(declaracion);
			if (resultado.first()) {
				i = resultado.getLong("baneado");
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			MainMultiservidor.escribirLog("ERROR SQL: " + e.toString());
			e.printStackTrace();
		}
		return i;
	}
	
	public static String GET_IDIOMA(final String cuenta) {
		String str = "";
		try {
			String consultaSQL = "SELECT `idioma` FROM `cuentas` WHERE `cuenta` = ? ;";
			final PreparedStatement declaracion = transaccionSQL(consultaSQL);
			declaracion.setString(1, cuenta);
			final ResultSet resultado = consultaSQL(declaracion);
			if (resultado.first()) {
				str = resultado.getString("idioma");
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			MainMultiservidor.escribirLog("ERROR SQL: " + e.toString());
			e.printStackTrace();
		}
		return str;
	}
	
	public static void SET_BANEADO(final String cuenta, final long baneado) {
		String consultaSQL = "UPDATE `cuentas` SET `baneado` = ? WHERE `cuenta` = ? ;";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL);
			declaracion.setLong(1, baneado);
			declaracion.setString(2, cuenta);
			declaracion.executeUpdate();
			cerrarDeclaracion(declaracion);
		} catch (final Exception e) {
			MainMultiservidor.escribirLog("ERROR SQL: " + e.toString());
			MainMultiservidor.escribirLog("LINEA SQL: " + consultaSQL);
			e.printStackTrace();
		}
	}
	
	public static byte GET_ACTUALIZAR(final String cuenta) {
		byte i = 0;
		try {
			final String consultaSQL = "SELECT `actualizar` FROM `cuentas` WHERE `cuenta` = ? ;";
			final PreparedStatement declaracion = transaccionSQL(consultaSQL);
			declaracion.setString(1, cuenta);
			final ResultSet resultado = consultaSQL(declaracion);
			if (resultado.first()) {
				i = resultado.getByte("actualizar");
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			MainMultiservidor.escribirLog("ERROR SQL: " + e.toString());
			e.printStackTrace();
		}
		return i;
	}
	
	public static byte GET_RANGO(final String cuenta) {
		byte i = 0;
		try {
			String consultaSQL = "SELECT `rango` FROM `cuentas` WHERE `cuenta` = ? ;";
			final PreparedStatement declaracion = transaccionSQL(consultaSQL);
			declaracion.setString(1, cuenta);
			final ResultSet resultado = consultaSQL(declaracion);
			if (resultado.first()) {
				i = resultado.getByte("rango");
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			MainMultiservidor.escribirLog("ERROR SQL: " + e.toString());
			e.printStackTrace();
		}
		return i;
	}
	
	public static String GET_ULTIMA_IP(final String cuenta) {
		String str = "";
		try {
			String consultaSQL = "SELECT `ultimaIP` FROM `cuentas` WHERE `cuenta` = ? ;";
			final PreparedStatement declaracion = transaccionSQL(consultaSQL);
			declaracion.setString(1, cuenta);
			final ResultSet resultado = consultaSQL(declaracion);
			if (resultado.first()) {
				str = resultado.getString("ultimaIP");
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			MainMultiservidor.escribirLog("ERROR SQL: " + e.toString());
			e.printStackTrace();
		}
		return str;
	}
	
	public static String GET_PREGUNTA_SECRETA(final String cuenta) {
		String str = "";
		try {
			String consultaSQL = "SELECT `pregunta` FROM `cuentas` WHERE `cuenta` = ? ;";
			final PreparedStatement declaracion = transaccionSQL(consultaSQL);
			declaracion.setString(1, cuenta);
			final ResultSet resultado = consultaSQL(declaracion);
			if (resultado.first()) {
				str = resultado.getString("pregunta");
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			MainMultiservidor.escribirLog("ERROR SQL: " + e.toString());
			e.printStackTrace();
		}
		return str;
	}
	
	public static String GET_RESPUESTA_SECRETA(final String cuenta) {
		String str = "";
		try {
			String consultaSQL = "SELECT `respuesta` FROM `cuentas` WHERE `cuenta` = ? ;";
			final PreparedStatement declaracion = transaccionSQL(consultaSQL);
			declaracion.setString(1, cuenta);
			final ResultSet resultado = consultaSQL(declaracion);
			if (resultado.first()) {
				str = resultado.getString("respuesta");
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			MainMultiservidor.escribirLog("ERROR SQL: " + e.toString());
			e.printStackTrace();
		}
		return str;
	}
	
	public static String GET_APODO(final String cuenta) {
		String str = "";
		try {
			String consultaSQL = "SELECT `apodo` FROM `cuentas` WHERE `cuenta` = ? ;";
			final PreparedStatement declaracion = transaccionSQL(consultaSQL);
			declaracion.setString(1, cuenta);
			final ResultSet resultado = consultaSQL(declaracion);
			if (resultado.first()) {
				str = resultado.getString("apodo");
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			MainMultiservidor.escribirLog("ERROR SQL: " + e.toString());
			e.printStackTrace();
		}
		return str;
	}
	
	public static String GET_APELLIDO(final String cuenta) {
		String str = "";
		try {
			String consultaSQL = "SELECT `apellido` FROM `cuentas` WHERE `cuenta` = ? ;";
			final PreparedStatement declaracion = transaccionSQL(consultaSQL);
			declaracion.setString(1, cuenta);
			final ResultSet resultado = consultaSQL(declaracion);
			if (resultado.first()) {
				str = resultado.getString("apellido");
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			MainMultiservidor.escribirLog("ERROR SQL: " + e.toString());
			e.printStackTrace();
		}
		return str;
	}
	
	public static boolean GET_APODO_EXISTE(final String apodo) {
		boolean str = false;
		try {
			String consultaSQL = "SELECT * FROM `cuentas` WHERE `apodo` = ? ;";
			final PreparedStatement declaracion = transaccionSQL(consultaSQL);
			declaracion.setString(1, apodo);
			final ResultSet resultado = consultaSQL(declaracion);
			if (resultado.first()) {
				str = true;
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			MainMultiservidor.escribirLog("ERROR SQL: " + e.toString());
			e.printStackTrace();
		}
		return str;
	}
	
	public static long GET_ABONO(final String cuenta) {
		long l = 0;
		try {
			String consultaSQL = "SELECT `abono` FROM `cuentas` WHERE `cuenta` = ? ;";
			final PreparedStatement declaracion = transaccionSQL(consultaSQL);
			declaracion.setString(1, cuenta);
			final ResultSet resultado = consultaSQL(declaracion);
			while (resultado.next()) {
				l = resultado.getLong("abono");
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			MainMultiservidor.escribirLog("ERROR SQL: " + e.toString());
			e.printStackTrace();
		}
		return l;
	}
	
	public static int GET_ID_CUENTA_NOMBRE(final String cuenta) {
		int i = 0;
		try {
			String consultaSQL = "SELECT `id` FROM `cuentas` WHERE `cuenta` = ? ;";
			final PreparedStatement declaracion = transaccionSQL(consultaSQL);
			declaracion.setString(1, cuenta);
			final ResultSet resultado = consultaSQL(declaracion);
			if (resultado.first()) {
				i = resultado.getInt("id");
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			MainMultiservidor.escribirLog("ERROR SQL: " + e.toString());
			e.printStackTrace();
		}
		return i;
	}
	
	public static String GET_CONTRASEÑA_CUENTA(final String cuenta) {
		String str = "";
		try {
			String consultaSQL = "SELECT `contraseña` FROM `cuentas` WHERE `cuenta` = ? ;";
			final PreparedStatement declaracion = transaccionSQL(consultaSQL);
			declaracion.setString(1, cuenta);
			final ResultSet resultado = consultaSQL(declaracion);
			if (resultado.first()) {
				str = resultado.getString("contraseña");
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			MainMultiservidor.escribirLog("ERROR SQL: " + e.toString());
			e.printStackTrace();
		}
		return str;
	}
	
	public static String GET_CONTRASEÑA_SI(final String cuenta, final String respuesta, final String email,
	final String cumpleaños) {
		String str = "";
		try {
			final String consultaSQL = "SELECT `contraseña` FROM `cuentas` WHERE `cuenta` = ? AND `respuesta` = ? AND `email` = ? AND `cumpleaños` = ? ;";
			final PreparedStatement declaracion = transaccionSQL(consultaSQL);
			declaracion.setString(1, cuenta);
			declaracion.setString(2, respuesta);
			declaracion.setString(3, email);
			declaracion.setString(4, cumpleaños);
			final ResultSet resultado = consultaSQL(declaracion);
			if (resultado.first()) {
				str = resultado.getString("contraseña");
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			MainMultiservidor.escribirLog("ERROR SQL: " + e.toString());
			e.printStackTrace();
		}
		return str;
	}
	
	public static String CAMBIAR_CONTRASEÑA(final String cuenta, final String email, final String respuesta,
	final String nuevaPass, final int id) {
		String str = "";
		try {
			final String consultaSQL = "SELECT `contraseña` FROM `cuentas` WHERE `cuenta` = ? AND `respuesta` = ? AND `email` = ? ;";
			final PreparedStatement declaracion = transaccionSQL(consultaSQL);
			declaracion.setString(1, cuenta);
			declaracion.setString(2, respuesta);
			declaracion.setString(3, email);
			final ResultSet resultado = consultaSQL(declaracion);
			if (resultado.first()) {
				try {
					CAMBIAR_CONTRASEÑA_CUENTA(nuevaPass, id);
					str = nuevaPass;
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			MainMultiservidor.escribirLog("ERROR SQL: " + e.toString());
			e.printStackTrace();
		}
		return str;
	}
	
	public static void CAMBIAR_CONTRASEÑA_CUENTA(final String contraseña, final int cuentaID) {
		final String consultaSQL = "UPDATE `cuentas` SET `contraseña`= ? WHERE `id`= ? ;";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL);
			declaracion.setString(1, contraseña);
			declaracion.setInt(2, cuentaID);
			declaracion.executeUpdate();
			cerrarDeclaracion(declaracion);
		} catch (final Exception e) {
			MainMultiservidor.escribirLog("ERROR SQL: " + e.toString());
			MainMultiservidor.escribirLog("LINEA SQL: " + consultaSQL);
		}
	}
	
	public static void UPDATE_ULTIMA_IP(final String ultimaIP, final int cuentaID) {
		String consultaSQL = "UPDATE `cuentas` SET `ultimaIP`= ? WHERE `id`= ? ;";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL);
			declaracion.setString(1, ultimaIP);
			declaracion.setInt(2, cuentaID);
			declaracion.executeUpdate();
			cerrarDeclaracion(declaracion);
		} catch (final Exception e) {
			MainMultiservidor.escribirLog("ERROR SQL: " + e.toString());
			MainMultiservidor.escribirLog("LINEA SQL: " + consultaSQL);
		}
	}
	
	public static void UPDATE_APODO(final String apodo, final int cuentaID) {
		String consultaSQL = "UPDATE `cuentas` SET `apodo`= ? WHERE `id`= ? ;";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL);
			declaracion.setString(1, apodo);
			declaracion.setInt(2, cuentaID);
			declaracion.executeUpdate();
			cerrarDeclaracion(declaracion);
		} catch (final Exception e) {
			MainMultiservidor.escribirLog("ERROR SQL: " + e.toString());
			MainMultiservidor.escribirLog("LINEA SQL: " + consultaSQL);
		}
	}
	
	public static boolean INSERT_BAN_IP(final String ip) {
		final String consultaSQL = "INSERT INTO `banip` (ip) VALUES (?);";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL);
			declaracion.setString(1, ip);
			declaracion.executeUpdate();
			cerrarDeclaracion(declaracion);
			return true;
		} catch (final Exception e) {
			MainMultiservidor.escribirLog("ERROR SQL: " + e.toString());
			MainMultiservidor.escribirLog("LINEA SQL: " + consultaSQL);
			e.printStackTrace();
		}
		return false;
	}
	
	public static boolean ES_IP_BANEADA(final String ip) {
		boolean b = false;
		try {
			final String consultaSQL = "SELECT `ip` FROM `banip` WHERE `ip` = ? ;";
			final PreparedStatement declaracion = transaccionSQL(consultaSQL);
			declaracion.setString(1, ip);
			final ResultSet resultado = consultaSQL(declaracion);
			if (resultado.first()) {
				b = true;
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			MainMultiservidor.escribirLog("ERROR SQL: " + e.toString());
			e.printStackTrace();
		}
		return b;
	}
	
	public static void CARGAR_CUENTAS() {
		try {
			String consultaSQL = "SELECT * from cuentas ;";
			final ResultSet resultado = consultaSQL(consultaSQL);
			while (resultado.next()) {
				Cuenta cuenta = new Cuenta(resultado.getInt("id"), resultado.getString("cuenta").toLowerCase(), resultado
				.getString("apodo"));
				Mundo.addCuenta(cuenta);
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			MainMultiservidor.escribirLog("ERROR SQL: " + e.toString());
			e.printStackTrace();
		}
	}
	
	public static void CARGAR_CUENTA_POR_NOMBRE(final String cuenta) {
		try {
			String consultaSQL = "SELECT * from cuentas WHERE `cuenta` = ? ;";
			final PreparedStatement declaracion = transaccionSQL(consultaSQL);
			declaracion.setString(1, cuenta);
			final ResultSet resultado = consultaSQL(declaracion);
			if (resultado.first()) {
				Cuenta cc = new Cuenta(resultado.getInt("id"), resultado.getString("cuenta").toLowerCase(), resultado
				.getString("apodo"));
				Mundo.addCuenta(cc);
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			MainMultiservidor.escribirLog("ERROR SQL: " + e.toString());
			e.printStackTrace();
		}
	}
}
