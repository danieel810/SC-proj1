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

	private final static String FILE = "Users.txt";
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
						System.out.println("Wrong password");
						return;
					}
				} else { // User ainda não existe
					outStream.writeObject(3);
					outStream.writeObject("Insert your name");
					String nome = (String) inStream.readObject();
					registaUser(user, passwd, nome);
				}
				boolean b = true;
				while(b) {
					String[] line = ((String) inStream.readObject()).split(" ");
					switch(line[0]) {
					case "f":
					case "follow":
						follow(user, line[1]);
						break;
					case "u":
					case "unfollow":
						unfollow(user, line[1]);
						break;
					case "v":
					case "viewfollowers":
						viewFollowers(user);
						break;
					case "p":
					case "post":
						post(user);
						break;
					case "w":
					case "wall":
						wall(user, Integer.parseInt(line[1]));
						break;
					case "l":
					case "like":
						like(user, line[1]);
						break;
					case "n":
					case "newgroup":
						newGroup(user, line[1]);
						break;
					case "a":
					case "addu":
						addNewMember(user, line[1], line[2]);
						break;
					case "r":
					case "removeu":
						removeMember(user, line[1], line[2]);
						break;
					case "g":
					case "ginfo":
						if(line.length == 2){
							ginfo(user, line[1]);
						}else{
							ginfo(user);
						}
						break;
					case "m":
					case "msg":
						StringBuilder bob = new StringBuilder();
						for (int i = 2; i < line.length; i++) {
							bob.append(line[i] + " ");
						}
						msg(user, line[1], bob.toString());
						break;
					case "c":
					case "collect":
						collect(user, line[1]);
						break;
					case "h":
					case "history":
						history(user, line[1]);
						break;
					default:
						b = false;
						outStream.writeObject("Left\n");
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
				try {
					outStream.writeObject("File does not exist\n");
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			catch(IOException e) {
				e.printStackTrace();				
			} catch (ClassNotFoundException e1) {
				e1.printStackTrace();
			}
		}

		private void history(String user, String groupID) throws IOException {
			List<String> gruposAux = Arrays.asList(getFromDoc("Grupos", "Grupos").split(","));
			List<String> membersAux = Arrays.asList(getFromDoc("Grupos/" + groupID, "Members").split(","));
			if (!gruposAux.contains(groupID)) {
				outStream.writeObject(groupID + " does not exist");
			} else if (!membersAux.contains(user) && !getFromDoc("Grupos/" + groupID, "Owner").equals(user)) {
				outStream.writeObject("You are not a member of group " + groupID);
			} else {
				String[] grupos = getFromDoc("Users/" + user, "Grupos").split(",");
				int id = 0;
				for(String grupo : grupos) {
					String[] info = grupo.split("/");
					if (info[0].equals(groupID)) {
						id = Integer.parseInt(info[1]);
					}
				}
				if (id == 0) {
					outStream.writeObject("Zero messages in your personal history of group" + groupID + "\n");
				} else {
					Scanner sc = new Scanner(new File("Users/" + user + ".txt"));
					StringBuilder bob = new StringBuilder();
					String[] chat = getChat(groupID);
					for (int i = id; i < chat.length; i++) {
						bob.append(chat[i] + "\n");
					}
					if(bob.toString().equals("")) {
						outStream.writeObject("Zero messages in your personal history of group" + groupID + "\n");
					} else {
						outStream.writeObject(bob.toString());
					}
					sc.close();
				}
			}
		}

		private void collect (String user, String groupID) throws NumberFormatException, IOException {
			if (!getFromDoc("Grupos", "Grupos").contains(groupID)) {
				outStream.writeObject(groupID + " does not exist");
			} else if (!getFromDoc("Users/" + user, "Grupos").contains(groupID) && 
					!getFromDoc("Grupos/" + groupID, "Owner").equals(user)) {
				outStream.writeObject("You are not in the group " + groupID);
			} else {
				String[] grupos = getFromDoc("Users/" + user, "Grupos").split(",");
				int currID = 0;
				for (String i : grupos) {
					String[] g = i.split("/");
					if (g[0].equals(groupID)) {
						currID = Integer.parseInt(g[1]);
					}
				}
				int lastID = Integer.parseInt(getFromDoc("Grupos/" + groupID, "ID"));
				String[] chat = getChat(groupID);
				if (chat.length == 0) {
					outStream.writeObject("Chat doesn't contain any message\n");
				} 
				StringBuilder bob = new StringBuilder();
				for (int i = currID; i < lastID; i++) {
					bob.append(chat[i] + "\n");	            	
				}
				if(bob.toString().equals("")){
					outStream.writeObject("There are no new messages\n");
				} else {
					outStream.writeObject(bob.toString());
					changeGID(user, groupID,lastID);
				}
			}
		}
		/**
		 * 
		 * @param user
		 * @param groupID
		 * @param id
		 * @throws FileNotFoundException
		 */

		private void changeGID(String user, String groupID, int id) throws FileNotFoundException {
			Scanner sc = new Scanner(new File("Users/" + user + ".txt"));
			StringBuilder bob = new StringBuilder();
			while(sc.hasNextLine()) {
				String line = sc.nextLine();
				String[] sp = line.split(":");
				if(sp[0].equals("Grupos")) {
					bob.append("Grupos:");
					String[] grupos = sp[1].split(",");
					for(String grupo : grupos) {
						if(grupo.split("/")[0].equals(groupID)) {
							bob.append(groupID + "/" + id + ",");
						} else { 
							bob.append(grupo + ",");
						}
						bob.append("\n");
					}
				} else {
					bob.append(line + "\n");
				}
			}
			PrintWriter pw = new PrintWriter("Users/" + user + ".txt");
			pw.print(bob.toString());
			pw.close();
			sc.close();
		}

		private String[] getChat(String groupID) throws FileNotFoundException {
			Scanner sc = new Scanner(new File("Grupos/" + groupID + ".txt"));
			int ID = Integer.parseInt(getFromDoc("Grupos/" + groupID, "ID"));
			boolean b = false;
			String[] msgs = new String[ID];
			int i = 0;
			while (sc.hasNextLine()) {
				if (b) {
					msgs[i] = sc.nextLine();
					i++;
				} else {
					if (sc.nextLine().equals("Chat:")) {
						b = true;
					}
				}
			}
			sc.close();
			return msgs;
		}

		private void msg(String user, String groupID, String msg) throws IOException {
			List<String> grupos = Arrays.asList(getFromDoc("Grupos", "Grupos").split(","));
			if(grupos.contains(groupID)) {
				List<String> members = Arrays.asList(getFromDoc("Grupos/" + groupID, "Members").split(","));
				if(members.contains(user) || getFromDoc("Grupos/" + groupID, "Owner").equals(user)) {
					int id = Integer.parseInt(getFromDoc("Grupos/" + groupID, "ID"));
					id++;
					changeID("Grupos/" + groupID, id);
					newMessage(groupID, "Chat", msg);
					outStream.writeObject("Message received!");					
				} else {
					outStream.writeObject("You are not in that group!");
				}
			} else {
				outStream.writeObject("Group does not exist!");
			}
		}

		private void newMessage(String groupID, String tag, String info) throws FileNotFoundException{
			Scanner sc = new Scanner(new File("Grupos/" + groupID + ".txt"));
			StringBuilder bob = new StringBuilder();
			while(sc.hasNextLine()) {
				String line = sc.nextLine();
				String[] sp = line.split(":");
				if(sp[0].equals(tag)) {
					bob.append("Chat:\n");
					while(sc.hasNextLine()) {
						bob.append(sc.nextLine() + "\n");
					}
					bob.append(info + System.lineSeparator());
				} else {
					bob.append(line + "\n");
				}
			}
			sc.close();
			PrintWriter pw = new PrintWriter("Grupos/" + groupID + ".txt");
			pw.print(bob.toString());
			pw.close();
		}

		private void ginfo(String user) throws FileNotFoundException, IOException {
			String grupos = getFromDoc("Users/" + user, "Grupos");
			String owner = getFromDoc("Users/" + user, "Owner");
			StringBuilder bob = new StringBuilder();
			if(grupos == ""){
				bob.append("You aren't a member of any group" + "\n");
			}else {
				bob.append("You are member of: ");
				String[] g = grupos.split(",");
				for(String gr : g){
					bob.append(gr.split("/")[0] + ",");
				}
				bob.deleteCharAt(bob.length() - 1);
				bob.append("\n");
			}

			if(owner == ""){
				bob.append("You aren't the owner of any group" + "\n");
			}else {
				bob.append("You are the owner of: " + owner.substring(0, owner.length() -1) + "\n");
			}
			outStream.writeObject(bob.toString());
		}

		private void ginfo(String user, String groupID) throws FileNotFoundException, IOException{
			File grupo = new File("Grupos/" + groupID + ".txt");
			if(grupo.exists()){
				String owner = getFromDoc("Grupos/" + groupID, "Owner");
				List<String> m = Arrays.asList(getFromDoc("Grupos/" + groupID, "Members").split(","));
				if(!user.equals(owner) && !m.contains(user)){
					outStream.writeObject("You aren't the owner of the group\n");
				} else {
					String members = getFromDoc("Grupos/" + groupID, "Members");
					if(members != ""){
						outStream.writeObject("The owner of the group is: " + owner + "\n" +
								"The members of the group are: " + members.substring(0, members.length() -1) + "\n");
					} else{
						outStream.writeObject("The owner of the group is: " + owner + "\n" +
								"The group has no members \n");
					}
				}
			}else {
				outStream.writeObject("The group doesn't exist\n");
			}
		}

		private void removeMember(String owner, String userID, String groupID) throws  IOException {
			List<String> grupos = Arrays.asList(getFromDoc("Grupos", "Grupos").split(","));
			if(grupos.contains(groupID) && getFromDoc("Grupos/" + groupID, "Owner").equals(owner) && !owner.equals(userID)) {
				List<String> members = Arrays.asList(getFromDoc("Grupos/" + groupID, "Members").split(","));
				if(members.contains(userID)) {
					String[] gru = getFromDoc("Users/" + userID, "Grupos").split(",");
					int currID = 0;
					for (String i : gru) {
						String[] g = i.split("/");
						if (g[0].equals(groupID)) {
							currID = Integer.parseInt(g[1]);
						}
					}
					removeFromDoc("Grupos/" + groupID, "Members", userID);
					removeFromDoc("Users/" + userID, "Grupos", groupID + "/" + currID);
					outStream.writeObject("Member removed");
				} else {
					outStream.writeObject("Member isn't in the group");
				}
			} else {
				outStream.writeObject("This isn't the owner of the group");
			}
		}

		private void addNewMember(String owner, String userID, String groupID) throws IOException {
			List<String> grupos = Arrays.asList(getFromDoc("Grupos", "Grupos").split(","));
			if(grupos.contains(groupID) && getFromDoc("Grupos/" + groupID, "Owner").equals(owner) && !owner.equals(userID)) {
				List<String> members = Arrays.asList(getFromDoc("Grupos/" + groupID, "Members").split(","));
				if(!members.contains(userID)) {
					int gID = Integer.parseInt(getFromDoc("Grupos/" + groupID, "ID"));
					addToDoc("Grupos/" + groupID, "Members", userID);
					addToDoc("Users/" + userID, "Grupos", groupID + "/" +  gID);
					outStream.writeObject("Member added");
				} else {
					outStream.writeObject("Member is already in group");
				}
			} else {
				outStream.writeObject("This isn't the owner of the group");
			}
		}

		private void newGroup(String user, String groupID) throws IOException {
			List<String> grupos = Arrays.asList(getFromDoc("Grupos", "Grupos").split(","));
			if(!grupos.contains(groupID)) {
				addToDoc("Grupos", "Grupos", groupID);
				addToDoc("Users/" + user, "Grupos", groupID + "/0");
				addToDoc("Users/" + user, "Owner", groupID);
				PrintWriter pw = new PrintWriter("Grupos/" + groupID + ".txt");
				pw.println("Owner:" + user);
				pw.println("Members:");
				pw.println("ID:0");
				pw.print("Chat:\n");
				pw.close();
				outStream.writeObject("Group created");
			} else {
				outStream.writeObject("Group with that name already exists");
			}

		}

		private void like(String user, String photoID) throws IOException { //photoID é user:id
			String[] profilePhoto = photoID.split(":");
			boolean b = false;
			String[] photos = getFromDoc("Users/" + profilePhoto[0], "Fotos").split(",");
			for(String photo : photos) {
				if(photo.split("/")[0].equals(profilePhoto[1])) {
					removeFromDoc("Users/" + profilePhoto[0], "Fotos", photo);
					int likes = Integer.parseInt(photo.split("/")[1]) + 1;
					String newInfo = photo.split("/")[0] + "/" + likes;
					addToDoc("Users/" + profilePhoto[0], "Fotos", newInfo);
					b = true;
				}
			}
			if(b) {
				outStream.writeObject("Liked photo\n");
			} else {
				outStream.writeObject("Photo does not exist!\n");
			}
		}

		private void wall(String user, int nfotos) throws IOException {
			List<String> seguindo = Arrays.asList(getFromDoc("Users/" + user, "Seguindo").split(","));
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
			String[] photos = getFromDoc("Users/" + user, "Fotos").split(",");
			for(String photo : photos) {
				if(photo.split("/")[0].equals(id)) {
					outStream.writeObject(user + ": (id/likes) " + photo);
				}
			}
		}

		private void sendPhoto(String user, String photo) throws IOException {
			File file = new File("Fotos/" + user + ";" + photo + ".jpg");
			InputStream is = new FileInputStream(file);
			byte[] buffer = new byte[MEGABYTE];
			int length = 0;
			outStream.writeObject(user + ";" + photo + ".jpg");
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

		private void post(String user) throws ClassNotFoundException, IOException {
			int id = Integer.parseInt(getFromDoc("Users/" + user, "ID"));
			id++;
			saveImage(user, id);
			addToDoc("Users/" + user, "Fotos", String.valueOf(id) + "/0");
			addToDoc("Fotos", null, user + ":" + id);
			changeID("Users/" + user, id);
			
			outStream.writeObject("foto adicionada\n");
			outStream.flush();
			inStream.skip(Long.MAX_VALUE); // TODO: Pensar nisto, já pensei não consigo chegar a outra conclusão
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
			FileOutputStream fos = new FileOutputStream("Fotos/" + user + ";" + id + ".jpg");

			byte[] buffer = new byte[MEGABYTE];
			int read = 0;
			int remaining = filesize;
			while((read = inStream.read(buffer, 0, Math.min(buffer.length, remaining))) > 0) {
				remaining -= read;
				fos.write(buffer, 0, read);
			}
			fos.close();
		}
		
		private void viewFollowers(String user) throws IOException{
			Scanner sc = new Scanner(new File("Users/" + user+ ".txt"));
			while(sc.hasNextLine()) {
				String line = sc.nextLine();
				String[] sp = line.split(":");
				if(sp[0].equals("Seguidores")) {
					if(sp.length > 1) {
						outStream.writeObject(sp[1].substring(0, sp[1].length() - 1) + "\n");
					} else {
						outStream.writeObject("You don't have any followers\n");
					}
					break;
				}
			}
			sc.close();
		}

		private void unfollow(String user, String userASeguir) throws IOException {
			if(users.get(userASeguir) != null) { //Caso o userASeguir exista
				if(seguir(user, userASeguir)) {
					removeFromDoc("Users/" + userASeguir, "Seguidores", user);
					removeFromDoc("Users/" + user, "Seguindo", userASeguir);
					outStream.writeObject("User unfollowed\n");
				} else {
					outStream.writeObject("User isn't being followed\n");
				}
			} else {
				outStream.writeObject("User does not exist\n");
			}
		}

		private void follow(String user, String userASeguir) throws IOException {
			if(users.get(userASeguir) != null) { //Caso o userASeguir exista
				if(!seguir(user, userASeguir)) {
					addToDoc("Users/" + userASeguir, "Seguidores", user);
					addToDoc("Users/" + user, "Seguindo", userASeguir);
					outStream.writeObject("User followed\n");
				} else {
					outStream.writeObject("User is already being followed\n");
				}
			} else {
				outStream.writeObject("User does not exist\n");
			}
		}

		private boolean seguir(String user, String userASeguir) throws FileNotFoundException {
			Scanner sc = new Scanner(new File("Users/" + userASeguir + ".txt"));
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
			Scanner sc = new Scanner (doc);
			StringBuilder bob = new StringBuilder();

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
				bob.append(line + "\n");
			}

			PrintWriter pt = new PrintWriter (doc);
			pt.print(bob.toString());
			sc.close();
			pt.close();
		}

		private void addToDoc (String docName, String tag, String info) throws FileNotFoundException{
			File doc = new File(docName + ".txt");
			Scanner sc = new Scanner (doc);
			StringBuilder bob = new StringBuilder();
			if(tag != null) {
				while(sc.hasNextLine()){
					String line = sc.nextLine();
					String[] sp = line.split(":");
					if(sp[0].equals(tag)) {
						line = line + (info + ",");
					} 
					bob.append(line + "\n");
				}
			} else {
				StringBuilder minibob = new StringBuilder();
				while(sc.hasNextLine()){
					String line = sc.nextLine();
					minibob.append(line + "\n");
				}
				bob.append(info + "\n");
				bob.append(minibob.toString() + "\n");
			}
			PrintWriter pt = new PrintWriter (doc);
			pt.print(bob.toString());
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
			PrintWriter t = new PrintWriter("Users/" + user + ".txt");
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
