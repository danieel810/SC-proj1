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
					String pw = users.get(user).get(1); // Tudo deu certo;
					if(pw.equals(passwd)) {
						outStream.writeObject(1);
					} else {
						outStream.writeObject(2); // User existe mas a pw não é aquela
						System.out.println("Password errada");
						return;
					}
				} else { // User ainda não existe
					outStream.writeObject(3);
					outStream.writeObject("Insira o seu nome");
					String nome = (String) inStream.readObject();
					registaUser(user, passwd, nome);
				}

				switch((String) inStream.readObject()) {
				case "f":
				case "follow":
					break;
				case "u":
				case "unfollow":
					break;
				case "v":
				case "viewfollowers":
					break;
				case "p":
				case "post":
					break;
				case "w":
				case "wall":
					break;
				case "l":
				case "like":
					break;
				case "n":
				case "newgroup":
					break;
				case "a":
				case "addu":
					break;
				case "r":
				case "removeu":
					break;
				case "g":
				case "ginfo":
					break;
				case "m":
				case "msg":
					break;
				case "c":
				case "collect":
					break;
				case "h":
				case "history":
					break;

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
		@SuppressWarnings("resource")
		private void registaUser(String user, String passwd, String nome) throws FileNotFoundException {
			ArrayList<String> list = new ArrayList<>();
			list.add(nome);
			list.add(passwd);
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
			pw = new PrintWriter(user + ".txt");
			pw.println("User:" + user);
			pw.println("Seguidores:");
			pw.println("Seguindo:");
			pw.println("Fotos:");
			pw.println("Grupos:");
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
