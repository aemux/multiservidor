package login;

import java.io.PrintWriter;
import java.util.ArrayList;
import estaticos.GestorSalida;
import variables.Cuenta;

public class EnEspera {
	private static ArrayList<Cuenta> _cuentas = new ArrayList<Cuenta>();
	
	public static void enEspera(final int pendiente, final PrintWriter out) {
		try {
			GestorSalida.ENVIAR_Af_NUEVA_COLA(out, pendiente + 1, 0, 0, "", -1);
		} catch (final Exception e) {
			GestorSalida.ENVIAR_AlEd_DESCONECTAR_CUENTA_CONECTADA(out);
		}
	}
	
	public static int getIndexOf(final Cuenta cuenta) {
		return _cuentas.indexOf(cuenta);
	}
	
	public static void addEspera(final Cuenta cuenta) {
		if (!_cuentas.contains(cuenta)) {
			_cuentas.add(cuenta);
		}
	}
	
	public static void delEspera(final Cuenta cuenta) {
		if (cuenta != null) {
			cuenta.pararTimer();
			_cuentas.remove(cuenta);
		}
	}
	
	public static void suTurno(final Cuenta cuenta, final PrintWriter _out) {
		delEspera(cuenta);
		GestorSalida.ENVIAR_Ad_Ac_AH_AlK_AQ_INFO_CUENTA_Y_SERVER(_out, cuenta.getApodo(), cuenta.getRango() > 0 ? 1 : 0,
		cuenta.getPregunta());
	}
}
