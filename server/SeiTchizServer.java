package server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;

public class SeiTchizServer {

	private final static String FILE = "users.txt";
	private HashMap<String, ArrayList<String>> users = new HashMap<>();

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

				user = (String) inStream.readObject();
				passwd = (String)inStream.readObject();
				System.out.println("thread: depois de receber a password e o user");

				if(users.get(user) != null) {
					outStream.writeObject(true);
				} else {
					outStream.writeObject(false);
					outStream.writeObject("Insira o seu nome");
					String nome = (String) inStream.readObject();
					registaUser(user, passwd, nome);
				}

				outStream.close();
				inStream.close();
				socket.close();

			} catch(IOException e) {
				e.printStackTrace();				
			} catch (ClassNotFoundException e1) {
				e1.printStackTrace();
			}

		}

		private void registaUser(String user, String passwd, String nome) throws FileNotFoundException {
			for(String u : users.keySet()) {
				ArrayList<String> t = users.get(u);
				Iterator<String> y = t.iterator();
				while(y.hasNext()) {
					System.out.println(y.next());
				}
			}
			ArrayList<String> list = new ArrayList<>();
			list.add(passwd);
			list.add(nome);
			users.put(user, list);
			PrintWriter pw = new PrintWriter(FILE);
			for(String s : users.keySet()) {
				pw.print(s + ":");
				ArrayList<String> lista = users.get(s);
				for (int i = 0; i < lista.size(); i++) {
					pw.print(lista.get(i));
					if(i + 1 < lista.size()) {
						pw.print(":");
					}
				}
				pw.println();
			}
			pw.close();
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
			loadUsers();
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

	private void loadUsers() throws FileNotFoundException {
		Scanner sc = new Scanner(new File(FILE));
		while(sc.hasNextLine()) {
			String line = sc.nextLine();
			String[] credencias = line.split(":");
			ArrayList<String> list = new ArrayList<>();
			list.add(credencias[0]);
			list.add(credencias[1]);
			users.put(credencias[0], list);
		}
		
		sc.close();
	}
}
