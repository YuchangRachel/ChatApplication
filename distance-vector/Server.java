import java.net.*;
import java.util.*;
import java.io.*;

public class Server {

	static Map<Integer, HashMap<Integer, Integer>> routingTables; 
	static Map<String, Integer> neighborIpToId;
	static Map<Integer, Node> neighbors;
	static Map<Integer, Integer> edges;
	static int myID;
	static boolean isCrashed = false;
	static int packets = 0;

	public static int maxNumPeers;
	private int id;
	static int listeningPort;
	private ServerSocket socketListener;
	private Socket clientSocket;
	private String hostAddress;
	public static HashMap<Integer, Client> clientList;
	private static HashMap<Client, Socket> clientSocketMap;
	DatagramSocket datagramSocket;


	public Server(int listeningPort) {
		this.listeningPort = listeningPort;
		maxNumPeers = 0;

		try{
			socketListener = new ServerSocket(listeningPort);
		}catch(Exception e){
			e.printStackTrace();
		}

		hostAddress = "";
		clientSocket = null;
		clientList = new HashMap<>();
		clientSocketMap = new HashMap<>();
		System.out.println("Welcome!");
	}

	public static void updateInterval(int time){
		new Thread(() -> {
			try{
				while(!isCrashed){
					Thread.sleep(time * 1000);
					if (!isCrashed ){
						Server.updateAll();
					}

				}
			} catch (Exception e){
				System.out.println("Unable to run.");
				e.printStackTrace();
			}
		}).start();
	}

	// Get the listening port number of the host
	public int getPortNumber() {
		return listeningPort;
	}


	//udp socket communication
	// Sets up the server socket and establishes connection with clients
	public void initializeListenerSocket() {
		System.out.println("Listening on port:  " + listeningPort);

		byte[] incomingMessage = new byte[1024];
		DatagramPacket packet = new DatagramPacket(incomingMessage, incomingMessage.length);

		new Thread(() -> {
			try {
				datagramSocket = new DatagramSocket(listeningPort);
				while (true) {
					try {
						if (datagramSocket != null) {
							datagramSocket.receive(packet);
							packets++;
							String newData = new String(packet.getData(), 0, packet.getLength());
							deserializePacketData(newData);
						} else {
							System.out.println("The datagram socket is NULL!");
							Thread.currentThread().interrupt();

							return;
						}
					} catch (Exception e) {
						System.out.println("Cannot connect to client!");
						e.printStackTrace();
					}
				}
			} catch (SocketException e) {
				e.printStackTrace();
			}

		}).start();
	}


	/**
	 * The routing updates are sent using this specific message format
	 * @param data The Receieved Packet
	 */
	public void deserializePacketData(String data) {
		String [] message = data.split(",");
		int i = 0;
		int numUpdates = Integer.parseInt(message[0]);
		String serverPort = message[1];
		String serverIp = message[2];
		int serverId = neighborIpToId.get(serverIp);
		int messageElementsIndex = 3;
		int myCostToServer;

		if(edges.containsKey(serverId)){
			myCostToServer = edges.get(serverId);
		} else {
			myCostToServer = Integer.MAX_VALUE;
		}

		for(int j = 0; j < numUpdates; j++) {
			String neighborIp = message[messageElementsIndex++];
			String neighborPort = message[messageElementsIndex++];
			int neighborId;
			int neighborCost;
			if(!message[messageElementsIndex].equals("null")){
				neighborId = Integer.parseInt(message[messageElementsIndex++]);

			} else {
				neighborId = -1;
				messageElementsIndex++;
			}
			if(!message[messageElementsIndex].equals("null")) {
				if(message[messageElementsIndex].contains("inf")) {
					neighborCost = Integer.MAX_VALUE;
					messageElementsIndex++;
				} else {

					neighborCost = Integer.parseInt(message[messageElementsIndex++]);
				}

			} else {
				neighborCost = Integer.MAX_VALUE;
				messageElementsIndex++;
			}

			if(neighborId == myID) {
				edges.put(serverId, neighborCost);
			}
			routingTables.get(serverId).put(neighborId, neighborCost);
		}
		if(serverId != myID && isCrashed == false){
			System.out.println("\nMESSAGE RECEIVED FROM SERVER " + serverId);
		}

	}

	/**
	 * This function is responsible for sending messages to all servers.
	 * It uses a HashMap of its neighbors and creates packets using DatagramPackets.
	 */
	public static void updateAll() {
		if(!isCrashed){
			for (int neighborId : neighbors.keySet()) {
				byte [] packet = createDVUpdatePacket();
				try {
					send(neighborId, packet);
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		} else {
			System.out.println("Cannot updateAll() after a crash!");
		}
	}

	public static byte [] createDVUpdatePacket() {
		String message = "";
		message += edges.size() + ",";
		message += neighbors.get(myID).port + ",";
		message += neighbors.get(myID).ip + ",";
		for(Integer neighborId : neighbors.keySet()){
			message += neighbors.get(neighborId).ip + ",";
			message += neighbors.get(neighborId).port + ",";
			message += neighborId + ",";
			message += edges.get(neighborId) + ",";
		}
		return message.getBytes();
	}

	/**
	 * This is a function for sending packets to different servers. 
	 * It uses a DatagramSocket and a DatagramPacket to create and send a packet, and a HashMap of its neighbors. 
	 * 
	 * @param id			the id of the server the message is getting sent to.
	 * @param messageBytes	A byte array that is going to store the packet being sent.
	 * @throws IOException	generic exception.
	 * 
	 */
	public static void send(int id, byte [] messageBytes) throws IOException {

		Node client = neighbors.get(id);
		DatagramSocket datagramSocket = new DatagramSocket();
		InetAddress recipient = InetAddress.getByName(client.ip);
		DatagramPacket packet = new DatagramPacket(messageBytes, messageBytes.length, recipient,
				Integer.parseInt(client.port));
		try {
			datagramSocket.send(packet);

		} catch(Exception e){
			System.out.println("Error sending to server " + id);
			e.printStackTrace();
		}
	}
	/**
	 * 
	 * This reads all the topology files and stores our server and neighbor data in Hash Maps.
	 * @param filename		A reference to the topology file that is for this server. 
	 * @see neighbors  		A Hash Map of this nodes neighbors, stored as an integer for which neighbor it is, a Node object for the neighbor.
	 * @see edges 	 		A Hash Map of the neighbors and the cost to get to them.
	 * @see neighborIpToID	A Hash Map that maps out the server ID to each servers IP.
	 */
	public static void readTopology(String filename) throws Exception{
		neighbors = new HashMap<>();
		edges = new HashMap<>();
		neighborIpToId = new HashMap<>();

		BufferedReader reader = null;
		//open file
		try {
			reader = new BufferedReader(new FileReader(filename));

			int numberOfServers = Integer.parseInt(reader.readLine());
			int numberOfNeighbors = Integer.parseInt(reader.readLine());

			for (int i = 0; i < numberOfServers; i++){
				String[] str = reader.readLine().split(" ");
				int serverID = Integer.parseInt(str[0]);
				String serverIP = str[1];
				String serverPort = str[2];
				//routingtable size is 4
				routingTables.put(serverID, new HashMap<>());
				Node newNode = new Node(serverIP, serverPort);
				neighbors.put(serverID, newNode);
				neighborIpToId.put(serverIP, serverID);
			}

			for (int j = 0; j < numberOfNeighbors; j++){
				String[] parts = reader.readLine().split(" ");
				int fromServerID = Integer.parseInt(parts[0]);
				int toServerID = Integer.parseInt(parts[1]);
				int cost = Integer.parseInt(parts[2]);

				myID = fromServerID;
				edges.put(toServerID, cost);

			}
			edges.put(myID, 0);

			//debug
			for (Integer id : neighbors.keySet()){
				Integer key = id;
				Node n = neighbors.get(key);
				System.out.println(key + " " + n.ip + " " + n.port);
			}
			for (Integer edge : edges.keySet()){
				Integer key = edge;
				Integer value = edges.get(key);
				System.out.println(key + " " + value);
			}

		} catch (FileNotFoundException e) {
			System.out.println("Such file not Found!");
		}
	}

    /**
	 * Displays the server's neighbors and the routing table in the consol
	 */
	public static void display() {
		Set<Integer> edgeIds = edges.keySet();
		System.out.println("\nServer " + myID + "'s Cost Table: \n<Neighbor-ID><Server-ID><Cost>");
		for (int id : edgeIds) {
			int currentEdgeCost = edges.get(id);
			String curId = (myID == Integer.MAX_VALUE) ? "inf" : (myID + "") ;
			String curCost = (currentEdgeCost == Integer.MAX_VALUE) ? "inf" : (currentEdgeCost + "");
			System.out.println("      "+id + "\t        " + curId + "\t  " + curCost);
		}

		int [] costs = new int[routingTables.size()];
		int [] nextHops = new int[routingTables.size()];

		for(int i = 0; i < routingTables.size(); i++) {
			costs[i] = Integer.MAX_VALUE;
			nextHops[i] = Integer.MAX_VALUE;
		}
		for(int nodeId : routingTables.keySet()) {

			if(edges.containsKey(nodeId)) {
				costs[nodeId - 1] = edges.get(nodeId);
				nextHops[nodeId - 1] = nodeId;
			}
		}
		//calculate the shortest distance
		for(int nodeId : routingTables.keySet()) {
			if(nodeId != myID) {
				for(int nodeNeighborId : routingTables.get(nodeId).keySet()) {
					if(nodeNeighborId != myID) {
						if(costs[nodeId - 1] < Integer.MAX_VALUE) {
							if (routingTables.get(nodeId).get(nodeNeighborId) != Integer.MAX_VALUE) {
								int totalCostFromMeToThisNode = routingTables.get(nodeId).get(nodeNeighborId) + costs[nodeId - 1];
								if(totalCostFromMeToThisNode < costs[nodeNeighborId - 1]){
									costs[nodeNeighborId - 1] = totalCostFromMeToThisNode;
									nextHops[nodeNeighborId - 1] = nodeId;
								} 
							}
						} 
					} 
				}
			}
		}
		System.out.println("\nRouting Table for server No. " + myID);
		System.out.println(" ___________________________________");
		System.out.println("|    dest   |  nexthop  |    cost   |");
		System.out.println("|___________|___________|___________|");
		for(int i = 0; i < routingTables.size(); i++){
			int check = costs[i];
			if (check == Integer.MAX_VALUE)
			{
				if(nextHops[i] == Integer.MAX_VALUE) {
					System.out.println("|      "+(i+1)+"    |    "+"inf"+"    |     "+"inf"+"   |");

				} else  {
					System.out.println("|      "+(i+1)+"    |     "+nextHops[i]+"     |     "+"inf"+"   |");
				}
			} else {
				if(nextHops[i] == Integer.MAX_VALUE) {
					System.out.println("|      "+(i+1)+"    |    "+"inf"+"    |      "+costs[i]+"    |");

				} else {
					System.out.println("|      "+(i+1)+"    |     "+nextHops[i]+"     |      "+costs[i]+"    |");

				}

			}
			System.out.println("|___________|___________|___________|");
		}
	}

	/**
	 * 	Updates the cost of a link between two nodes.
	 * @param server1 This servers Node
	 * @param server2 The destination node to be updated
	 * @param cost The new cost to the destination node
	 */
	 public static void update(int server1, int server2, int cost) {
        if (myID != server1) {
            System.out.println("wrong server id");
        } else {
			if (edges.containsKey(server2)){
				if(edges.get(server2) != Integer.MAX_VALUE){
					edges.put(server2, cost);
					System.out.println("update command: SUCCESS!");
				} else {
					System.out.println("This server isn't one of your neighbors");
					System.out.println("update command: FAILED!");
				}
			} else {
				System.out.println("update command: FAILED!");
			}
        }
    }

	//disable id
	public static void disable(int disabledServer) {
        Server.update(myID, disabledServer, Integer.MAX_VALUE);
    }

	//cash
    public static void crash() {
        for (int neighborid : edges.keySet()) {
            Server.update(myID, neighborid, Integer.MAX_VALUE);
        }


    }

}
