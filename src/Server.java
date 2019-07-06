import java.io.*;
import java.net.*;

public class Server {
	private static ServerSocket ss;
	
	public static void main(String[] args) {
		try {
			ss = new ServerSocket(2000);
			while(true) {
				Socket socket = ss.accept();
				new ActionHandler(socket).start();
			}
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		finally {
			try {
				ss.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
