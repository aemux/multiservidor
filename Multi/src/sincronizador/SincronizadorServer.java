package sincronizador;

import java.net.ServerSocket;
import java.net.Socket;
import estaticos.MainMultiservidor;

public class SincronizadorServer implements Runnable {
	private static ServerSocket _serverSocket;
	private static Thread _thread;
	
	public SincronizadorServer() {
		try {
			_serverSocket = new ServerSocket(MainMultiservidor.PUERTO_SINCRONIZADOR);
			_thread = new Thread(this);
			_thread.setDaemon(true);
			_thread.start();
		} catch (Exception e) {
			if (MainMultiservidor.MOSTRAR_SINCRONIZACION) {
				System.out.println("NO SE PUEDE CREAR EL SINCRONIZADOR SERVER");
			}
			e.printStackTrace();
		}
	}
	
	public void run() {
		try {
			while (true) {
				Socket socket = _serverSocket.accept();
				new SincronizadorSocket(socket);
			}
		} catch (Exception e) {}
	}
}
