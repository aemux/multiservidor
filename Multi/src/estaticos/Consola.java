package estaticos;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Consola extends Thread {
	private static boolean CONSOLA_ACTIVADA = true;
	
	public Consola() {
		this.setDaemon(true);
		this.setPriority(7);
		this.start();
	}
	
	public void run() {
		while (CONSOLA_ACTIVADA) {
			try {
				final BufferedReader b = new BufferedReader(new InputStreamReader(System.in));
				String linea = b.readLine();
				String str = "";
				try {
					str = linea.substring(linea.indexOf(" ") + 1);
					linea = linea.split(" ")[0];
				} catch (final Exception e2) {}
				leerComandos(linea, str);
			} catch (final Exception e) {
				System.out.println("Error al ingresar texto a la consola"); 
			}
		}
	}
	
	public static void leerComandos(final String linea, final String valor) {
		try {
			switch (linea.toUpperCase()) {
				case "ENVIADOS" :
					MainMultiservidor.MOSTRAR_ENVIOS = valor.equalsIgnoreCase("true");
					break;
				case "RECIBIDOS" :
					MainMultiservidor.MOSTRAR_RECIBIDOS = valor.equalsIgnoreCase("true");
					break;
				case "DEBUG" :
					MainMultiservidor.MODO_DEBUG = valor.equalsIgnoreCase("true");
					break;
				case "DESACTIVAR" :
				case "DESACTIVE" :
				case "DESACTIVER" :
					CONSOLA_ACTIVADA = false;
					System.out.println("CONSOLA DESACTIVADA");
					break;
				// case "DESLOGUEAR" :
				// case "DESLOGUEADOS" :
				// case "LOGS0" :
				// GestorSQL.UPDATE_CUENTAS_LOG_CERO();
				// System.out.println("Logs 0");
				// break;
				case "RELOG" :
				case "RECARGAR" :
					MainMultiservidor.cargarConfiguracion();
					System.out.println("Se recargo la config correctamente");
					break;
				case "EXIT" :
				case "RESET" :
					System.exit(0);
					break;
				default :
					System.out.println("Comando no existe");
					return;
			}
			System.out.println("Comando realizado: " + linea + " -> " + valor);
		} catch (final Exception e) {
			System.err.println("Ocurrio un error con el comando " + linea);
		}
	}
}
