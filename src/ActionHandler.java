import java.io.*;
import java.net.*;
import java.sql.*;
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
		catch(SQLException | IOException e) {
			this.closeResources();
			e.printStackTrace();
		} 
		
	}
	
	public void handleClient() {
		try {
			String choice = scan.nextLine();
			switch(choice) {
				case "logout" : db.logOut();
						   this.run();
						   break;
				default : System.out.println("Invalid choice");
						  throw new InvalidChoiceException();
			}
		}
		catch(InvalidChoiceException e) {
			e.printStackTrace();
		}
		
	}
	
	public void handleAdmin() {
		try {
			String choice = scan.nextLine();
			switch(choice) {
				case "1" : adFirstOption(); break;
				case "2" : adSecondOption(); break;
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
		String name = scan.nextLine();
		String quantity = scan.nextLine();
		String price = scan.nextLine();
		try {
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
	
	public Object handleUpdate() {
		String name = scan.nextLine();
		String quantity = scan.nextLine();
		double price = Double.parseDouble(scan.nextLine());
		try {
			return db.updateProduct(name, quantity, price);
		}
		catch(SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	public Object handleDelete() {
		String name = scan.nextLine();
		try {
			return db.deleteProduct(name);
		}
		catch(SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}
	

}
