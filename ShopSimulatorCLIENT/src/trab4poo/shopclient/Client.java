package trab4poo.shopclient;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {

	public static void main(String[] args) {
		System.out.println("digite user: ");
		Scanner scan = new Scanner(System.in);
		String user = scan.next();
		String line;
		
		try {
			Socket socket = new Socket("localhost", 3700);
			PrintWriter pw = new PrintWriter(
				new OutputStreamWriter(socket.getOutputStream()), true);
			
			pw.println(user);
			
			System.out.println("digite mensagens: ");
			while ((line = scan.next()) != null && !line.isEmpty())
				pw.println(line);	
			
			scan.close();
			pw.close();
			socket.close();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
