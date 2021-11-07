
//Object to reprsents a node in the distributed system
import java.io.*;
import java.util.*;
import java.net.*;

class Node {
	// node identifier
	private NodeID identifier;

	private Listener listener;

	// Local variable to store the adjacent list of the neighbours
	private List<NodeID[]> neighborsList = new ArrayList<NodeID[]>();

	private List<List<Integer>> routingTable = new ArrayList<>();
	// Map to store the nodeID as key and Hostname and Portnumber as Value
	private HashMap<Integer, List<String>> nodeInfo = new HashMap<>();

	// Reverse Map to store the Hostname and the NodeID
	private HashMap<String, NodeID> reverseNodeInfo = new HashMap<>();

	// Map to store the NodeID and Socket Connection
	private HashMap<Integer, Socket> neighborConn = new HashMap<>();

	// Local variable to store the number of nodes
	private int numNode;

	// Local variable to store the reference of the thread
	private Thread t;

	// Server Class object reference
	private Server T1;

	// constructor
	public Node(NodeID identifier, String configFile, Listener listener) {
		// Your code goes here
		this.identifier = identifier;
		this.listener = listener;

		// Parse the configuration file
		fileParser(configFile);

		// Start a listening server thread
		T1 = new Server(this);
		t = new Thread(T1);
		t.start();

		// create socket connections to the neighbour
		createSocketConn();
	}

	// Helper function the remove the part after the comments
	private String cleanLine(String line) {
		return (line.substring(0, line.indexOf("#") == -1 ? line.length() : line.indexOf("#")).trim());
	}

	// Helper function to check if the given lines contains a integer or not, or if the first digit in the line is number or not.
	private boolean IsInt_ByException(String str) {
		try {
			Integer.parseInt(str);
			return true;
		} catch (NumberFormatException nfe) {
			if (!str.isEmpty() && Character.isDigit(str.charAt(0)))
				return true;
			else
				return false;
		}
	}

	// Helper function to skipInvalidLines by checking if the first character is number or not.
	private String skipInvalidLines(BufferedReader br) {
		String line = "";
		try {
			while (((line = cleanLine(br.readLine())) != null) && (!IsInt_ByException(line))) {
				// System.out.println(line + " " + IsInt_ByException(line));
			}
			return line;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	// Helper function to convert the Host name to the IP address
	private String convertHostToIP(String token) {
		return "10.176.69" + "." + Integer.toString(31 + Integer.parseInt(token.substring(2, 4)));
	}

	// Helper function to parse the configuration file
	private void fileParser(String configFile) {
		File file = new File(configFile);
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line = "";
			String[] tokens;
			int count_of_lines;
			// Find the num of nodes line by skipping invalid line
			line = skipInvalidLines(br);
			// Parse the valid num of nodes line
			numNode = Integer.parseInt(line);
			// System.out.println(numNode);
			// Find the key-value pair of the nodes by skipping invalid line
			line = skipInvalidLines(br);
			// Parse the valid key-value pair of nodes
			count_of_lines = numNode;
			do {
				tokens = line.split("\\s+");
				List<String> hostName_ListenPort = new ArrayList<>();
				hostName_ListenPort.add(convertHostToIP(tokens[1]));
				hostName_ListenPort.add(tokens[2]);
				nodeInfo.put(Integer.parseInt(tokens[0]), hostName_ListenPort);
				reverseNodeInfo.put(convertHostToIP(tokens[1]), new NodeID(Integer.parseInt(tokens[0])));
			} while ((--count_of_lines) != 0 && ((line = cleanLine(br.readLine())) != null));

			// nodeInfo.forEach((key, value) -> System.out.println(key + " " + value.get(0) + " " + value.get(1)));

			// Find the neighbour array by skipping invalid line
			line = skipInvalidLines(br);
			// Parse the List of Neighbours
			count_of_lines = numNode;
			do {
				tokens = line.split("\\s+");
				NodeID[] neighborNode = new NodeID[tokens.length];
				for (int i = 0; i < tokens.length; i++)
					neighborNode[i] = new NodeID(Integer.parseInt(tokens[i]));
				neighborsList.add(numNode - count_of_lines, neighborNode);
			} while ((--count_of_lines) != 0 && ((line = cleanLine(br.readLine())) != null));

			// for (NodeID[] neigbor : neighborsList) {
			// 	for (int i = 0; i < neigbor.length; i++) {
			// 		System.out.println(neigbor[i].getID() + " ");
			// 	}
			// }

		} catch (IOException e) {
			System.out.println("File not found");
		}
	}

	// Helper to create the socket among the neighbour and keep on retrying if the socket couldn't be formed
	private void createSocketConn() {
		NodeID[] neighbors = getNeighbors();

		for (NodeID neighbor : neighbors) {
			String IPaddr = nodeInfo.get(neighbor.getID()).get(0);
			int portNo = Integer.parseInt(nodeInfo.get(neighbor.getID()).get(1));
			boolean retry = true;
			while (retry) {
				try {
					Socket sck = new Socket(IPaddr, portNo);
					neighborConn.put(neighbor.getID(), sck);
					retry = false;
				} catch (Exception e) {
					// System.out.println("Retrying ... for Node " + neighbor.getID());
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		}
	}

	// methods
	public NodeID[] getNeighbors() {
		return neighborsList.get(identifier.getID());
	}

	public void send(Message message, NodeID destination) {
		//System.out.println("Node::send-> Going to send " + destination.getID() + " " + message.data);
		Socket clientServer = neighborConn.get(destination.getID());
		try {
			ObjectOutputStream outStream = new ObjectOutputStream(clientServer.getOutputStream());
			outStream.writeObject(message);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void sendToAll(Message message) {
		// Your code goes here
		NodeID[] neighbors = getNeighbors();
		for (NodeID neighbor : neighbors) {
			send(message, neighbor);
		}
	}

	public void tearDown() {
		neighborConn.forEach((neighborID, socket) -> {
			try {
				socket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});

		T1.stop();
	}

	public void setRoutingTable(List<List<Integer>> recRT)
	{
		this.routingTable = recRT;
	}

	// Getter function for ID
	public NodeID getNodeID() {
		return identifier;
	}

	// Getter function for ID
	public int getNumNodes() {
		return numNode;
	}

	// Getter function for NodeInfo
	public HashMap<Integer, List<String>> getNodeInfo() {
		return nodeInfo;
	}

	// Getter function for ReverseNodeInfo
	public HashMap<String, NodeID> getReverseNodeInfo() {
		return reverseNodeInfo;
	}

	// Getter function for Routing Table
	public List<List<Integer>> getRoutingTable() {
		return routingTable;
	}

	// Getter function for listener object
	public Listener getListener() {
		return listener;
	}
}
