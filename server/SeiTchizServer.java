package server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class SeiTchizServer {

	private final static String FILE = "users.txt";
	private HashMap<String, ArrayList<String>> users = new HashMap<>();

	class ServerThread implements Runnable{

		private final int MEGABYTE = 1024;
		private Socket socket = null;
		private ObjectOutputStream outStream = null;
		private ObjectInputStream inStream = null;

		ServerThread(Socket inSoc) {
			socket = inSoc;
		}

		public void run() {
			try {
				outStream = new ObjectOutputStream(socket.getOutputStream());
				inStream = new ObjectInputStream(socket.getInputStream());

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
				boolean b = true;
				while(b) {
					String[] line = ((String) inStream.readObject()).split(" ");
					switch(line[0]) {
					case "f":
					case "follow":
						System.out.println("Entrou no follow");
						follow(user, line[1]);
						outStream.writeObject("followed");
						break;
					case "u":
					case "unfollow":
						unfollow(user, line[1]);
						outStream.writeObject("unfollowed");
						break;
					case "v":
					case "viewfollowers":
						outStream.writeObject(viewFollowers(user));
						break;
					case "p":
					case "post":
						System.out.println("Entrou no post");
						post(user);
						break;
					case "w":
					case "wall":
						System.out.println("Entra no wall");
						wall(user, Integer.parseInt(line[1]));
						break;
					case "l":
					case "like":
						like(user, line[1]);
						break;
					case "n":
					case "newgroup":
						newGroup(user, line[1]);
						outStream.writeObject("Group created");
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
						b = false;
						outStream.writeObject("saiu");
						break;
					}	
				}

				outStream.close();
				inStream.close();
				socket.close();

			} catch (ArrayIndexOutOfBoundsException e) {
				// Avisar que o stor é mongo
			} catch(FileNotFoundException e){
				System.out.println("Ficheiro não existe");
			}
			catch(IOException e) {
				e.printStackTrace();				
			} catch (ClassNotFoundException e1) {
				e1.printStackTrace();
			}

		}

		private void newGroup(String user, String groupID) throws FileNotFoundException {
			addToDoc(user, "Grupos", groupID);
			addToDoc(user, "Owner", groupID);
			PrintWriter pw = new PrintWriter(groupID + ".txt");
			pw.println("Owner:" + user);
			pw.println("Members:");
			pw.println("Chat:");
			pw.close();
		}

		private void like(String user, String photoID) throws FileNotFoundException { //photoID é user:id
			String[] profilePhoto = photoID.split(":");
			String[] photos = getFromDoc(profilePhoto[0], "Fotos").split(",");
			for(String photo : photos) {
				if(photo.split("/")[0].equals(profilePhoto[1])) {
					removeFromDoc(profilePhoto[0], "Fotos", photo);
					String newInfo = photo.split("/")[0] + "/" + Integer.parseInt(photo.split("/")[1] + 1);
					addToDoc(profilePhoto[0], "Fotos", newInfo);
				}
			}
		}

		private void wall(String user, int nfotos) throws IOException {
			List<String> seguindo = Arrays.asList(getFromDoc(user, "Seguindo").split(","));
			Scanner fotos = new Scanner(new File("Fotos.txt"));
			while(fotos.hasNextLine()) {
				String[] t = fotos.nextLine().split(":");
				if(seguindo.contains(t[0]) && nfotos > 0) {
					outStream.writeObject(nfotos > 0);
					nfotos--;
					sendPhoto(t[0], t[1]);
					sendIDAndLikes(t[0], t[1]);
				}
			}
			outStream.writeObject(false);
			fotos.close();
		}

		private void sendIDAndLikes(String user, String id) throws IOException {
			String[] photos = getFromDoc(user, "Fotos").split(",");
			for(String photo : photos) {
				if(photo.split("/")[0].equals(id)) {
					outStream.writeObject(user + ": (id/likes) " + photo);
				}
			}
		}

		private void sendPhoto(String user, String photo) throws IOException {
			File file = new File(user + ";" + photo + ".jpg");
			InputStream is = new FileInputStream(file);
			byte[] buffer = new byte[MEGABYTE];
			int length = 0;
			outStream.writeObject(photo + ".jpg");
			int filesize = (int) file.length();
			outStream.writeObject(filesize);
			while((length = is.read(buffer, 0, buffer.length)) > 0) {
				outStream.write(buffer, 0, length);
			}
			is.close();
		}

		private String getFromDoc(String docName, String tag) throws FileNotFoundException {
			Scanner sc = new Scanner(new File(docName + ".txt"));
			while(sc.hasNextLine()){
				String line = sc.nextLine();
				String[] sp = line.split(":");
				if(sp[0].equals(tag)) {
					if(sp.length > 1){
						sc.close();
						return sp[1];
					}
				}
			}
			sc.close();
			return "";
		}

		private String viewFollowers(String user) throws FileNotFoundException{
			Scanner sc = new Scanner(new File(user+ ".txt"));
			while(sc.hasNextLine()) {
				String line = sc.nextLine();
				String[] sp = line.split(":");
				if(sp[0].equals("Seguidores")) {
					sc.close();
					if(sp.length > 1) {
						return sp[1].substring(0, sp[1].length() - 1);
					} else {
						return "Não tem followers";
					}
				}
			}
			sc.close();
			return "erro";
		}
		private void post(String user) throws ClassNotFoundException, IOException {
			int id = Integer.parseInt(getFromDoc(user, "ID"));
			id++;

			saveImage(user, id);


			addToDoc(user, "Fotos", String.valueOf(id) + "/0");
			addToDoc("Fotos", null, user + ":" + id);
			changeID(user, id);

		}

		private void changeID(String user, int id) throws FileNotFoundException {
			Scanner sc = new Scanner(new File(user + ".txt"));
			StringBuilder bob = new StringBuilder();
			while(sc.hasNextLine()) {
				String line = sc.nextLine();
				String[] sp = line.split(":");
				if(sp[0].equals("ID")) {
					bob.append("ID:" + id + "\n");
				} else {
					bob.append(line + "\n");
				}
			}
			PrintWriter pw = new PrintWriter(user + ".txt");
			pw.print(bob.toString());
			pw.close();
			sc.close();
		}

		private void saveImage(String user, int id) throws ClassNotFoundException, IOException {          
			int filesize = (int) inStream.readObject();

			FileOutputStream fos = new FileOutputStream(user + ";" + id + ".jpg");

			byte[] buffer = new byte[MEGABYTE];
			int read = 0;
			int remaining = filesize;
			while((read = inStream.read(buffer, 0, Math.min(buffer.length, remaining))) > 0) {
				remaining -= read;
				fos.write(buffer, 0, read);
			}
			fos.close();
			outStream.writeObject("foto adicionada");
			outStream.flush();
			inStream.skip(100000);
			System.out.println("Saiu do post");
		}


		private void unfollow(String user, String userASeguir) {
			if(users.get(userASeguir) != null) {
				try {
					removeFromDoc(userASeguir, "Seguidores", user);

					removeFromDoc(user, "Seguindo", userASeguir);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}
		}

		private void follow(String user, String userASeguir) {
			if(users.get(userASeguir) != null) {
				try {
					if(!seguir(user, userASeguir)) {
						addToDoc(userASeguir, "Seguidores", user);

						addToDoc(user, "Seguindo", userASeguir);
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}
		}

		private boolean seguir(String user, String userASeguir) throws FileNotFoundException {
			Scanner sc = new Scanner(new File(userASeguir + ".txt"));
			while(sc.hasNextLine()) {
				String line = sc.nextLine();
				String[] sp = line.split(":");
				if(sp[0].equals("Seguidores")) {
					if(sp.length > 1) {
						sc.close();
						return sp[1].contains(user + ",");
					}
				}
			}
			sc.close();
			return false;
		}

		private void removeFromDoc (String docName, String tag, String info) throws FileNotFoundException{
			File doc = new File(docName + ".txt");
			File temp = new File("temp.txt");
			Scanner sc = new Scanner (doc);
			PrintWriter pt = new PrintWriter (temp);
			while(sc.hasNextLine()){
				String line = sc.nextLine();
				String[] sp = line.split(":");
				if(sp[0].equals(tag)) {
					String[] aux = line.split(info + ",");
					if(aux.length > 1) {
						line = aux[0] + aux[1];
					} else {
						line = aux[0];
					}
				} 
				pt.println(line);
			}
			doc.delete();
			temp.renameTo(doc);
			sc.close();
			pt.close();
		}

		private void addToDoc (String docName, String tag, String info) throws FileNotFoundException{
			File doc = new File(docName + ".txt");
			File temp = new File("temp.txt");
			Scanner sc = new Scanner (doc);
			PrintWriter pt = new PrintWriter (temp);
			if(tag != null) {
				while(sc.hasNextLine()){
					String line = sc.nextLine();
					String[] sp = line.split(":");
					if(sp[0].equals(tag)) {
						line = line + (info + ",");
					} 
					pt.println(line);
				}
			} else {
				StringBuilder bob = new StringBuilder();
				while(sc.hasNextLine()){
					String line = sc.nextLine();
					bob.append(line + "\n");
				}
				pt.println(info);
				pt.print(bob.toString());
			}
			doc.delete();
			temp.renameTo(doc);
			sc.close();
			pt.close();
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
			t.println("Seguidores:");
			t.println("Seguindo:");
			t.println("Fotos:");
			t.println("ID:0");
			t.println("Grupos:");
			t.print("Owner:");
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
