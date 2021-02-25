package client;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Scanner;

import javax.imageio.ImageIO;

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
				printOptions();
				line = sc.nextLine();
				pedido(line);
			} while(!line.equals("quit"));
			
			socket.close();
			sc.close();
			
		} catch(FileNotFoundException e) {
			System.out.println("Ficheiro não existe");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	private static void pedido(String line) throws IOException, ClassNotFoundException {
		String[] t = line.split("\\s+");
		switch(t[0]) {
		case "f":
		case "follow":
			if(t.length == 2) {
				outStream.writeObject(line);
				System.out.println((String) inStream.readObject());
			}
			break;
		case "u":
		case "unfollow":
			if(t.length == 2) {
				outStream.writeObject(line);
				System.out.println((String) inStream.readObject());
			}
			break;
		case "v":
		case "viewfollowers":
			if(t.length == 2) {
				outStream.writeObject(line);
				System.out.println((String) inStream.readObject());
			}
			break;
		case "p":
		case "post":
			post(line);
			System.out.println((String) inStream.readObject());
			break;
		case "w":
		case "wall":
			outStream.writeObject(line);
			wall();
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
			outStream.writeObject(line);
			System.out.println((String) inStream.readObject());
			break;
		}
	}

	private static void post(String line) throws IOException, ClassNotFoundException {
		outStream.writeObject(line);
		String[] t = line.split("\\s+");
	
		
		BufferedImage image = ImageIO.read(new File(t[1]));
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		ImageIO.write(image, "jpg", byteArrayOutputStream);
		byte[] size = ByteBuffer.allocate(4).putInt(byteArrayOutputStream.size()).array();
		outStream.write(size);
		outStream.write(byteArrayOutputStream.toByteArray());
		outStream.flush();
		/*
		String[] t = line.split("\\s+");
		File foto = new File(t[1]);
		outStream.writeObject(foto.getName());
		int filesize = (int) foto.length();
		outStream.writeObject(filesize);

		InputStream is = new FileInputStream(foto);
		
		byte[] buffer = new byte[MEGABYTE];
		int length = 0;
		while ((length = is.read(buffer, 0, buffer.length)) >= 0) {
			//length = is.read(buffer, 0, buffer.length);
			System.out.println("Length: " + length);
			outStream.write(buffer, 0, length);
		}
		//outStream.write(0);
		is.close();
		//
		 *
		 */
	}

	private static void printOptions() {
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
