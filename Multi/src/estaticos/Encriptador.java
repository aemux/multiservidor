package estaticos;
public class Encriptador {
	private static char[] HASH = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q',
	'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',
	'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-',
	'_'};// q = 16, N = 40, - = 63
	public static final String ABC_MIN = "abcdefghijklmnopqrstuvwxyz";
	public static final String ABC_MAY = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	public static final String VOCALES = "aeiouAEIOU";
	public static final String CONSONANTES = "bcdfghjklmnpqrstvwxyzBCDFGHJKLMNPQRSTVWXYZ";
	public static final String NUMEROS = "0123456789";
	public static final String GUIONES = "_-";
	
	public static String palabraAleatorio(final int limite) {
		final StringBuilder nombre = new StringBuilder();
		int i = (int) Math.floor(Math.random() * ABC_MAY.length());
		char temp = ABC_MAY.charAt(i);
		nombre.append(temp);
		char xxx;
		while (nombre.length() < limite) {
			i = (int) Math.floor(Math.random() * ABC_MIN.length());
			xxx = ABC_MIN.charAt(i);
			if (temp == xxx || (VOCALES.contains(temp + "") && VOCALES.contains(xxx + ""))
			|| (CONSONANTES.contains(temp + "") && CONSONANTES.contains(xxx + ""))) {
				continue;
			}
			temp = xxx;
			nombre.append(xxx);
		}
		return nombre.toString();
	}
	
	public static String filtro(final String s) {
		final StringBuilder filtrado = new StringBuilder();
		final char[] filtros = {'\'', '\"', '\\', '=', '#', '/', '!', '`', '+', '$', '%'};
		for (final char x : s.toCharArray()) {
			boolean paso = true;
			for (final char f : filtros) {
				if (x == f) {
					paso = false;
					break;
				}
			}
			if (!paso) {
				continue;
			}
			filtrado.append(x + "");
		}
		return filtrado.toString();
	}
	
	public static String encriptarContraseña(final String key, final String contraseña) {
		StringBuilder encriptado = new StringBuilder("#1");
		for (int i = 0; i < contraseña.length(); i++) {
			final char charPass = contraseña.charAt(i);
			final char charKey = key.charAt(i);
			final int a = charPass / 16;
			final int b = charPass % 16;
			final int a1 = (a + charKey) % HASH.length;
			final int b1 = (b + charKey) % HASH.length;
			encriptado.append(HASH[a1]);
			encriptado.append(HASH[b1]);
		}
		return encriptado.toString();
	}
	
	public static String desencriptarContraseña(final String contraseña, final String key) {
		int l1, l2, l3, l4, l5;
		String l7 = "";
		final String abecedario = ABC_MIN + ABC_MAY + GUIONES;
		for (l1 = 0; l1 <= contraseña.length() - 1; l1 += 2) {
			l3 = key.charAt(l1 / 2);
			l2 = abecedario.indexOf(contraseña.charAt(l1));
			l4 = 64 + l2 - l3;
			final int l11 = l1 + 1;
			l2 = abecedario.indexOf(contraseña.charAt(l11));
			l5 = 64 + l2 - l3;
			if (l5 < 0) {
				l5 = 64 + l5;
			}
			l7 = l7 + (char) (16 * l4 + l5);
		}
		return l7;
	}
	
	public static String encriptarIP(final String IP) {
		final String[] split = IP.split("\\.");
		final StringBuilder encriptado = new StringBuilder();
		int cantidad = 0;
		for (int i = 0; i < 50; i++) {
			for (int o = 0; o < 50; o++) {
				if (((i & 15) << 4 | o & 15) == Integer.parseInt(split[cantidad])) {
					final Character A = (char) (i + 48);
					final Character B = (char) (o + 48);
					encriptado.append(A.toString() + B.toString());
					i = 0;
					o = 0;
					cantidad++;
					if (cantidad == 4) {
						return encriptado.toString();
					}
				}
			}
		}
		return "DD";
	}
	
	public static String encriptarPuerto(final int puerto) {
		int P = puerto;
		final StringBuilder numero = new StringBuilder();
		for (int a = 2; a >= 0; a--) {
			numero.append(HASH[(int) (P / Math.pow(64, a))]);
			P = P % (int) Math.pow(64, a);
		}
		return numero.toString();
	}
	
	public static String celdaIDACodigo(final int celdaID) {
		final int char1 = celdaID / 64, char2 = celdaID % 64;
		return HASH[char1] + "" + HASH[char2];
	}
	
	public static short celdaCodigoAID(final String celdaCodigo) {
		final char char1 = celdaCodigo.charAt(0), char2 = celdaCodigo.charAt(1);
		short code1 = 0, code2 = 0, a = 0;
		while (a < HASH.length) {
			if (HASH[a] == char1) {
				code1 = (short) (a * 64);
			}
			if (HASH[a] == char2) {
				code2 = a;
			}
			a++;
		}
		return (short) (code1 + code2);
	}
	
	public static short getNumeroPorValorHash(final char c) {
		for (short a = 0; a < HASH.length; a++) {
			if (HASH[a] == c) {
				return a;
			}
		}
		return -1;
	}
	
	public static char getValorHashPorNumero(final int c) {
		return HASH[c];
	}
	
	public static String aUTF(final String entrada) {
		String out = "";
		try {
			out = new String(entrada.getBytes("UTF-8"));
		} catch (final Exception e) {
			System.out.println("Conversion en UTF-8 fallida! : " + e.getMessage());
		}
		return out;
	}
	
	public static String aUnicode(final String entrada) {
		String out = "";
		try {
			out = new String(entrada.getBytes(), "UTF-8");
		} catch (final Exception e) {
			System.out.println("Conversion en UNICODE fallida! : " + e.getMessage());
		}
		return out;
	}
}
