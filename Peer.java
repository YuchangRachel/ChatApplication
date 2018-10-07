import java.io.*;
import java.net.*;

public class Peer{
	private static int threadNum = 1; 
	private int port;
	Socket socket = null;
	public int id;


	public Peer(Socket socket) {
		this.socket = socket;
		this.port = socket.getPort();
		this.id = threadNum++;
	}

	public Peer(Socket socket, int port) {
		this.socket = socket;
		this.port = port;
		this.id = threadNum++;
	}
	
	public int getId(){
		return id;
	}

	public void sendMessage(String message){
		try{
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			out.println(message);
		}catch(Exception e){
			System.out.println(e);
		}
	}

	public void getMessage(){
		String response = "";
		try{
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			while ((response = in.readLine()) != null){
				String[] strPart = response.split(" ");
				if (strPart[1].equals("connection") && strPart[strPart.length-1].equals("established") && strPart[strPart.length-2].equals("successfully")){
					System.out.println("The connection to peer " + socket.getInetAddress().getHostAddress() + " is successfully established");
				}
				else{
					System.out.println("Message received from " + socket.getInetAddress());
					System.out.println("Sender's Port : "+ port);
					System.out.println("Message:  " + response);
				}
			}
		}catch(Exception e){return;}
	}

	public String getList(){
		String ip = socket.getInetAddress().getHostAddress();
		return id +  " :    " + ip + "     " + socket.getPort();
	}
}
