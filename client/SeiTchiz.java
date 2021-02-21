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
			boolean b = (Boolean) inStream.readObject();
			if (b) {
				System.out.println("ATMD4");
			} else {
				String sss = (String)inStream.readObject();
				System.out.println(sss);
				String nome = sc.nextLine();
				outStream.writeObject(nome);
			}
			socket.close();
			sc.close();
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
