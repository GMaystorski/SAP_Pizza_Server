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
	
	public List<Object> getUsers() throws SQLException{
		List<Object> results = new ArrayList<>();
		ResultSet rs = SQLExecutor.executeQuery(conn, "select username from users", new ArrayList<>());
		while(rs.next()) {
			results.add(rs.getObject(1));
		}
		return results;
	}
	
	public Object changeStatus(String username,int type) throws SQLException {
		List<Object> params = new ArrayList<>();
		params.add(type);
		params.add(username);
		return SQLExecutor.executeUpdate(conn, "update users set isAdmin = ? where username = ?", params);
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
	
	public Object createOrder(String location) throws SQLException {

		List<Object> params = new ArrayList<>();
		params.add(getUserId());
		params.add(location);
		return SQLExecutor.executeUpdate(conn, "insert into orders values(null,?,now(),?)", params);
	}
	
	
	public Object fillOrder(List<String> cart) throws SQLException {
		int orderId = getLastOrderId();
		for(int i = 0 ; i < cart.size() ; i+=2) {
			for(int j = 0 ; j < Integer.parseInt(cart.get(i+1)) ; j++){
				int flag = (int)putProduct(Integer.parseInt(cart.get(i)),orderId);
				if (flag == 0) {
					return 0;
				}
			}
		}
		return 1;
	}
	
	public Object putProduct(int productId , int orderId) throws SQLException {
		List<Object> params = new ArrayList<>();
		params.add(productId);
		params.add(orderId);
		return SQLExecutor.executeUpdate(conn, "insert into product_order values(?,?)", params);
	}
	
	public List<Object> getOrdersByUser() throws SQLException{
		List<Object> params = new ArrayList<>();
		List<Object> results = new ArrayList<>();
		params.add(getUserId());
		ResultSet rs = SQLExecutor.executeQuery(conn, "select id,location from orders where user_id = ?", params);
		while(rs.next()) {
			results.add(rs.getObject(1));
			results.add(rs.getObject(2));
		}
		
		return results;
		
	}
	
	public List<String> getCart(int orderId) throws SQLException{
		List<Object> params = new ArrayList<>();
		List<String> results = new ArrayList<>();
		params.add(orderId);
		ResultSet rs = SQLExecutor.executeQuery(conn, "select name,count(product_id),price from products "
										+ "join product_order on products.id = product_id where order_id = 4 group by product_id ", params);
		while(rs.next()) {
			results.add(rs.getString(1));
			results.add(String.valueOf(rs.getInt(2)));
			results.add(String.valueOf(rs.getDouble(3)));
		}
		
		return results;
		
		
	}
	
	public int getLastOrderId() throws SQLException {
		ResultSet rs = SQLExecutor.executeQuery(conn, "select id from orders order by id desc limit 1", new ArrayList<>());
		rs.next();
		return rs.getInt(1);
	}
	
	
 	public int getUserId() throws SQLException {
		List<Object> params = new ArrayList<>();
		params.add(currUser.get(0));
		ResultSet rs = SQLExecutor.executeQuery(conn, "select id from users where username = ?", params);
		rs.next();
		return rs.getInt(1);
	}
	
	public int getProductId(String product) throws SQLException {
		List<Object> params = new ArrayList<>();
		params.add(product);
		ResultSet rs = SQLExecutor.executeQuery(conn, "select id from products where name = ?", params);
		rs.next();
		return rs.getInt(1);
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
