import java.sql.*;
import java.util.*;

public class DbHelper {
	private Connection conn;
	private List<String> currUser = new ArrayList<>();
	
	public DbHelper() {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/pizzeria","root","");
		}
		catch(SQLException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public List<Object> logUser(String username , String password) throws SQLException{
		List<Object> params = new ArrayList<>();
		List<Object> results = new ArrayList<>();
		params.add(username);
		params.add(password);
		//System.out.println(username + "" + password);
		ResultSet rs = SQLExecutor.executeQuery(conn, "Select username , password , isAdmin from users where username = ? and password = ?", params);
		if(rs.next()) {
			results.add(rs.getObject(1));
			results.add(rs.getObject(2));
			results.add(rs.getObject(3));
			updateUser(rs.getString(1),String.valueOf(rs.getBoolean(3)));
		}
		return results;
	}
	
	public Object regUser(String username , String password) throws SQLException {
		List<Object> params = new ArrayList<>();
		params.add(username);
		params.add(password);
		return SQLExecutor.executeUpdate(conn, "Insert into users (username,password) values (?,?)", params);
	}
	
	public Object addProduct(String name ,String quantity , double price) throws SQLException {
		List<Object> params = new ArrayList<>();
		params.add(name);
		params.add(quantity);
		params.add(price);
		return SQLExecutor.executeUpdate(conn, "Insert into products values (null,?,?,?)", params);
	}
	
	public List<Object> getProducts() throws SQLException{
		List<Object> results = new ArrayList<>();
		ResultSet rs = SQLExecutor.executeQuery(conn, "Select name,quantity,price from products", new ArrayList<>());
		while(rs.next()) {
			results.add(rs.getObject(1));
			results.add(rs.getObject(2));
			results.add(rs.getObject(3));
		}
		return results;
	}
	
	public Object updateProduct(String name,String quantity,double price) throws SQLException {
		List<Object> params = new ArrayList<>();
		params.add(name);
		params.add(quantity);
		params.add(price);
		params.add(name);
		return SQLExecutor.executeUpdate(conn, "Update products set name = ? , quantity = ? , price = ? where name = ?", params);
	}
	
	public Object deleteProduct(String name) throws SQLException {
		List<Object> params = new ArrayList<>();
		params.add(name);
		return SQLExecutor.executeUpdate(conn, "Delete from products where name = ?", params);
	}
	
	
	public void closeConnection() {
		try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void updateUser(String username , String type) {
		if(currUser.isEmpty()) {
			currUser.add(username);
			currUser.add(type);
		}
		else {
			currUser.add(0, username);
			currUser.add(1, type);
		}
	}
	
	public String getType(){
		return currUser.get(1);
	}
	
	public String getUsername() {
		return currUser.get(0);
	}
	
	public boolean checkUser() {
		if(currUser.isEmpty()) {
			return false;
		}
		else return true;
	}
	
	public void logOut() {

		currUser.clear();
	}
	
	

}
