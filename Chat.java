package com.chatapplication
import java.io.*;
import java.net.*;
import java.util.*;

public class Chat {
	public static void main(String[] args) throws Exception{
		//no arguments passed
		if (args.length == 0){
			System.out.println("Enter your port number!!!");
			System.exit(0);
		}

		String ip = getMyIp();
		String port = new String();
		port += args[0];

		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(System.in));
			String addr = InetAddress. getLocalHost().getLoopbackAddress().toString();

			while (true){
				String input = br.readLine();

				if (input.equals("myport")){
					System.out.println("The program runs on port number: " + port);
				}
				if(input.equals("myip")){
					System.out.println("The IP Address: " + ip);
				}
				if (input.equals("help")){
					help();
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
	public static void connectTo() throws SocketException{
		Connect connection = new Connect()

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
