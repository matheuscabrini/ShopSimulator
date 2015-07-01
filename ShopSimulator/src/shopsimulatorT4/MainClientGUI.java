package shopsimulatorT4;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Optional;

import shopsimulatorT4.client.Client;
import shopsimulatorT4.client.ReturnValues;
import shopsimulatorT4.shared.Product;
import shopsimulatorT4.shared.Requisition;
import shopsimulatorT4.shared.ShoppingCart;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.PasswordField;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class MainClientGUI extends Application {

	static Client client; // realiza operacoes com o servidor da loja
	static String serverIp;
	static int serverPort;
	ObservableList<Product> productList; // mais recente lista de produtos obtida do servidor
	ShoppingCart shopCart; // referência ao carrinho de compras que será utilizado
						   // em várias janelas do programa
   
	Stage primaryStage; // tela principal do programa
    MenuBar menuBar; // menu do canto esquerdo superior, com System e Shopping Cart
    TableView<Product> prodTable; // tabela de produtos
    TableView<ShoppingCart.Purchase> cartTable; // tabela com dados do carrinho de compras

    String iconPath = "resources/icon3.png";
	    
	public static void main(String[] args) {
		if (args.length == 2) {
			serverIp = args[0];
			serverPort = Integer.parseInt(args[1]);
			
			launch(args);
		}
		else 
			System.out.println("usage: <server ip> <server port>");		
	}
	
	@Override
	public void start(Stage primaryStage) {	
		
		// Tentando estabelecer conexao com o servidor:
		try {
			client = new Client(serverIp, serverPort);
			showSuccessDialog("Succesfully connected to server.");
		} catch (IOException e) {
			showExceptionDialog("Error ocurred while connecting to server on "
					+ serverIp + " , port " + serverPort, e);
			return;
		}
		
		this.primaryStage = primaryStage;
		Scene scene = new Scene(new Group());
		primaryStage.setTitle("Shop Client");
		try {
			primaryStage.getIcons().add(new Image(this.getClass().
	    		getClassLoader().getResourceAsStream(iconPath)));
		} catch (Exception e) {
			showExceptionDialog("Error while loading icon.", e);
		}
		
		Label title = new Label("Shop Simulator !");
		title.setAlignment(Pos.CENTER);
		
		TextField idField = new TextField();
		idField.setPromptText("ID");
		PasswordField passField = new PasswordField();
		passField.setPromptText("Password");
		VBox fields = new VBox(5, idField, passField);
		fields.setAlignment(Pos.CENTER);
		
		Button signInButton = new Button("Sign in");
		Button signUpButton = new Button("Create new account...");
		HBox buttons = new HBox(5, signInButton, signUpButton);
		buttons.setAlignment(Pos.CENTER);
		
		VBox vbox = new VBox(10, title, fields, buttons);
		vbox.setAlignment(Pos.CENTER);
		vbox.setPadding(new Insets(10, 10, 10, 10));

		signInButton.setOnAction((ev) -> {
			signIn(idField.getText(), passField.getText());
		});
		
		signUpButton.setOnAction((ev) -> {
			signUpScreen();
		});
		
        ((Group) scene.getRoot()).getChildren().addAll(vbox);
        primaryStage.setScene(scene);
        primaryStage.show();
		vbox.requestFocus();
	}
	
	// Tentamos conectar o usuario ao sistema com os dados que forneceu.
	// Se o servidor retorna o codigo de sucesso, o usuario é levado a tela
	// principal da loja, senão, avisamos ele de qual erro ocorreu.
	void signIn(String id, String pass) {
		try {
			ReturnValues ret = client.signIn(id, pass);
			if (ret == ReturnValues.SUCCESS) {
				showSuccessDialog("You are now logged in the server.");
				initShopScreen();
			}
			else if (ret == ReturnValues.NO_SUCH_ID) 
				showErrorDialog("This ID does not exist! Are you sure it is correct?");
			else if (ret == ReturnValues.WRONG_PASSWORD) 
				showErrorDialog("Wrong password! Please, try again.");
			else
				showErrorDialog("Unknown error.");
		} catch (Exception e) {
			showExceptionDialog("Error while signing in on the server", e);
		}
	}
	
	// Tela de criação de nova conta no sistema. Deve-se preencher todos os dados
	void signUpScreen() {
		Stage signUpStage = new Stage();
		signUpStage.setTitle("New account");
		
		TextField idField = new TextField();
		idField.setPromptText("ID");
		TextField passField = new TextField();
		passField.setPromptText("Password");
		TextField nameField = new TextField();
		nameField.setPromptText("Name");
		TextField emailField = new TextField();
		emailField.setPromptText("Email");
		TextField addressField = new TextField();
		addressField.setPromptText("Address");
		TextField phoneField = new TextField();
		phoneField.setPromptText("Phone");
        
        Label errorMsg = new Label("Please, enter ALL the fields."); 
        errorMsg.setVisible(false);
        
        Button bOK = new Button("OK");
        bOK.setOnAction( (ev) -> {
        	if (idField.getText().isEmpty() || passField.getText().isEmpty() ||
        		nameField.getText().isEmpty() || emailField.getText().isEmpty() ||
        		addressField.getText().isEmpty() || phoneField.getText().isEmpty() ) {
        		errorMsg.setVisible(true);
        		return;
        	}
        	
        	// Fazendo o cadastro no servidor e obtendo sua resposta:
        	ReturnValues ret;
			try {
				ret = client.signUp(nameField.getText(), addressField.getText(), 
						phoneField.getText(), emailField.getText(), 
						idField.getText(), passField.getText());
			} catch (Exception e) {
				showExceptionDialog("Error while signing up new user on server", e);
				return;
			}
        	
        	if (ret == ReturnValues.SUCCESS)
        		showSuccessDialog("Account was succesfully created! :)");
        	else if (ret == ReturnValues.ALREADY_IN_USE_ID)
        		showErrorDialog("This ID is already in use. :(");
        	else 
        		showErrorDialog("Unknown error! Please, try restarting. :(");
        	
        	signUpStage.close();
        });
        
		VBox vbox = new VBox(5, idField, passField, nameField, emailField,
				addressField, phoneField, errorMsg, bOK);
		vbox.setAlignment(Pos.CENTER);
		vbox.setPadding(new Insets(10, 10, 10, 10));
		
		signUpStage.setWidth(300);
		signUpStage.setScene(new Scene(vbox));
		signUpStage.initModality(Modality.APPLICATION_MODAL); // pra nao poder sair desta tela
		signUpStage.show();
		vbox.requestFocus();
	}
	
	// Desenha a tela principal da loja na tela, com os produtos e menus para compra.
	void initShopScreen() {
		Scene scene = new Scene(new Group());

		//primaryStage.setWidth(700);
		//primaryStage.setHeight(700);
		
		initMenus(); // Inicializando menus System e Shopping Cart
		
		// Inicializando tabelas e seus containers (tabs\abas)
		
		prodTable = new TableView<Product>(); // tabela de produtos
		initProductTable();
		Tab prodTab = new Tab("Products");
		prodTab.setContent(prodTable);
		prodTab.setClosable(false);
		
		shopCart = new ShoppingCart();
		cartTable = new TableView<ShoppingCart.Purchase>(); // tabela de compras
		initCartTable();
		Tab cartTab = new Tab("Shopping Cart");
		cartTab.setContent(cartTable);
		cartTab.setClosable(false);
        
        TabPane tabPane = new TabPane(prodTab, cartTab);
        tabPane.setMinWidth(700);
        VBox vbox = new VBox(5);
        vbox.getChildren().addAll(menuBar, tabPane);
 
        ((Group) scene.getRoot()).getChildren().addAll(vbox);
 
        primaryStage.setScene(scene);
        primaryStage.show();
	}
	
	// Aqui inicializa-se o menu do canto esquerdo-superior da tela principal
	// que possibilita as as diferentes operações do usuário sobre o sistema 
	void initMenus() {
        Menu menuSystem = new Menu("System");
        Menu menuCart = new Menu("Actions");

        Menu menuSystemRefresh = new Menu("Refresh product list");
        menuSystemRefresh.setOnAction(ev -> {
        	initProductTable();
        	menuSystem.hide();
        });
        
        Menu menuSystemExit = new Menu("Exit");
        menuSystemExit.setOnAction(ev -> {
        	exitProgram();
        });

        Menu menuCartAdd = new Menu("Add product to cart...");
        menuCartAdd.setOnAction(ev -> {
        	addProductScreen();
        });
        
        Menu menuCartConfirm = new Menu("Confirm purchases/requisitions...");
        menuCartConfirm.setOnAction(ev -> {
        	cartConfirmScreen();
        });
        
        menuSystem.getItems().addAll(menuSystemRefresh, new SeparatorMenuItem(), menuSystemExit);
        menuCart.getItems().addAll(menuCartAdd, menuCartConfirm);

        menuBar = new MenuBar(menuSystem, menuCart);
	}
	
	// Inicialização (e atualização, quando necessário) da tabela de produtos
	@SuppressWarnings({ "unchecked", "rawtypes" })
	void initProductTable() {
		// Obtendo os produtos do servidor:
		ArrayList<Product> temp = null;
		try {
			temp = (ArrayList<Product>) client.getProducts();
			productList = FXCollections.observableArrayList(temp);
		} catch (Exception e) {
			showExceptionDialog("Error while fetching product list from server.", e);
			return;
		}
		
		prodTable.setPlaceholder(new Label("No products to show"));
		
        TableColumn<Product, Integer> codeCol = new TableColumn<Product, Integer>("Code");
        codeCol.setCellValueFactory(new PropertyValueFactory<>("code"));
        
        TableColumn<Product, String> nameCol = new TableColumn<Product, String>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        
        TableColumn<Product, String> expCol = new TableColumn<Product, String>("Expiration Date");
        expCol.setCellValueFactory(new PropertyValueFactory<>("expDate"));
        
        TableColumn<Product, String> priceCol = new TableColumn<Product, String>("Price ($)");        
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        
        TableColumn<Product, String> providerCol = new TableColumn<Product, String>("Provider");
        providerCol.setCellValueFactory(new PropertyValueFactory<>("provider"));
        
        TableColumn<Product, Integer> amountCol = new TableColumn<Product, Integer>("Amount");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        
        // Associando a lista de produtos à tabela:
        prodTable.setItems(productList);
        prodTable.setEditable(false);
        if (prodTable.getColumns().isEmpty())
        	prodTable.getColumns().addAll(codeCol, nameCol, expCol, priceCol, providerCol, amountCol);
        ((TableColumn) prodTable.getColumns().get(0)).setVisible(false); // update na tabela
        ((TableColumn) prodTable.getColumns().get(0)).setVisible(true);
	}

	// Inicialização (e atualização, quando necessário) da tabela 
	// de compras presentes no carrinho do cliente.
	@SuppressWarnings({ "unchecked", "rawtypes" })
	void initCartTable() {
		cartTable.setPlaceholder(new Label("No products in your shopping cart.\nBuy something! :D"));
		
        TableColumn<ShoppingCart.Purchase, Integer> codeCol = 
        		new TableColumn<ShoppingCart.Purchase, Integer>("Code");
        codeCol.setCellValueFactory(new PropertyValueFactory<>("prodCode"));
        
        TableColumn<ShoppingCart.Purchase, String> priceCol = 
        		new TableColumn<ShoppingCart.Purchase, String>("Price ($)");        
        priceCol.setCellValueFactory(new PropertyValueFactory<>("prodPrice"));
        
        TableColumn<ShoppingCart.Purchase, Integer> amountCol = 
        		new TableColumn<ShoppingCart.Purchase, Integer>("Amount");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amountPurchased"));
        
        // Associando a lista de compras à tabela:
		ObservableList<ShoppingCart.Purchase> purchasesList = FXCollections.observableArrayList();
		Iterator<ShoppingCart.Purchase> it = shopCart.getPurchases();
		while (it.hasNext()) 
			purchasesList.add(it.next());
        
		cartTable.setItems(purchasesList);
        
        cartTable.setEditable(false);
        if (cartTable.getColumns().isEmpty())
        	cartTable.getColumns().addAll(codeCol, priceCol, amountCol);
        ((TableColumn) cartTable.getColumns().get(0)).setVisible(false); // update na tabela
        ((TableColumn) cartTable.getColumns().get(0)).setVisible(true);
	}
	
	// Tela onde se adiciona um produto no carrinho de compras
	void addProductScreen() {
		Stage addProductStage = new Stage();
		addProductStage.setTitle("Add product to cart");
		
		TextField codeField = new TextField();
		codeField.setPromptText("Product code");
		TextField amountField = new TextField();
		amountField.setPromptText("Amount to buy");
        
        // Mensagem de erro caso user nao digite codigo ou amount
        Label errorMsg = new Label("Please, enter product code AND it's amount."); 
        errorMsg.setVisible(false);
        
        Button bOK = new Button("OK");
        bOK.setOnAction( (ev) -> {
        	
        	if (codeField.getText().isEmpty() || amountField.getText().isEmpty()) {
        		errorMsg.setVisible(true);
        		return;
        	}
        	
        	int code, amountBought;
        	try {
        		code = Integer.parseInt(codeField.getText());
        		amountBought = Integer.parseInt(amountField.getText());
        	} catch (NumberFormatException e) {
        		errorMsg.setVisible(true);
        		return;
        	}
        	
        	// verificando se código de produto foi valido e se tem a quantidade
        	// desejada de produtos de acordo com a lista que o client possui no momento
        	Product productBought = null;
        	for (Product product : productList) {
				if (code == product.getCode()) {
					productBought = product;
					
					// Se produto nao estiver disponivel, oferecemos a notificacao
					// por email ao usuario ao reestoque do produto
					if (productBought.getAmount() < amountBought) {
						showRequisitionDialog(code, productBought.getName());
						return;
					}
					
					break;
				}
			}
        	
        	if (productBought == null) { // Codigo digitado nao foi encontrado no sistema
        		showErrorDialog("Invalid product! \nAre you sure the product code is correct?");
        		return;
        	}
        		
        	shopCart.addPurchase(code, amountBought, productBought.getPrice());
    		initCartTable();
        	addProductStage.hide();
        });
        
		VBox vbox = new VBox(5, codeField, amountField, errorMsg, bOK);
		vbox.setAlignment(Pos.CENTER);
		vbox.setPadding(new Insets(10, 10, 10, 10));
		
		addProductStage.setWidth(400);
		addProductStage.setScene(new Scene(vbox));
		addProductStage.initModality(Modality.APPLICATION_MODAL); // pra nao poder sair desta tela
		addProductStage.show();
		vbox.requestFocus();
	}
	
	// Tela de confirmação para envio das compras ao servidor
	void cartConfirmScreen() {
		if (shopCart.isEmpty()) {
			showErrorDialog("Your shopping cart is empty.");
			return;
		}
			
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Confirmation Dialog");
		alert.setHeaderText("Confirm purchase(s)");
		alert.setContentText("The total price of your purchases is $" + shopCart.getTotalPrice()
				+ "\nClick OK to complete your transaction!");
		
		Optional<ButtonType> result = alert.showAndWait();
		if (result.get() == ButtonType.CANCEL) {
			alert.close();
			return;
		}
		
		// Enviando o carrinho ao servidor, obtendo a possível lista
		// de códigos de produtos que la não estavam disponiveis:
		try {
			sendShoppingCart();
		} catch (Exception e) {
			showExceptionDialog("Error while sending purchases to server.", e);
		}
	}
	
	// Realiza a operação de enviar as compras ao servidor, colocando na
	// tela a lista de produtos que estavam indisponiveis 
	void sendShoppingCart() throws IOException, ClassNotFoundException {
	    ArrayList<Integer> failList;
		failList = (ArrayList<Integer>) client.sendShoppingCart(shopCart);
		shopCart = new ShoppingCart(); // Discartando carrinho recém-enviado
		initCartTable();

		// Se a transação deu certo:
		if (failList == null) {
			showSuccessDialog("Your purchases and requisitions were sucessfully sent! :)");
			return;
		}
		
		// conterá os nomes e codigos dos produtos indisponiveis
	    StringBuilder builder = new StringBuilder();

		for (Integer code: failList) {
			for (Product prod : productList) {
				if (code == prod.getCode()) {
					builder.append(prod.getName() + " (Code " + code + ")\n");
					break;
				}
			}
		}
		
		// Abrindo janela com a lista de produtos indisponiveis e
		// escolha do usuário de ser notificado ao reestoque no sistema
	    Alert alert = new Alert(AlertType.CONFIRMATION);
	    alert.setTitle("Unavailable products");
	    alert.setContentText("The following products were unavailable"
	    		+ "in the server at the moment of purchase. Click OK and "
	    		+ "you will be notified via email when they are restocked.");
		
	    TextArea textArea = new TextArea(builder.toString());
	    textArea.setEditable(false);
	    textArea.setWrapText(true);
	    textArea.setMaxWidth(Double.MAX_VALUE);
	    textArea.setMaxHeight(Double.MAX_VALUE);
	    GridPane.setVgrow(textArea, Priority.ALWAYS);
	    GridPane.setHgrow(textArea, Priority.ALWAYS);
	    
	    GridPane expContent = new GridPane();
	    expContent.setMaxWidth(Double.MAX_VALUE);
	    expContent.add(textArea, 0, 0);

	    // Colocando o texto expandível no dialog
	    alert.getDialogPane().setExpandableContent(expContent);
	
		Optional<ButtonType> result = alert.showAndWait();
		if (result.get() == ButtonType.CANCEL) {
			alert.close();
			return;
		}
		
		// Como o usuário quer ser notificado, colocamos as requisitions
		// por cada produto em um carrinho e o enviamos ao servidor
		ShoppingCart reqCart = new ShoppingCart();
		for (Integer code : failList) 
			reqCart.addRequisition(new Requisition(code));
		
		try {
			client.sendShoppingCart(reqCart);
		} catch (Exception e) {
			showExceptionDialog("Error while sending requisitions to server", e);
		}
		
		alert.close();
	}
	
	// Encerramento do programa: fechamos a conexao com servidor
    void exitProgram() {
		try {
			client.closeConnection();
		} catch (IOException e) {
			showExceptionDialog("Error ocurred while disconnecting from server.", e);
		}
		primaryStage.hide();
    }
    
	// Este método é chamado pela Application quando o usuário
	// sai do programa pelo X no canto superior.
	@Override
	public void stop() {
		exitProgram();
	}
	
	// Métodos para gerar janelas de alerta para algum resultado
	// dentro do programa (dialogs):
	
	void showSuccessDialog(String msg) {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("Information Dialog");
		alert.setHeaderText("Success!");
		alert.setContentText(msg);
		alert.showAndWait();
	}
	
	void showRequisitionDialog(int productCode, String productName) {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Confirmation Dialog");
		alert.setHeaderText("This amount of "+productName+" is unavailable in our store"
				+ " at the moment...");
		alert.setContentText("Would you like to be notified via email when the product"
				+ " is restocked? This request will be activated when you confirm it in the Actions menu.");
		
		Optional<ButtonType> response = alert.showAndWait();
		if (response.get() == ButtonType.OK) {
			// Colocando a requisition no carrinho:
			shopCart.addRequisition(new Requisition(productCode));
		}
		
		alert.close();		
	}
	
	void showErrorDialog(String msg) {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("Error Dialog");
		alert.setHeaderText("Oops! Something went wrong...");
		alert.setContentText(msg);
		alert.showAndWait();
	}
	
    void showExceptionDialog(String msg, Exception ex) {
	    Alert alert = new Alert(AlertType.ERROR);
	    alert.setTitle("Exception Dialog");
	    alert.setContentText(msg);
		
	    StringWriter sw = new StringWriter();
	    PrintWriter pw = new PrintWriter(sw);
	    ex.printStackTrace(pw);
	    String exceptionText = sw.toString();
	
	    Label label = new Label("The exception stacktrace was:");
	
	    TextArea textArea = new TextArea(exceptionText);
	    textArea.setEditable(false);
	    textArea.setWrapText(true);
	
	    textArea.setMaxWidth(Double.MAX_VALUE);
	    textArea.setMaxHeight(Double.MAX_VALUE);
	    GridPane.setVgrow(textArea, Priority.ALWAYS);
	    GridPane.setHgrow(textArea, Priority.ALWAYS);
	
	    GridPane expContent = new GridPane();
	    expContent.setMaxWidth(Double.MAX_VALUE);
	    expContent.add(label, 0, 0);
	    expContent.add(textArea, 0, 1);

	    // Colocando o texto expandível no dialog
	    alert.getDialogPane().setExpandableContent(expContent);
	
	    alert.showAndWait();
    }
}