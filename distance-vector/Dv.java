import java.net.*;
import java.io.*;
import java.util.*;

public class Dv{
	public static void main(String[] args) throws Exception{

		//server startup command
		try{
			Server.routingTables = new HashMap<>();

			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			boolean start = false;
			String input = br.readLine();
			String[] lines = input.split(" ");

			//initialize routing table
			Server.routingTables = new HashMap<>();

			//server startup command server -t <topology-file-name> -i <routing-update-updateInterval>
			while (!start) {
				if (lines[0].equals("server")){
					if (lines.length == 5 && lines[1].equals("-t") && lines[3].equals("-i")){
						try{
							if (Integer.parseInt(lines[4]) > 0){
								start = true;
								break;
							}
							else {
								System.out.println("Routing update updateInterval should more than 0");
								System.exit(1);
							}
						}catch(NumberFormatException ne){
							System.out.println("Please enter a integer for rounting update updateInterval");
							System.exit(1);
						}
					}else {
						System.out.println("Please enter command \"server -t <topology-file-name> -i <routing-update-updateInterval>\" to begin.");
						System.exit(1);
					}
				} else {
					System.out.println("Please enter command \"server -t <topology-file-name> -i <routing-update-updateInterval>\" to begin.");
					System.exit(1);
				}
			}

			//read topology file
			String filename = lines[2];
			Server.readTopology(filename);
			
			int myPort = Integer.parseInt(Server.neighbors.get(Server.myID).port); 
			int updateInterval = Integer.parseInt(lines[4]);

			Server server = new Server(myPort);
			System.out.println("Server is ready!!!");
			String command = "";
			server.initializeListenerSocket();
			Server.updateInterval(updateInterval);


			//enter second level command input
			do {
				boolean success = false;
				Scanner s = new Scanner(System.in);
				command = s.nextLine();
				String[] parts = command.split(" ");
				String currentCommand = "";

				if (command.contains("update")) { 
					currentCommand = "update";
					if(parts.length < 4) {
						System.out.println("Enter the right amount of arguments");
					}
					else {
						try {
							int cost = 0;
							int server1 = Integer.parseInt(parts[1]);
							int server2 = Integer.parseInt(parts[2]);
							if(parts[3].equals("inf") ){
								cost = Integer.MAX_VALUE;
							} else{
								cost = Integer.parseInt(parts[3]); 
							}
							Server.update(server1, server2, cost);
						} catch(Exception e) {
							e.printStackTrace();
						}
					}
				} else if (command.equals("step")) { 
					Server.updateAll();
					Server.packets++;
					success = true;
				} else if (command.equals("packets")) { 
					System.out.println("You have received " + Server.packets + " packets since the last update.");
					Server.packets = 0;
					success = true;
				} else if (command.equals("display")) {
					Server.display();
					success = true;
				} else if (command.contains("disable")) { 
					if(parts.length < 2) {
						System.out.println("Enter the right amount of arguments");
					}
					else {
						int disabledServer = Integer.parseInt(parts[1]);
						Server.disable(disabledServer);
						success = true;
					}
				} else if (command.equals("crash")) { 
					for(int neigh : Server.edges.keySet() ){
						Server.edges.put(neigh, Integer.MAX_VALUE);
					}
					Server.updateAll();

					Server.crash();
					Server.isCrashed = true;
					success = true;
				} else if (command.equals("exit")) { 
					success = true;
					Server.crash();
					System.out.println("Thank you for using,GOOD BYE");

				} else {
					System.out.println("Enter valid command input!!");
				}

				//execution fail or success
				if(currentCommand != "update"){
					System.out.println("Testing part: " + parts[0]);
					if(success == false){
						System.out.println(parts[0] + " command: FAIL!");
					}
					else{
						System.out.println(parts[0] + " command: SUCCESS!");
					}
				}
				currentCommand = "";

			} while (!command.equals("exit"));
			if (command.equals("exit")) { 
				System.exit(0);
			}
		}catch(Exception e){}
	}


}


