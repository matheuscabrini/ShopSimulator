package shopsimulatorT4;

import java.util.ArrayList;

import java.util.List;

import java.util.Scanner;

import javafx.application.Application;
import javafx.stage.Stage;

import shopsimulatorT4.client.*;
import shopsimulatorT4.shared.*;

public class MainClient extends Application {

	public static void main(String[] args) throws Exception {
		
		if (args.length != 2)
			return;
		
		Client client = new Client(args[0], Integer.parseInt(args[1]));
		
		Scanner SC = new Scanner(System.in);
		
		while(SC.hasNextLine())
		{
			String[] cmdarr = parse(SC.nextLine());
			
			if (cmdarr == null) continue;
			
			if (cmdarr[0].equals("login"))
			{
				ReturnValues ret;
				if((ret = client.signIn(cmdarr[1], cmdarr[2])) == ReturnValues.SUCCESS)
					System.out.println("Logado como " + cmdarr[1] + ".");
				else
					System.out.println("Falha de login: " + ret);
			}
			else if (cmdarr[0].equals("signup"))
			{
				ReturnValues ret;
				ret = client.signUp(cmdarr[1], cmdarr[2], cmdarr[3], cmdarr[4], cmdarr[5], cmdarr[6]);
				
				if (ret == ReturnValues.SUCCESS)
					System.out.println("Cadastrado com sucesso.");
				else
					System.out.println("Falha ao cadastrar: " + ret);
			}
			else if (cmdarr[0].equals("products"))
			{
				ArrayList<Product> list = client.getProducts();
				for (Product p : list)
					System.out.println(p.getName());
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
	
	@Override
	public void start(Stage primaryStage) {

	}
}
