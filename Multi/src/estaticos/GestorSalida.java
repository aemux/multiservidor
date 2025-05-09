package estaticos;

import java.io.PrintWriter;
import java.util.Random;
import variables.Cuenta;

public class GestorSalida {
	public static void enviar(final PrintWriter out, String packet) {
		if (out != null && !packet.isEmpty() && !packet.equals("" + (char) 0x00)) {
			packet = Encriptador.aUTF(packet);
			out.print(packet + (char) 0x00);
			out.flush();
		}
	}
	
	public static void ENVIAR_XML_POLICY_FILE(final PrintWriter out) {
		final String packet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"//
		+ "<cross-domain-policy>" + "<site-control permitted-cross-domain-policies=\"all\" />"
		+ "<allow-access-from domain=\"*\" to-ports=\"*\" secure=\"false\" />"
		+ "<allow-http-request-headers-from domain=\"*\" headers=\"*\" secure=\"false\"/>" // s
		+ "</cross-domain-policy>";
		enviar(out, packet);
		if (MainMultiservidor.MOSTRAR_ENVIOS) {
			System.out.println("POLICY FILE: OUT>>  " + packet);
		}
	}
	
	public static void ENVIAR_pong(final PrintWriter out) {
		final String packet = "pong";
		enviar(out, packet);
		if (MainMultiservidor.MOSTRAR_ENVIOS) {
			System.out.println("DOFUS PONG: PERSO>>  " + packet);
		}
	}
	
	public static void ENVIAR_Bv_SONAR_MP3(final PrintWriter _perso, String str) {
		String packet = "Bv" + str;
		enviar(_perso, packet);
		if (MainMultiservidor.MOSTRAR_ENVIOS) {
			System.out.println("SONAR MP3: PERSO>>  " + packet);
		}
	}
	
	public static void ENVIAR_ÑV_ACTUALIZAR_URL_LINK_MP3(final PrintWriter perso) {
		String packet = "ÑV" + MainMultiservidor.URL_LINK_MP3;
		enviar(perso, packet);
	}
	
	public static void ENVIAR_qpong(final PrintWriter out) {
		final String packet = "qpong";
		enviar(out, packet);
		if (MainMultiservidor.MOSTRAR_ENVIOS) {
			System.out.println("DOFUS QPONG: PERSO>>  " + packet);
		}
	}
	
	public static String ENVIAR_HC_CODIGO_LLAVE(final PrintWriter out) {
		final String alfabeto = "abcdefghijklmnopqrstuvwxyz";
		final Random rand = new Random();
		StringBuilder codigoLlave = new StringBuilder();
		for (int i = 0; i < 32; i++) {
			codigoLlave.append(alfabeto.charAt(rand.nextInt(alfabeto.length())));
		}
		final String packet = "HC" + codigoLlave.toString();
		enviar(out, packet);
		if (MainMultiservidor.MOSTRAR_ENVIOS) {
			System.out.println("CODIGO LLAVE: OUT>>" + packet);
		}
		return codigoLlave.toString();
	}
	
	public static void ENVIAR_HR_RECUPERAR_CUENTA(final PrintWriter out, final String str) {
		final String packet = "HR" + str;
		enviar(out, packet);
		if (MainMultiservidor.MOSTRAR_ENVIOS) {
			System.out.println("RECUPERAR CUENTA: OUT>>  " + packet);
		}
	}
	
	public static void ENVIAR_HN_CAMBIAR_PASSWORD(final PrintWriter out, final String str) {
		final String packet = "HN" + str;
		enviar(out, packet);
		if (MainMultiservidor.MOSTRAR_ENVIOS) {
			System.out.println("CAMBIAR PASSWORD: OUT>>  " + packet);
		}
	}
	
	public static void ENVIAR_HF_CONFIRMAR_NUEVA_PASSWORD(final PrintWriter out, final String str) {
		final String packet = "HF" + str;
		enviar(out, packet);
		if (MainMultiservidor.MOSTRAR_ENVIOS) {
			System.out.println("CONFIRMA NUEVA PASS: OUT>>  " + packet);
		}
	}
	
	public static void ENVIAR_HP_PASS_ENVIADA(final PrintWriter out, final String str) {
		final String packet = "HP" + str;
		enviar(out, packet);
		if (MainMultiservidor.MOSTRAR_ENVIOS) {
			System.out.println("PASS ENVIADA: OUT>>  " + packet);
		}
	}
	
	public static void ENVIAR_AlEk_CUENTA_BANEADA_TIEMPO(final PrintWriter out, long tiempo) {
		tiempo -= System.currentTimeMillis();
		final int dia = (int) (tiempo / (1000 * 3600 * 24));
		tiempo %= 1000 * 3600 * 24;
		final int horas = (int) (tiempo / (1000 * 3600));
		tiempo %= 1000 * 3600;
		final int min = (int) (tiempo / (1000 * 60));
		String packet = "AlEk" + dia + "|" + horas + "|" + min;
		enviar(out, packet);
		if (MainMultiservidor.MOSTRAR_ENVIOS) {
			System.out.println("CUENTA BANEADA TIEMPO: CONEXION>>" + packet);
		}
	}
	
	public static void ENVIAR_AlEb_CUENTA_BANEADA_DEFINITIVO(final PrintWriter out) {
		final String packet = "AlEb";
		enviar(out, packet);
		if (MainMultiservidor.MOSTRAR_ENVIOS) {
			System.out.println("CUENTA BANEADA DEFINITIVA: CONEXION>>" + packet);
		}
	}
	
	public static void ENVIAR_AlEv_ERROR_VERSION_DEL_CLIENTE(final PrintWriter out) {
		final String packet = "AlEv" + MainMultiservidor.VERSION_CLIENTE;
		enviar(out, packet);
		if (MainMultiservidor.MOSTRAR_ENVIOS) {
			System.out.println("ERROR VERSION CLIENTE: CONEXION>>" + packet);
		}
	}
	
	public static void ENVIAR_AlEw_MUCHOS_JUG_ONLINE(final PrintWriter out) {
		final String packet = "AlEw";
		enviar(out, packet);
		if (MainMultiservidor.MOSTRAR_ENVIOS) {
			System.out.println("MAX JUG ONLINE: CONEXION>>  " + packet);
		}
	}
	
	public static void ENVIAR_AlEx_NOMBRE_O_PASS_INCORRECTA(final PrintWriter out) {
		final String packet = "AlEx";
		enviar(out, packet);
		if (MainMultiservidor.MOSTRAR_ENVIOS) {
			System.out.println("NOMBRE O PASS INCORRECTA: CONEXION>>" + packet);
		}
	}
	
	public static void ENVIAR_AlEp_CUENTA_NO_VALIDA(final PrintWriter out) {
		final String packet = "AlEp";
		enviar(out, packet);
		if (MainMultiservidor.MOSTRAR_ENVIOS) {
			System.out.println("CUENTA NO VALIDA: CONEXION>>" + packet);
		}
	}
	
	public static void ENVIAR_AlEn_CONEXION_NO_TERMINADA(final PrintWriter out) {
		final String packet = "AlEn";
		enviar(out, packet);
		if (MainMultiservidor.MOSTRAR_ENVIOS) {
			System.out.println("CONEXION NO TERMINADA: CONEXION>>" + packet);
		}
	}
	
	public static void ENVIAR_AlEd_DESCONECTAR_CUENTA_CONECTADA(final PrintWriter out) {
		final String packet = "AlEd";
		enviar(out, packet);
		if (MainMultiservidor.MOSTRAR_ENVIOS) {
			System.out.println("MISMA CUENTA CONECTADA: CONEXION>>" + packet);
		}
	}
	
	public static void ENVIAR_AlEm_SERVER_MANTENIMIENTO(final PrintWriter out) {
		final String packet = "AlEm";
		enviar(out, packet);
		if (MainMultiservidor.MOSTRAR_ENVIOS) {
			System.out.println("SERVER MANTENIMIENTO: CONEXION>>" + packet);
		}
	}
	
	public static void ENVIAR_Af_NUEVA_COLA(final PrintWriter out, final int posicion, final int totalAbo,
	final int totalNonAbo, final String suscribirse, final int colaID) {
		final String packet = "Af" + posicion + "|" + totalAbo + "|" + totalNonAbo + "|" + suscribirse + "|" + colaID;
		enviar(out, packet);
		if (MainMultiservidor.MOSTRAR_ENVIOS) {
			System.out.println("MULTIPAQUETES: CONEXION>>" + packet);
		}
	}
	
	public static void ENVIAR_Aq_MODIFICA_COLA(final PrintWriter out, final int posicion) {
		final String packet = "Aq" + posicion;
		enviar(out, packet);
		if (MainMultiservidor.MOSTRAR_ENVIOS) {
			System.out.println("MULTIPAQUETES: CONEXION>>" + packet);
		}
	}
	
	public static void ENVIAR_Ad_Ac_AH_AlK_AQ_INFO_CUENTA_Y_SERVER(final PrintWriter out, final String apodo,
	final int rango, final String pregunta) {
		final StringBuilder packet = new StringBuilder();
		packet.append("Ad" + apodo + (char) 0x00);
		packet.append("Ac" + "??" + (char) 0x00);// comunidad
		packet.append("AH" + Mundo.packetParaAH() + (char) 0x00);
		packet.append("AlK" + rango + (char) 0x00);
		packet.append("AQ" + pregunta.replace(" ", "+"));
		enviar(out, packet.toString());
		if (MainMultiservidor.MOSTRAR_ENVIOS) {
			System.out.println("CONEXION: OUT>>" + packet.toString());
		}
	}
	
	public static void ENVIAR_AH_ESTADO_SERVIDORES(final PrintWriter out) {
		enviar(out, "AH" + Mundo.packetParaAH());
	}
	
	public static void ENVIAR_AxK_TIEMPO_ABONADO_NRO_PJS(final PrintWriter out, final Cuenta cuenta) {
		final String packet = "AxK" + cuenta.getAbono() + cuenta.getStringPersonajes();
		enviar(out, packet);
		if (MainMultiservidor.MOSTRAR_ENVIOS) {
			System.out.println("TIEMPO ABON NRO PJS: OUT>>" + packet);
		}
	}
	
	public static void ENVIAR_AXK_O_AYK_IP_SERVER(final PrintWriter out, final int cuentaID, final String ipServer,
	final int puertoServer) {
		final StringBuilder packet = new StringBuilder("A");
		if (MainMultiservidor.ENCRIPTAR_IP) {
			packet.append("XK" + Encriptador.encriptarIP(ipServer) + Encriptador.encriptarPuerto(puertoServer) + cuentaID);
		} else {
			packet.append("YK" + ipServer + ":" + puertoServer + ";" + cuentaID);
		}
		enviar(out, packet.toString());
		if (MainMultiservidor.MOSTRAR_ENVIOS) {
			System.out.println("CONEXION IP SERVER: OUT>>" + packet);
		}
	}
	
	public static void ENVIAR_BN_NADA(final PrintWriter out) {
		enviar(out, "BN");
		if (MainMultiservidor.MOSTRAR_ENVIOS) {
			System.out.println("NADA: PERSO>>  " + "BN");
		}
	}
	
	public static void ENVIAR_HU_ACTUALIZAR_DATOS(final PrintWriter out) {
		enviar(out, "HU");
		if (MainMultiservidor.MOSTRAR_ENVIOS) {
			System.out.println("ACTUALIZAR DATOS: PERSO>>  " + "HU");
		}
	}
	
	public static void ENVIAR_HV_CONFIRMADA_ACTUALIZACION_DATOS(final PrintWriter out) {
		enviar(out, "Hu");
		if (MainMultiservidor.MOSTRAR_ENVIOS) {
			System.out.println("CONFIRMADA ACTUALIZACION DATOS: PERSO>>  " + "Hu");
		}
	}
	
	public static void ENVIAR_M0_MENSAJE_SVR_MUESTRA_DISCONNECT(final PrintWriter out, final String id, final String msj,
	final String nombre) {
		final String packet = "M0" + id + "|" + msj + "|" + nombre;
		enviar(out, packet);
		enviar(out, "ATE");
		if (MainMultiservidor.MOSTRAR_ENVIOS) {
			System.out.println("MSJ SERVER: PERSO>>  " + packet);
		}
	}
	
	public static void ENVIAR_M145_MENSAJE_PANEL_INFORMACION(final PrintWriter perso, final String str) {
		String packet = "M145|" + str.replace("\\n", "\n").replace("\\t", "\t").replace("\\r", "\r");
		enviar(perso, packet);
		if (MainMultiservidor.MOSTRAR_ENVIOS) {
			System.out.println("MSJ SERVER: PERSO>>  " + packet);
		}
	}
}
