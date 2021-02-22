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
					System.out.println(pw);
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
				String[] line = ((String) inStream.readObject()).split(" ");
				 
				switch(line[0]) {
				case "f":
				case "follow":
					System.out.println("Entrou no follow");
					follow(user, line[1]);
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
				default:
					System.out.println("Entrou no default");
					//Avisar que o cliente foi mongo
					break;
				}

				outStream.close();
				inStream.close();
				socket.close();

			} catch (ArrayIndexOutOfBoundsException e) {
				// Avisar que o stor é mongo
			} catch(IOException e) {
				e.printStackTrace();				
			} catch (ClassNotFoundException e1) {
				e1.printStackTrace();
			}

		}

		private void follow(String user, String userASeguir) {
			if(users.get(userASeguir) != null) {
				System.out.println("Entrou no if");
				try {
					Scanner userASeguirSC = new Scanner(new File(userASeguir + ".txt"));
					StringBuilder bob = new StringBuilder();
					while(userASeguirSC.hasNextLine()) {
						String line = userASeguirSC.nextLine();						
						String[] sp = line.split(":");
						
						if(sp[0].equals("Seguidores")) {
							sp[1] = sp[1] + user + ",";
							bob.append(sp[0] + ":");
							bob.append(sp[1] + "\n");
						} else {
							bob.append(line + "\n");
						}
					}
					System.out.println(bob.toString());
					userASeguirSC.close();
					
					PrintWriter pw = new PrintWriter(userASeguir + ".txt");
					pw.println(bob.toString());
					pw.close();
					
					Scanner eu = new Scanner(new File(user + ".txt"));
					bob = new StringBuilder();
					while(eu.hasNextLine()) {
						String line = eu.nextLine();
						String[] sp = line.split(":");
					}
					
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}
		}

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
			pw.close();
			PrintWriter t = new PrintWriter(user + ".txt");
			t.println("User:" + user);
			t.println("Seguidores: ");
			t.println("Seguindo: ");
			t.println("Fotos: ");
			t.println("Grupos: ");
			t.println("Owner: ");
			t.close();
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
			// user:nome:pw
			String[] credencias = line.split(":");
			ArrayList<String> list = new ArrayList<>();
			list.add(credencias[1]);
			list.add(credencias[2]);
			users.put(credencias[0], list);
		}

		sc.close();
	}
}
