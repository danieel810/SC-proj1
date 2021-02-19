package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class SeiTchizServer {
	
	private final static String FILE = "users.txt";

	class ServerThread implements Runnable{

		private Socket socket = null;

		ServerThread(Socket inSoc) {
			socket = inSoc;
		}

		public void run() {
			try {
				ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
				ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());
				
				String user = null;
				String passwd = null;
				
				try {
					user = (String) inStream.readObject();
					passwd = (String)inStream.readObject();
					System.out.println("thread: depois de receber a password e o user");
				} catch (ClassNotFoundException e1) {
					e1.printStackTrace();
				}
				
				if(autenticado(user,passwd)) {
					outStream.writeObject(true);
				} else {
					outStream.writeObject(false);
				}
				
				outStream.close();
				inStream.close();
				socket.close();
				
			} catch(IOException e) {
				e.printStackTrace();				
			}

		}

		private boolean autenticado(String user, String passwd) {
			// TODO fazer autenticacao
			return false;
		}

	}

	public static void main(String[] args) {
		System.out.println("servidor: main");
		SeiTchizServer server = new SeiTchizServer();
		server.startServer(Integer.parseInt(args[0]));
	}

	@SuppressWarnings("resource")
	private void startServer(int port) {
		ServerSocket sSoc = null;
		try {
			sSoc = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
		}

		while(true) {
			try {
				Socket inSoc = sSoc.accept();
				ServerThread newServerThread = new ServerThread(inSoc);
				newServerThread.run();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		//sSoc.close();
	}
}
