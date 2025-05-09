package estaticos;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Calendar;
import login.LoginServer;
import sincronizador.SincronizadorServer;
import variables.Servidor;

public class MainMultiservidor {
	public static boolean ES_LOCALHOST = false;
	public static String SONIDO_BIENVENIDA = "";
	public static String URL_LINK_MP3 = "http://localhost/mp3/";
	public static String DIRECTORIO_LOCAL_MP3 = "C://wamp/www/mp3/";
	private static String ARCHIVO_CONFIG = "multi.json";
	public static Calendar DATE_ERROR;
	public static Calendar DATE_ESTADISTICA;
	public static PrintStream LOG_ERRORES;
	public static PrintStream LOG_ESTADISTICAS;
	public static boolean ENCRIPTAR_IP;
	public static boolean PERMITIR_MULTICUENTA = true;
	public static boolean MOSTRAR_RECIBIDOS;
	public static boolean MOSTRAR_ENVIOS;
	public static boolean MOSTRAR_SINCRONIZACION;
	public static boolean MODO_DEBUG;
	public static boolean PARAM_ANTI_DDOS;
	public static boolean PARAM_MOSTRAR_IP;
	public static boolean PARAM_MOSTRAR_EXCEPTIONS;
	public static boolean ACTIVAR_FILA_ESPERA = true;
	public static boolean ACCESO_VIP;
	public static int PUERTO_MULTISERVIDOR = 444;
	public static int PUERTO_SINCRONIZADOR = 19999;
	// public static ArrayList<String> IP_SERVIDOR = new ArrayList<String>();// 25.91.217.194 -
	// 213.152.29.73
	public static String VERSION_CLIENTE = "1.29.1";
	public static String BD_HOST = "localhost";
	public static String BD_USUARIO = "root";
	public static String BD_PASS = "";
	public static String BD_CUENTAS = "";
	public static int MILISEGUNDOS_SIG_CONEXION = 500;
	public static int SEGUNDOS_INFO_STATUS = 60;
	public static int SEGUNDOS_ESTADISTICAS = 300;
	public static int SEGUNDOS_TRANSACCION_BD = 10;
	public static int SEGUNDOS_ESPERA = 15;
	public static int LIMITE_JUGADORES = 100;
	// public static int MAX_CONEXIONES_POR_IP = 8;
	public static byte MAX_CUENTAS_POR_IP = 8;
	public static byte MAX_CONEXION_POR_SEGUNDO = 10;
	
	public static void main(final String[] args) {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				MainMultiservidor.cerrarServer();
			}
		});
		System.out.println("");
		System.out.println("\u001B[31m ---------------------------------------------\u001B[0m"); 
		System.out.println("\u001B[31m|            MULTISERVIDOR V1.47.5            |\u001B[0m"); 
		System.out.println("\u001B[31m ---------------------------------------------\u001B[0m"); 		
		System.out.println("");			
		System.out.println("\t\t\u001B[32mCreado Por:\u001B[0m \u001B[33mAemux\u001B[0m");	
		System.out.println("");		
		System.out.println("\t\u001B[5m\u001B[31m[!] Inicializando modulos...\u001B[0m");
				
		cargarConfiguracion();
		crearLogEstadisticas();
		if (GestorSQL.iniciarConexion()) {
			System.out.println("Módulo Exitoso");
		} else {
			escribirLog("CONEXION SQL INVALIDA!!");
			System.exit(1);
			return;
		}
		System.out.print("\u001B[32mInicializando Módulo MultiServidor: \u001B[0m");		
		
		Mundo.crearMultiServer();
	
		
		new SincronizadorServer();
		new LoginServer();
		new Consola();
		System.out.println("\u001B[32mEsperando que los jugadores se conecten:\u001B[0m");

	}
	
	public static void crearLogErrores() {
		while (true) {
			try {
				DATE_ERROR = Calendar.getInstance();
				final String date = DATE_ERROR.get(Calendar.DAY_OF_MONTH) + "-" + (DATE_ERROR.get(Calendar.MONTH) + 1) + "-"
				+ DATE_ERROR.get(Calendar.YEAR);
				LOG_ERRORES = new PrintStream(new FileOutputStream("Logs_MultiServidor/Log_" + date + ".txt", true));
				LOG_ERRORES.println("---------- INICIO DEL MULTISERVIDOR ----------");
				LOG_ERRORES.flush();
				System.setErr(LOG_ERRORES);
				break;
			} catch (final IOException e) {
				new File("Logs_MultiServidor").mkdir();
			} catch (Exception e) {
				break;
			}
		}
	}
	
	public static void crearLogEstadisticas() {
		while (true) {
			try {
				DATE_ESTADISTICA = Calendar.getInstance();
				final String date = DATE_ESTADISTICA.get(Calendar.DAY_OF_MONTH) + "-"
				+ (DATE_ESTADISTICA.get(Calendar.MONTH) + 1) + "-" + DATE_ESTADISTICA.get(Calendar.YEAR);
				LOG_ESTADISTICAS = new PrintStream(new FileOutputStream("Estadisticas_Multiservidor/Conectados_" + date
				+ ".txt", true));
				LOG_ESTADISTICAS.flush();
				break;
			} catch (final IOException e) {
				new File("Estadisticas_Multiservidor").mkdir();
			} catch (Exception e) {
				break;
			}
		}
	}
	
	public static void cargarConfiguracion() {
		try {
			final BufferedReader config = new BufferedReader(new FileReader(ARCHIVO_CONFIG));
			String linea = "";
			ArrayList<String> comandos = new ArrayList<>();
			while ((linea = config.readLine()) != null) {
				try {
					final String param = linea.split("=")[0].trim();
					final String valor = linea.split("=")[1].trim();
					if (comandos.contains(param)) {
						System.out.println("En el " + ARCHIVO_CONFIG + " se repite el comando " + param);
						System.exit(1);
						return;
					} else {
						comandos.add(param);
					}
					switch (param.toUpperCase()) {
						case "SOUND_WELCOME" :
						case "MP3_WELCOME" :
						case "SONIDO_BIENVENIDA" :
							String idioma = valor.split(" ", 2)[0];
							String sonido = valor.split(" ", 2)[1];
							String v = TextoAVoz.crearMP3(sonido, idioma);
							if (v != null && !v.isEmpty()) {
								SONIDO_BIENVENIDA = v;
							}
							break;
						case "DIRECTORIO_LOCAL_MP3" :
							DIRECTORIO_LOCAL_MP3 = valor;
							break;
						case "URL_LINK_MP3" :
							URL_LINK_MP3 = valor;
							break;
						case "MOSTRAR_ENVIADOS" :
						case "ENVIADOS" :
							MOSTRAR_ENVIOS = valor.equalsIgnoreCase("true");
							break;
						case "SINCRONIZADOS" :
						case "MOSTRAR_SINCRONIZACION" :
							MOSTRAR_SINCRONIZACION = valor.equalsIgnoreCase("true");
							break;
						case "MOSTRAR_RECIBIDOS" :
						case "RECIBIDOS" :
							MOSTRAR_RECIBIDOS = valor.equalsIgnoreCase("true");
							break;
						case "MODO_DEBUG" :
							MODO_DEBUG = valor.equalsIgnoreCase("true");
							break;
						case "PARAM_ANTI_DDOS" :
							PARAM_ANTI_DDOS = valor.equalsIgnoreCase("true");
							break;
						case "PARAM_MOSTRAR_IP" :
							PARAM_MOSTRAR_IP = valor.equalsIgnoreCase("true");
							break;
						case "PARAM_MOSTRAR_EXCEPTIONS" :
							PARAM_MOSTRAR_EXCEPTIONS = valor.equalsIgnoreCase("true");
							break;
						case "ACTIVAR_FILA_ESPERA" :
							ACTIVAR_FILA_ESPERA = valor.equalsIgnoreCase("true");
							break;
						case "ACCESO_VIP" :
							ACCESO_VIP = valor.equalsIgnoreCase("true");
							break;
						case "ENCRIPTAR_IP" :
							ENCRIPTAR_IP = valor.equalsIgnoreCase("true");
							break;
						case "VERSION_CLIENTE" :
							VERSION_CLIENTE = valor;
							break;
						case "PUERTO_MULTISERVER" :
						case "PUERTO_MULTISERVIDOR" :
							PUERTO_MULTISERVIDOR = Integer.parseInt(valor);
							break;
						case "PUERTO_SINCRONIZACION" :
						case "PUERTO_SINCRONIZADOR" :
							PUERTO_SINCRONIZADOR = Integer.parseInt(valor);
							break;
						case "BD_HOST" :
							BD_HOST = valor;
							break;
						case "BD_USER" :
						case "BD_USUARIO" :
							BD_USUARIO = valor;
							break;
						case "BD_PASSWORD" :
						case "BD_CONTRASEÃ‘A" :
						case "BD_PASS" :
							BD_PASS = valor;
							break;
						case "BD_ACCOUNTS" :
						case "BD_COMPTES" :
						case "BD_CUENTAS" :
						case "BD_LOGIN" :
						case "BD_REALM" :
							BD_CUENTAS = valor;
							break;
						case "MAX_CUENTAS_POR_IP" :
							MAX_CUENTAS_POR_IP = Byte.parseByte(valor);
							break;
						// case "MAX_CONEXIONES_POR_IP" :
						// MAX_CONEXIONES_POR_IP = Integer.parseInt(valor);
						// break;
						case "SEGUNDOS_ESPERA" :
						case "TIEMPO_ESPERA" :
							SEGUNDOS_ESPERA = Integer.parseInt(valor);
							break;
						case "SEGUNDOS_TRANSACCION_BD" :
						case "TIEMPO_TRANSACCION_BD" :
							SEGUNDOS_TRANSACCION_BD = Integer.parseInt(valor);
							break;
						case "SEGUNDOS_INFO_STATUS" :
						case "TIEMPO_INFO_STATUS" :
							SEGUNDOS_INFO_STATUS = Integer.parseInt(valor);
							break;
						case "SEGUNDOS_ESTADISTICAS" :
							SEGUNDOS_ESTADISTICAS = Integer.parseInt(valor);
							break;
						case "MAX_CONEXION_POR_SEGUNDO" :
						case "CONEXION_SEGUNDO" :
							MAX_CONEXION_POR_SEGUNDO = (byte) (Byte.parseByte(valor) - 1);
							break;
						case "MILISEGUNDOS_SIG_CONEXION" :
						case "TIEMPO_SIG_CONEXION" :
							MILISEGUNDOS_SIG_CONEXION = Integer.parseInt(valor);
							break;
						case "ENABLED_MULTIACCOUNT" :
						case "PERMITIR_MULTICUENTA" :
							PERMITIR_MULTICUENTA = valor.equalsIgnoreCase("true");
							break;
						case "LIMITE_JUGADORES" :
							LIMITE_JUGADORES = Short.parseShort(valor);
							break;
						case "CONFIG_SERVERS" :
							final String[] s = valor.split(";");
							for (final String sx : s) {
								try {
									final int id = Integer.parseInt(sx.split(",")[0]);
									final int puerto = Integer.parseInt(sx.split(",")[1]);
									Mundo.Servidores.put(id, new Servidor(id, puerto, Servidor.SERVIDOR_OFFLINE));
								} catch (final Exception e) {
									System.out.println("ERROR EN CONFIG_SERVER " + e.toString());
								}
							}
							break;
					}
				} catch (Exception e) {}
			}
			config.close();
		} catch (final Exception e) {
			System.out.println(e.toString());
			System.out.println("Ficha de la configuraciÃ³n no existe o ilegible");
		}
	}
	
	public static void escribirLog(final String str) {
		System.out.println(str);
		try {
			Calendar temp = Calendar.getInstance();
			if (temp.get(Calendar.DAY_OF_YEAR) != DATE_ERROR.get(Calendar.DAY_OF_YEAR)) {
				crearLogErrores();
			}
			final String hora = temp.get(Calendar.HOUR_OF_DAY) + ":" + temp.get(+Calendar.MINUTE) + ":"
			+ temp.get(Calendar.SECOND);
			LOG_ERRORES.println("[" + hora + "]  " + str);
			LOG_ERRORES.flush();
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void escribirEstadisticas() {
		try {
			Calendar temp = Calendar.getInstance();
			if (temp.get(Calendar.DAY_OF_YEAR) != DATE_ESTADISTICA.get(Calendar.DAY_OF_YEAR)) {
				crearLogEstadisticas();
			}
			final String hora = temp.get(Calendar.HOUR_OF_DAY) + ":" + temp.get(+Calendar.MINUTE) + ":"
			+ temp.get(Calendar.SECOND);
			LOG_ESTADISTICAS.printf("%-8s", hora);
			for (final Servidor server : Mundo.Servidores.values()) {
				LOG_ESTADISTICAS.printf("\tSERVIDOR_%-4d(%1d):%3d", server.getID(), server.getEstado(), server.getConectados());
			}
			LOG_ESTADISTICAS.println();
			LOG_ESTADISTICAS.flush();
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void cerrarServer() {
		System.out.println("SE ESTA INICIANDO EL CIERRE DEL SERVIDOR ...");
		GestorSQL.cerrarConexion();
		System.out.println("EL SERVIDOR SE CERRO EXITOSAMENTE");
	}
	
	public static void infoStatus() {
		try {
			BufferedWriter mod = new BufferedWriter(new FileWriter("info_status.php"));
			mod.write("<?php " + Mundo.infoStatus() + "?>");
			mod.flush();
			mod.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
