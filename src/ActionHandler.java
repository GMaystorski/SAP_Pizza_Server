import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ActionHandler extends Thread{
	
	private Socket socket;
	private ObjectOutputStream objWrite;
	private Scanner scan;
	private DbHelper db;
	
	public ActionHandler(Socket socket) throws IOException {
		this.socket = socket;
		objWrite = new ObjectOutputStream(socket.getOutputStream());
		scan = new Scanner(socket.getInputStream());
		db = new DbHelper();
	}
	
	@Override
	public void run() {
		
		do {
		handleMain();
		}while(!db.checkUser());
		
		switch(db.getType()){
			case "false" : handleClient(); break;
			case "true" : handleAdmin(); break;
		}
		
	}
	
	public void closeResources() {
		try {
			socket.close();
			objWrite.close();
			scan.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void handleMain() {
		try {
			String username = scan.nextLine();
			String password = scan.nextLine();
			String type = scan.nextLine();
			switch(type) {
				case "login" : objWrite.writeObject(db.logUser(username, password));break;
				case "register" : objWrite.writeObject(db.regUser(username, password));
								  db.updateUser(username, "0"); 
								  break;
			}
		}
		catch(SQLException | IOException  e) {
			try {
				objWrite.writeObject(0);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			//this.closeResources();
			e.printStackTrace();
		} 
		
	}
	
	public void handleClient() {
		try {
			String choice = scan.nextLine();
			switch(choice) {
				case "1" : clFirstOption(); break;
				case "2" : handleViewProducts();break;
				case "3" : handleReorder();break;
				case "logout" : db.logOut();
						   this.run();
						   break;
				default : System.out.println("Invalid choice");
						  throw new InvalidChoiceException();
			}
		}
		catch(InvalidChoiceException | IOException | SQLException  e) {
			e.printStackTrace();
		}
		
	}
	
	public void handleViewProducts() throws IOException, SQLException  {
		objWrite.writeObject(db.getProducts());
		String choice = scan.nextLine();
		switch(choice) {
			case "back" : handleClient(); break;
			case "proceed" : handleClient(); break;
		}
	}
	
	public void handleAdmin() {
		try {
			String choice = scan.nextLine();
			switch(choice) {
				case "1" : adFirstOption(); break;
				case "2" : adSecondOption(); break;
				case "3" : adThirdOption(); break;
				case "4" : adFourthOption(); break;
				case "logout" : db.logOut();
						   this.run();
						   break;
				default : System.out.println("Invalid choice");
						  throw new InvalidChoiceException();
			}
		}
		catch(InvalidChoiceException | IOException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public Object handleAdd() {
		
		try {
			String name = scan.nextLine();
			String quantity = scan.nextLine();
			String price = scan.nextLine();
		  return db.addProduct(name, quantity, Double.parseDouble(price));
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	public void adFirstOption() throws IOException {
			String choice = scan.nextLine();
			switch(choice) {
				case "back" : handleAdmin(); break;
				case "add" : objWrite.writeObject(handleAdd());adFirstOption();break;
			}
	}
	
	public void adSecondOption() throws IOException, SQLException {
		objWrite.writeObject(db.getProducts());
			String choice = scan.nextLine();
			switch(choice) {
				case "update" : objWrite.writeObject(handleUpdate()); adSecondOption();break;
				case "delete" : objWrite.writeObject(handleDelete()); adSecondOption();break;
				case "back" : handleAdmin(); break;
			}
	}
	
	public void adThirdOption() throws SQLException, IOException {
		List<List<Object>> orders = new ArrayList<>();
		String type = scan.nextLine();
		if(type.equals("setDates")) {
			String dateFrom = scan.nextLine();
			String dateTo = scan.nextLine();
			orders = db.getOrderDateToDate(dateFrom, dateTo);
		}
		else if(type.equals("empty")) {
			orders = db.getOrderDateToDate(null, null);
		}
		if(orders == null) {
			handleAdmin();
		}
		else {
			objWrite.writeObject(orders.size());
			for(int i = 0 ; i < orders.size() ; i++) {
				objWrite.writeObject(db.getCart((int) orders.get(i).get(0)));
			}
			objWrite.writeObject(orders);
			String choice = scan.nextLine();
			switch(choice) {
				case "back" : handleAdmin(); break;
			}
		}
		
		
	}
	
	public void adFourthOption() throws IOException, SQLException {
		objWrite.writeObject(db.getUsers());
		int flag = 0;
		while(flag == 0) {
			String choice = scan.nextLine();
			switch(choice) {
				case "1" : objWrite.writeObject(handleUserChange(1)); break;
				case "0" : objWrite.writeObject(handleUserChange(0)); break;
				case "back" : flag = 1; handleAdmin(); break;
			}
		}
	}
	
	public void handleReorder() {
		try {
			List<Object> orderInfo = db.getOrdersByUser();
			if(!orderInfo.isEmpty()) {
				objWrite.writeObject(orderInfo.size()/2);
				for(int i = 0 ; i < orderInfo.size() ; i+=2) {
					objWrite.writeObject(db.getCart((int) orderInfo.get(i)));
					objWrite.writeObject(orderInfo.get(i+1));
				}
				String choice = scan.nextLine();
				switch(choice) {
					case "create" : objWrite.writeObject(handleOrder()); handleReorder();break;
					case "back" : handleClient(); break;
				}
			}
			else {
				objWrite.writeObject(0);
				handleClient();
			}
			
		} catch (SQLException | IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public void clFirstOption() throws IOException {
		String choice = scan.nextLine();
		switch(choice) {
			case "create" : objWrite.writeObject(handleOrder()); clFirstOption();break;
			case "back" : handleClient(); break;
		}
	}
	
	public Object handleUserChange(int type) {
		try {
			String username = scan.nextLine();
			return db.changeStatus(username,type);
		}
		catch(SQLException  e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	public Object handleUpdate() {
		try {
			String name = scan.nextLine();
			String quantity = scan.nextLine();
			double price = Double.parseDouble(scan.nextLine());
			return db.updateProduct(name, quantity, price);
		}
		catch(SQLException | NumberFormatException e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	public Object handleDelete() {
		try {
			String name = scan.nextLine();
			return db.deleteProduct(name);
		}
		catch(SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	
	public Object handleOrder() {
		try {
			String location = scan.nextLine();
			List<String> cart = getCart();
			handleCart(cart);
			db.createOrder(location);
			return db.fillOrder(cart);
		}
		catch(SQLException e) {
			e.printStackTrace();
		}
		
		return 0;
	}
	
	public List<String> getCart(){
		int size = Integer.parseInt(scan.nextLine());
		List<String> cart = new ArrayList<>();
		for(int i = 0 ; i < size ; i++) {
			cart.add(i, scan.nextLine());
		}
		return cart;
		
	}
	
	public void handleCart(List<String> cart) {
		try {
			for(int i = 0 ; i < cart.size() ; i+=2) {
				cart.set(i, String.valueOf(db.getProductId(cart.get(i))));
				cart.remove(i+2);
			}
		}
		catch(SQLException e) {
			e.printStackTrace();
		}
	}
	

}
