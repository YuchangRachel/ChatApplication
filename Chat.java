import java.io.*;
import java.net.*;
import java.util.*;

public class Chat {
	//global variable tacking peer who is connected 
	public static List<Peer> chatList = new ArrayList<Peer>();


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
				String[] lines = input.split(" ");

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
					System.out.print(connect(input));
				}
				else if (lines[0].equals("list")){
					list();
				}
				else if (lines[0].equals("send")){
					System.out.print(send(input));
				}
				else if (lines[0].equals("terminate")){

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

	//client connect to other server is connected who also need update list 
	public static void addChatList(Peer peer){
		chatList.add(peer);
	}

	//connect <destination ip><destination port>
	public static String connect(String input){
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


		try{
			Socket socket = new Socket(desIp, desPort);
			Peer peer = new Peer(socket, desPort);

			//testing
			/*
			   if (desIp.equals(getMyIp())){
			   return "Self connection failed!!!\n";   //self connection situation
			   }
			//if (duplicate connection using list)
			*/

			peer.sendMessage("The connection to another peer is successfully established");

			//add peer into list
			chatList.add(peer);

			return "The connection to peer " + desIp + " is successfully established\n";

		}catch(UnknownHostException e){
			return "It is unknown hostname, connection failed!!!";
		}
		catch(Exception i){
			return "Connection failed!!!";
		}


	}

	//list 
	public static void list(){
		System.out.println("id:   IP address:       Port No.:");
		for (Peer peer : chatList){
			System.out.println(peer.getList());
		}
	}

	//send <connection id><message>
	public static String send(String input){
		String[] part = input.split(" ");
		int connId = part[0];
		String msg = part[1];

		if (connInt < 0 || connInt > chatList.size())
			return "Connection id is out of bound, please check it in 'list'command!!!";
		if (msg.length() > 100)
			return "Message is out of 100 characters long, including blank spaces!!!";


	}


	//validate ip
	public static boolean isValidIPv4(String ip) {
		if (ip.length() < 7) return false;
		if (ip.charAt(0) == '.') return false;
		if (ip.charAt(ip.length()-1) == '.') return false;
		String[] tokens = ip.split("\\.");
		if(tokens.length!=4) return false;
		for(String token:tokens) {
			if(!isValidIPv4Token(token)) return false;
		}
		return true;
	}
	public boolean isValidIPv4Token(String token) {
		if(token.startsWith("0") && token.length()>1) return false;
		try {
			int parsedInt = Integer.parseInt(token);
			if(parsedInt<0 || parsedInt>255) return false;
			if(parsedInt==0 && token.charAt(0)!='0') return false;
		} catch(NumberFormatException nfe) {
			return false;
		}
		return true;
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


}
