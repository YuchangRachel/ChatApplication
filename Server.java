import java.io.*; 
import java.util.*; 
import java.net.*; 

public class Server extends Thread {
	ServerSocket listener = null;

	public Server(int listenPort){
		try {
			listener = new ServerSocket(listenPort);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run(){
		Socket conn = null;
		try {
			while(true){
				//server receive incoming client request
				conn = listener.accept();
				int port = conn.getPort();
				Peer peer = new Peer(conn, port);


				//server side, also need to update list
				Chat.addChatList(peer);

				//server's thread client
				Client client = new Client(new Peer(conn));
				client.getMessage();
				client.start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
