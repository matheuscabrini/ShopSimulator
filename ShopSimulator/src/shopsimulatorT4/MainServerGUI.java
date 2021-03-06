package shopsimulatorT4;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDate;

import shopsimulatorT4.server.ShopManager;
import shopsimulatorT4.server.User;
import shopsimulatorT4.shared.Product;
import shopsimulatorT4.shared.Requisition;
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
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class MainServerGUI extends Application{

	static int serverPort;
	ShopManager shopMan; // realiza operacoes no sistema da loja (servidor e arquivos)
   
	Stage primaryStage; // tela principal do programa
    MenuBar menuBar; // menu do canto esquerdo superior, com File, Actions...
    TableView<Product> tableP;// tabela de produtos
    TableView<User> tableU; // tabela de users
    TableView<Requisition> tableR; // tabela de requisitions
    
    static final String ICON_PATH = "resources/icon3.png";
	
	public static void main(String[] args) {
		if (args.length == 1) {
			serverPort = Integer.parseInt(args[0]);
			launch(args);
		}
		else 
			System.out.println("usage: <server port>");
	}
	
	// Aqui, nesta tela inicial, � requisitado do usu�rio a port do server
	@Override
	public void start(Stage primaryStage) {
		try {
			shopMan = ShopManager.getInstance();
			shopMan.listenForClients(serverPort);
		} catch (IOException e) {
			showExceptionDialog("Error ocurred while opening files.", e);
			return;
		}

		Scene scene = new Scene(new Group());
		this.primaryStage = primaryStage;
		primaryStage.setWidth(700);
		//primaryStage.setHeight(700);
		primaryStage.setTitle("Shop Server");
		try {
			primaryStage.getIcons().add(new Image(this.getClass().
	    		getClassLoader().getResourceAsStream(ICON_PATH)));
		} catch (Exception e) {
			showExceptionDialog("Exception while loading icon.", e);
		}
		
		initMenus(); // Inicializando menus File, Actions
		
		// Inicializando tabelas e seus containers (tabs\abas)
		
		tableP = new TableView<Product>(); // tabela de produtos
		initProductTable();
		Tab tabP = new Tab("Products");
		tabP.setContent(tableP);
		tabP.setClosable(false);
		
		tableU = new TableView<User>(); // tabela de users
		initUserTable();
		Tab tabU = new Tab("Users");
		tabU.setContent(tableU);
		tabU.setClosable(false);
		
		tableR = new TableView<Requisition>(); // tabela de requisitions
		initRequisitionTable();
		Tab tabR = new Tab("Requisitions");
        tabR.setContent(tableR);
        tabR.setClosable(false);
        
        TabPane tabPane = new TabPane(tabP, tabU, tabR);
        tabPane.setMinWidth(700);
        VBox vbox = new VBox(5);
        vbox.getChildren().addAll(menuBar, tabPane);
 
        ((Group) scene.getRoot()).getChildren().addAll(vbox);
 
        primaryStage.setScene(scene);
        primaryStage.show();
	}
	
	// Aqui inicializa-se o menu do canto esquerdo-superior da tela principal
	// que possibilita as as diferentes opera��es do usu�rio sobre o sistema 
	void initMenus() {
        Menu menuFile = new Menu("File");
        Menu menuActions = new Menu("Actions");
        
		Menu menuFileSave = new Menu("Save");
        menuFileSave.setOnAction(ev -> {
        	saveChanges();
        	menuFile.hide();
        });
        
        Menu menuFileExit = new Menu("Exit");
        menuFileExit.setOnAction(ev -> {
        	exitProgram();
        });
        	
        Menu menuNewProduct = new Menu("New product...");
        menuNewProduct.setOnAction(ev -> {
        	createProduct();
        });
        
        Menu menuUpdateProduct = new Menu("Update product amount...");
        menuUpdateProduct.setOnAction(ev -> {
        	updateProduct();
        });
        
        Menu menuRefresh = new Menu("Refresh tables");
        menuRefresh.setOnAction(ev -> {
        	initProductTable();
        	initUserTable();
        	initRequisitionTable();
        	menuActions.hide();
        });
        
        menuFile.getItems().addAll(menuFileSave, new SeparatorMenuItem(), menuFileExit);
        menuActions.getItems().addAll(menuNewProduct, menuUpdateProduct, 
        		new SeparatorMenuItem(), menuRefresh);

        menuBar = new MenuBar(menuFile, menuActions);
	}
	
	// Inicializa��o (e atualiza��o, quando necess�rio) das tabelas
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	void initProductTable() {
		tableP.setPlaceholder(new Label("No products to show"));
		
		// Os procedimentos abaixo tornar�o poss�vel que a tabela
		// obtenha os dados de cada produto na lista do sistema 
		// por meio de seus getters.
		
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
        
        // Associando a lista de produtos � tabela:
		ObservableList<Product> pList = FXCollections.observableArrayList((shopMan.getProducts()));
        tableP.setItems(pList);
        tableP.setEditable(false);
        if (tableP.getColumns().isEmpty())
        	tableP.getColumns().addAll(codeCol, nameCol, expCol, priceCol, providerCol, amountCol);
        ((TableColumn) tableP.getColumns().get(0)).setVisible(false); // update na tabela
        ((TableColumn) tableP.getColumns().get(0)).setVisible(true);
	}

	// An�logo ao de products
	@SuppressWarnings({ "unchecked", "rawtypes" })
	void initUserTable() {
		tableU.setPlaceholder(new Label("No users to show"));
        
        TableColumn<User, String> IDCol = new TableColumn<User, String>("ID");
        IDCol.setCellValueFactory(new PropertyValueFactory<>("ID"));
        
        TableColumn<User, String> nameCol = new TableColumn<User, String>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        
        TableColumn<User, String> emailCol = new TableColumn<User, String>("Email");        
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        
        TableColumn<User, String> addressCol = new TableColumn<User, String>("Address");
        addressCol.setCellValueFactory(new PropertyValueFactory<>("address"));
        
        TableColumn<User, String> phoneCol = new TableColumn<User, String>("Phone");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        
        ObservableList<User> uList = FXCollections.observableArrayList((shopMan.getUsers()));
        tableU.setItems(uList);
        tableU.setEditable(false);
        if (tableU.getColumns().isEmpty())
        	tableU.getColumns().addAll(IDCol, nameCol, emailCol, addressCol, phoneCol);
        ((TableColumn) tableU.getColumns().get(0)).setVisible(false); // update
        ((TableColumn) tableU.getColumns().get(0)).setVisible(true);
	}
	
	// An�logo ao de products
	@SuppressWarnings({ "unchecked", "rawtypes" })
	void initRequisitionTable() {
		tableR.setPlaceholder(new Label("No requisitions for products to show"));

        TableColumn<Requisition, Integer> codeCol = 
        		new TableColumn<Requisition, Integer>("Code");
        codeCol.setCellValueFactory(new PropertyValueFactory<>("productCode"));
        
        TableColumn<Requisition, String> nameCol = 
        		new TableColumn<Requisition, String>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("userName"));
        
        TableColumn<Requisition, String> emailCol = 
        		new TableColumn<Requisition, String>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("userEmail"));    
        
        TableColumn productCol = new TableColumn<>("Requested product");
        productCol.getColumns().addAll(codeCol);
        
        TableColumn userCol = new TableColumn<>("Requester");
        userCol.getColumns().addAll(nameCol, emailCol);
        
        ObservableList<Requisition> rList = FXCollections.observableArrayList((shopMan.getRequisitions()));
        tableR.setItems(rList);
        tableR.setEditable(false);
        if (tableR.getColumns().isEmpty())
        	tableR.getColumns().addAll(productCol, userCol);
        ((TableColumn) tableR.getColumns().get(0)).setVisible(false); // update
        ((TableColumn) tableR.getColumns().get(0)).setVisible(true);
	}
	
	// Este m�todo cria uma tela nova, na qual podem ser digitados
	// os diferentes campos poss�veis para o registro de um produto
	// Pode-se omitir os dados de quaisquer campos
	void createProduct() {
		Stage productStage = new Stage();
		productStage.setTitle("New product");
				
		TextField nameField = new TextField();
		nameField.setPromptText("Name");
		TextField amntField = new TextField();
		amntField.setPromptText("Amount in stock");
		TextField provField = new TextField();
		provField.setPromptText("Provider");
		TextField priceField = new TextField();
		priceField.setPromptText("Price (format ex.: 4,99)");
		DatePicker dPicker = new DatePicker();
		dPicker.setPromptText("Expiration date");
		
        // Mensagem de erro caso user nao digite os campos corretamente
        Label errorMsg = new Label(); 
        errorMsg.setVisible(false);
		
        Button bOK = new Button("OK");
        bOK.setOnAction( (ev) -> {
        	
        	if (nameField.getText().isEmpty() || 
            	amntField.getText().isEmpty() ||
            	priceField.getText().isEmpty() || 
            	provField.getText().isEmpty() ||
            	dPicker.getValue() == null) {
        		errorMsg.setText("Please, fill in all the fields correctly.");
            	errorMsg.setVisible(true);
           		return;
           	}
        	
        	int amount;
        	try {
        		amount = Integer.parseInt(amntField.getText());	
        	} catch (NumberFormatException e) {
        		errorMsg.setText("Amount should be a valid number.");
        		errorMsg.setVisible(true);
        		return;
        	}
        	
        	LocalDate date = dPicker.getValue();
        	Product p = new Product(nameField.getText(), 
        			priceField.getText(), date.getDayOfMonth(), 
        			date.getMonthValue(), date.getYear(), 
        			provField.getText(), amount);
        	shopMan.addProduct(p);
        	
        	initProductTable();
        	productStage.hide();
        	showSuccessDialog("Product was succesfully added to the system.");
        });
        
		VBox vbox = new VBox(5, nameField, amntField, provField, 
				priceField, dPicker, errorMsg, bOK);
		vbox.setAlignment(Pos.CENTER);
		vbox.setPadding(new Insets(10, 10, 10, 10));
		
		productStage.setWidth(400);
		productStage.setScene(new Scene(vbox));
		productStage.initModality(Modality.APPLICATION_MODAL);
		productStage.show();
		vbox.requestFocus();
	}
	
	// Tela de update no estoque de um produto.
	void updateProduct() {
		Stage updateProdStage = new Stage();
		updateProdStage.setTitle("Update product amount");
		
		TextField codeField = new TextField();
		codeField.setPromptText("Product code");
		TextField amountField = new TextField();
		amountField.setPromptText("Amount to add");
        
        // Mensagem de erro caso user nao digite codigo ou amount
        Label errorMsg = new Label("Please, enter product code AND it's amount."); 
        errorMsg.setVisible(false);
        
        Button bOK = new Button("OK");
        bOK.setOnAction( (ev) -> {
        	if (codeField.getText().isEmpty() || 
        		amountField.getText().isEmpty()) {
        		errorMsg.setVisible(true);
        		return;
        	}
        	
        	int code, amount;
        	try {
        		code = Integer.parseInt(codeField.getText());
        		amount = Integer.parseInt(amountField.getText());
        	} catch (NumberFormatException e) {
        		errorMsg.setVisible(true);
        		return;
        	}
        	
        	// verificando se c�digo de produto foi valido e se o update deu certo
        	if (shopMan.addProductAmount(code, amount) == false) {
        		showErrorDialog("Invalid product code.");
        		return;
        	}
        	
    		initProductTable();
        	initRequisitionTable();
        	updateProdStage.hide();
        	showSuccessDialog(amount + " units of "
        			+ shopMan.getProductByCode(code).getName() + " were added.");
        });
        
		VBox vbox = new VBox(5, codeField, amountField, errorMsg, bOK);
		vbox.setAlignment(Pos.CENTER);
		vbox.setPadding(new Insets(10, 10, 10, 10));
		
		updateProdStage.setWidth(400);
		updateProdStage.setScene(new Scene(vbox));
		updateProdStage.initModality(Modality.APPLICATION_MODAL); // pra nao poder sair desta tela
		updateProdStage.show();
		vbox.requestFocus();
	}
    
	// Salva nos arquivos as mudan�as feitas no sistema.
    void saveChanges() {
		try {
			shopMan.saveChangesToFiles();
		} catch (IOException e) {
			showExceptionDialog("Error ocurred while saving data to files.", e);
		}
    }
	
	// M�todo para encerrar o programa, que deve ser synchronized
	// para n�o atrapalhar quaisquer opera��es que estejam sendo feitas
	// sobre o sistema no momento
    synchronized void exitProgram() {
		try {
			shopMan.close();
		} catch (IOException e) {
			showExceptionDialog("Error ocurred while saving data to files.", e);
		}
		primaryStage.hide();
    }
    
	// Este m�todo � chamado pela Application quando o usu�rio
	// sai do programa pelo X no canto superior.
	@Override
	public void stop() {
		exitProgram();
	}
	
	// M�todos para gerar janelas de alerta para algum resultado
	// dentro do programa (dialogs):
	
	void showSuccessDialog(String msg) {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("Information Dialog");
		alert.setHeaderText("Success!");
		alert.setContentText(msg);
		alert.showAndWait();
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

	    // Colocando o texto expand�vel no dialog
	    alert.getDialogPane().setExpandableContent(expContent);
	
	    alert.showAndWait();
    }
}
