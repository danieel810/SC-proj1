package client;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class SeiTchiz {

	private static final int MEGABYTE = 1024;
	private static ObjectOutputStream outStream;
	private static ObjectInputStream inStream;
	
	public static void main(String[] args) {
		Socket socket = null;
		String[] AdressEporta = args[0].split(":");
		Scanner sc = new Scanner(System.in);
		try {
			String adress = AdressEporta[0];
			int porta = Integer.parseInt(AdressEporta[1]);
			socket = new Socket(adress, porta);
			String pw = null;
			if (args.length == 2)  {
				System.out.println("Insira a sua password: ");
				pw = sc.nextLine();
			} else {
				pw = args[2];
			}
			String id = args[1];
			outStream = new ObjectOutputStream(socket.getOutputStream());
			outStream.writeObject(id);
			outStream.writeObject(pw);
			inStream = new ObjectInputStream(socket.getInputStream());
			int autenticado = (int) inStream.readObject();
			switch (autenticado) {
			case 1: //User atenticado e deu certo
				//TODO
				break;
			case 2: //User existe mas a pw não é essa
				//TODO
				break;
			case 3: //User não existe
				System.out.println((String)inStream.readObject());
				String nome = sc.nextLine();
				outStream.writeObject(nome);
				break;
			}
			String line = null;
			do {
				System.out.println("Escolha uma opção: ");
				System.out.println("follow <userID>");
				System.out.println("unfollow <userID>");
				System.out.println("viewfollowers");
				System.out.println("post <photo>");
				System.out.println("wall <nPhotos>");
				System.out.println("like <photoID>");
				System.out.println("newgroup <groupID>");
				System.out.println("addu <userID> <groupID>");
				System.out.println("removeu <userID> <groupID>");
				System.out.println("ginfo [groupID]");
				System.out.println("msg <groupID> <msg>");
				System.out.println("collect <groupID>");
				System.out.println("history <groupID>");
				System.out.println("quit");
				line = sc.nextLine();
				outStream.writeObject(line);
				String[] t = line.split("\\s+");
				if(t[0].equals("wall") || t[0].equals("w")){
					wall();	
				} else {
					String ssss = (String) inStream.readObject();
					System.out.println(ssss);
				}
			} while(!line.equals("quit"));
			
			socket.close();
			sc.close();
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	private static void wall() throws ClassNotFoundException, IOException {
		boolean bb = true;
		while(bb){
			bb = (boolean) inStream.readObject();
			if(bb){
				String name = (String) inStream.readObject();
				int filesize = (int) inStream.readObject();
				OutputStream os = new FileOutputStream(name);
				byte[] buffer = new byte[MEGABYTE];
				int read = 0;
				int remaining = filesize;
				while((read = inStream.read(buffer, 0, Math.min(buffer.length, remaining))) > 0) {
					remaining -= read;
					os.write(buffer, 0, read);
				}
				os.close();
			}	
		}
	}
}
