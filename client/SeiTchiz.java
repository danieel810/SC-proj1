package client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class SeiTchiz {

	public static void main(String[] args) {
		Socket socket = null;
		String s = args[0];
		String[] ss = s.split(":");
		Scanner sc = new Scanner(System.in);
		try {
			socket = new Socket(ss[0], Integer.parseInt(ss[1]));
			String pw = null;
			if (args.length == 2)  {
				System.out.println("Insira a sua password: ");
				pw = sc.nextLine();
			} else {
				pw = args[2];
			}
			String id = args[1];
			ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
			outStream.writeObject(id);
			outStream.writeObject(pw);
			ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());
			int b = (int) inStream.readObject();
			switch (b) {
			case 1:
				
				break;
			case 2:
				
				break;

			case 3:
				String sss = (String)inStream.readObject();
				System.out.println(sss);
				String nome = sc.nextLine();
				outStream.writeObject(nome);
				break;
			} 
			
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
}
