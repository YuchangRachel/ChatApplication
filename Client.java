import java.io.*; 
import java.net.*; 

public class Client extends Thread { 
	private Peer peer;

	public Client(Peer peer){
		this.peer = peer;
	}

	public void run(){
		peer.getMessage();
	}
} 

