import java.io.*;
import java.net.*;
import java.util.*;

public class Chat {
	public static void main(String[] args) throws Exception{
		//no arguments passed
		if (args.length == 0){
			System.out.println("Enter your port number!!!");
			System.exit(1);
		}

		String myip = getMyIp();
		int myport = Integer.parseInt(args[0]);
		Server server = null;

		//now server is listening
		try{
			server = new Server(myport);
			server.start();
		}catch(Exception e){
			e.printStackTrace();
		}

		System.out.println("Peer-2-Peer Chat Now!!!!!");

		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(System.in));

			while (true){
				String input = br.readLine();
				String[] lines = input.split(" ", 3);   

				if (input.equals("myport")){
					System.out.println("The program runs on port number: " + myport);
				}
				else if (input.equals("myip")){
					System.out.println("The IP Address: " + myip);
				}
				else if (input.equals("help")){
					help();
				}
				else if (lines[0].equals("connect")){
					System.out.print(server.connect(input));
				}
				else if (lines[0].equals("list")){
					server.list();
				}
				else if (lines[0].equals("send")){
					System.out.print(server.send(Integer.parseInt(lines[1]), lines[2]));  //lines[2] is whole message, including blank space
				}
				else if (lines[0].equals("terminate")){
					System.out.print(server.terminate(input));
				}
				else if (lines[0].equals("exit")){

				}
				else {
					System.out.println("Insert correct command input or enter 'help' to see manual!!!");
				}


			}
		}catch (IOException e){
			e.printStackTrace();
		}

	}

	//get ip
	public static String getMyIp() throws SocketException{
		String ip = null;
		try {
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			for (NetworkInterface iface : Collections.list(interfaces)) {

				// filters out 127.0.0.1 and inactive interfaces
				if (iface.isLoopback() || !iface.isUp()) continue;

				Enumeration<InetAddress> addresses = iface.getInetAddresses();
				for (InetAddress addr : Collections.list(addresses)){
					//filters out Ipv6
					if (addr instanceof Inet6Address) continue;

					ip = addr.getHostAddress();
				}
			}
		} catch (SocketException e) {
			throw new RuntimeException(e);
		}
		return ip;
	}

	//help command input: Display information about the available user interface options or command manual
	public static void help(){
		System.out.println("myip: Display the IP of this proces.\n");
		System.out.println("myport: Display the port on which this process is listening for incoming connections.\n");
		System.out.println("connect <destionationIP> <destionation port#>: Establish a new TCP connection to the specified <destination> at the specified < port no>. The <destination> is the IP address of the computer. Any attempt to connect to an invalid IP should be rejected and suitable error message should be displayed. Success or failure in connections between two peers should be indicated by both the peers using suitable messages. Self-connections and duplicate connections should be flagged with suitable error messages.\n ");
		System.out.println("list: Display a numbered list of all the connections this process is part of. This list includes id, IP address, Port No. columns\n");
		System.out.println("send <connection id> <message>: Send the message to the host on the connection that is designated by number(id) when command list is used. Message to be sent can be up-to-100 characters long, including blank spaces.\n");
		System.out.println("terminate <connection id>: Terminate the connection with specified id, and then update list if 'list' as command input. If send the peer of id that already terminated, error message is displayed.\n");
		System.out.println("exit: Close all connections and terminate this process. This peer exit news will display on screens of other two peers. They should update their connection list by removing the peer that exit.\n");
	}



	public static class Server extends Thread{
		public List<PeerConnection> chatList;
		private boolean endThread = false;
		private int port;
		ServerSocket server = null;

		public Server(int port) throws IOException{
			this.port = port;
			chatList = Collections.synchronizedList(new ArrayList<PeerConnection>());
		}

		public void run(){
			try {
				server = new ServerSocket(port);
			}catch(Exception e){
				e.printStackTrace();
			}

			while(!endThread){
				Socket conn = null;
				try{
					conn = server.accept();
				} catch (Exception e) {
					e.printStackTrace();
				}

				if (conn != null) {
					PeerConnection newChat = new PeerConnection(conn, chatList);
					newChat.start();
					chatList.add(newChat);
				}
			}
		}

		//connect <destination ip><destination port>
		public String connect(String input){
			String[] line = input.split(" ");
			String desIp = line[1];
			int desPort = Integer.parseInt(line[2]);

			if (line.length < 3)
				return "Invalid connection input, try again!!!\n";

			//A valid port value is between 0 and 65535
			if ((desPort< 0) || (desPort > 65535))
				return "Invalid port number, try again!!!\n";

			//check ip valid or not
			if (!isValidIPv4(desIp))
				return "Invalid ip, check it again!!!\n";


			//testing
			/*
			   if (desIp.equals(getMyIp())){
			   return "Self connection failed!!!\n";   //self connection situation
			   }
			   */

			//Socket client = new Socket();
			Socket client = null;
			PeerConnection newChat = null;
			try{
				//client.connect(new InetSocketAddress(desIp, desPort));
				client = new Socket(desIp, desPort);
				newChat = new PeerConnection(client, chatList);
				newChat.start();

				//add peer into list
				chatList.add(newChat);

			}catch(Exception i){
				return "Connection failed!!!";
			}


			//chatList.get(chatList.size() - 1).connectNotify();
			return "The connection to peer " + desIp + " is successfully established\n";


		}


		//list 
		public void list(){
			System.out.println("id:   IP address:   Port No.:");
			for (int i = 0; i < chatList.size(); i++){
				System.out.println((i+1) + " : " + chatList.get(i).getHost() + "   " + chatList.get(i).getPort());
			}
		}

		//send <connection id><message>
		public String send(int connId, String msg){
			if (connId < 0 || connId > chatList.size())
				return "Connection id is out of bound, please check it in 'list'command!!!\n";
			if (msg.length() > 100)
				return "Message is out of 100 characters long, including blank spaces!!!\n";
			chatList.get(connId-1).sendMessage(msg);
			return "Message sent to " + connId +  "\n";

		}

		//terminate
		// go through the list look thorough the list once found id, terminate connection & remove from list(update list)
		// if id not found, handle it accordingly
		public String terminate(String input){
			String[] strParts = input.split(" ");
			int chatNum;

			if (strParts[1].matches("\\d+")) chatNum = Integer.parseInt(strParts[1]) - 1;
			else return "Invalid chat id.\n";
			if (chatNum < 0 || chatNum >= chatList.size()) return "Chat id is out of bounds.\n";
			chatList.get(chatNum).disconnect();
			chatList.remove(chatNum);

			/*else{
			// make a separate list of deleted id's maybe in order to keep track of what has already been deleted
			System.out.println("The Id your searching has either already been deleted or Id not found. Please check the list\n");
			help();
			}*/
			return "Chat connection terminated\n";
		}



		//validate ip
		public static boolean isValidIPv4(String ip) {
			if (ip.length() < 7) return false;
			if (ip.charAt(0) == '.') return false;
			if (ip.charAt(ip.length()- 1) == '.') return false;
			String[] tokens = ip.split("\\.");
			if(tokens.length != 4) return false;
			for(String token:tokens) {
				if(!isValidIPv4Token(token)) return false;
			}
			return true;
		}
		public static boolean isValidIPv4Token(String token) {
			if(token.startsWith("0") && token.length() > 1) return false;
			try {
				int parsedInt = Integer.parseInt(token);
				if(parsedInt < 0 || parsedInt > 255) return false;
				if(parsedInt == 0 && token.charAt(0) != '0') return false;
			} catch(NumberFormatException e) {
				return false;
			}
			return true;
		}


	}




	public static class PeerConnection extends Thread{
		private boolean endThread = false;
		private List<PeerConnection> peers;
		Socket client = null;
		DataOutputStream out = null;
		DataInputStream in = null;


		public PeerConnection(Socket client, List<PeerConnection> peers) {
			this.client = client;
			this.peers = peers;

		}

		public void run(){
			try{
				out = new DataOutputStream(client.getOutputStream());
				in = new DataInputStream(client.getInputStream());
			} catch (Exception e) {return;}


			while(!endThread){
				try{
					int messageType = in.readInt();

					switch(messageType){

						case -1: 
							endThread = true;
							System.out.println("\nClient " + getHost() + ":" + getPort() +" disconnected.\n");
							peers.remove(this);
							break;

						case 1:
							System.out.println("The connection to peer " + getHost() + " is successfully established");
							break;

						case 2:
							String response = in.readUTF();

							System.out.println("\nMessage received from " + getHost());
							System.out.println("Sender's Port: " + getPort());
							System.out.println("Message: " + response);

					}
				} catch (Exception e) {}
			}
			try {
				out.close();
				in.close();
				client.close();

			} catch (Exception e) {}

		}

		public void connectNotify(){
			try{
				out.writeInt(1);
				out.flush();
			}catch(Exception e){
				System.out.println(e);
			}
		}

		public void sendMessage(String message) {
			try {
				out.writeInt(2);
				out.writeUTF(message);
				out.flush();
			} catch(Exception e){
				// System.out.println(e);
			}
		}


		public void disconnect(){
			try {
				out.writeInt(-1);
				out.flush();
			} catch(Exception e){
				// System.out.println(e);
			}
			endThread = true;

		}

		public String getHost(){
			return client.getInetAddress().getHostAddress();
		}
		public int getPort(){
			return client.getPort();
		}
	}
}
