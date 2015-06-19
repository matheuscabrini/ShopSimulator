package shopsimulatorT4;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import shopsimulatorT4.server.*;
import shopsimulatorT4.shared.*;

//import javafx.application.Application;
//import javafx.stage.Stage;


public class MainServer{

	public static void main(String[] args) throws Exception {
		
		System.out.println("Iniciando servidor...");
		ShopManager shop = ShopManager.getInstance();
		shop.listenForClients();
		System.out.println("Servidor iniciado.");
		
		Scanner SC = new Scanner(System.in);
		
		while(SC.hasNextLine())
		{
			String[] cmdarr = parse(SC.nextLine());
			
			if (cmdarr[0].equals("add"))
			{
				Product p = new Product(cmdarr[0], cmdarr[1], Integer.parseInt(cmdarr[2]), Integer.parseInt(cmdarr[3]), Integer.parseInt(cmdarr[4]), cmdarr[5], Integer.parseInt(cmdarr[6]));
				if(shop.addProduct(p))
					System.out.println("Produto adicionado.");
				else
					System.out.println("Falha ao adicionar produto.");
			}
		}
		SC.close();
		
		//launch(args);
	}

	private static String[] parse(String s)						//função para realizar parse dos comandos - recebe uma string e retorna
	{														//um array com cada "pedaço" (token) dessa string separado, sendo que
		String[] arr = s.split(" ");						//termos separados por espaço consistem tokens diferentes, a não ser que
		List<String> list = new ArrayList<String>();		//estejam entre aspas duplas - termos entre aspas duplas são um único token
		
		//list é uma lista auxiliar, só será utilizada durante a execução do método
		
		for (int i = 0; i < arr.length; i++)
		{
			String curr = "";
			try 
			{
				if (arr[i].charAt(0) == '"')	//tratamento dos tokens entre aspas
				{
					boolean first = true;	
					do
					{
						if (first)
							first = false;
						else
							curr = curr + " ";
					
						curr = curr + arr[i];
						i++;
					
					} while (arr[i-1].charAt(arr[i-1].length()-1) != '"');
					curr = curr.substring(1, curr.length()-1);
					i--;
				}
				else							//tratamento dos tokens sem aspas
					curr = arr[i];
			}
			catch (IndexOutOfBoundsException E)
			{
				System.out.println("Failed parsing command. Is the syntax correct?");
				return null;
			}
			
			list.add(curr);
		}
		
		String[] ret = new String[list.size()];
		
		for (int i = 0; i < ret.length; i++)
			ret[i] = list.get(i);
			
		return ret;
	}
	
//	@Override
//	public void start(Stage primaryStage) {
//
//	}
	
}
