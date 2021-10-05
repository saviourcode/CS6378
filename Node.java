
//Object to reprsents a node in the distributed system
import java.io.*;
import java.util.*;
import java.net.*;

class Node {
	// node identifier
	private NodeID identifier;

	private Listener listener;

	private List<NodeID[]> neighborsList = new ArrayList<NodeID[]>();

	private HashMap<Integer, List<String>> nodeInfo = new HashMap<>();

	private HashMap<String, NodeID> reverseNodeInfo = new HashMap<>();

	private HashMap<Integer, Socket> neighborConn = new HashMap<>();

	private int numNode;

	private Thread t;

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

		Scanner sc = new Scanner(System.in);

		sc.nextLine();

		// create socket connections to the neighbour
		createSocketConn();
	}

	private String cleanLine(String line) {
		return (line.substring(0, line.indexOf("#") == -1 ? line.length() : line.indexOf("#")).trim());
	}

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

	private String skipInvalidLines(BufferedReader br) {
		String line = "";
		try {
			while (((line = cleanLine(br.readLine())) != null) && (!IsInt_ByException(line))) {
				System.out.println(line + " " + IsInt_ByException(line));
			}
			return line;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private String convertHostToIP(String token) {
		return "10.176.69" + "." + Integer.toString(31 + Integer.parseInt(token.substring(2, 4)));
	}

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
			System.out.println(numNode);
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

			nodeInfo.forEach((key, value) -> System.out.println(key + " " + value.get(0) + " " + value.get(1)));

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

			for (NodeID[] neigbor : neighborsList) {
				for (int i = 0; i < neigbor.length; i++) {
					System.out.print(neigbor[i].getID() + " ");
				}
				System.out.println();
			}
		} catch (IOException e) {
			System.out.println("File not found");
		}
	}

	private void createSocketConn() {
		NodeID[] neighbors = getNeighbors();

		for (NodeID neighbor : neighbors) {
			String IPaddr = nodeInfo.get(neighbor.getID()).get(0);
			int portNo = Integer.parseInt(nodeInfo.get(neighbor.getID()).get(1));
			try {
				neighborConn.put(neighbor.getID(), new Socket(IPaddr, portNo));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	// methods
	public NodeID[] getNeighbors() {
		return neighborsList.get(identifier.getID());
	}

	public void send(Message message, NodeID destination) {
		Socket clientServer = neighborConn.get(destination.getID());
		try {
			OutputStream outStream = new DataOutputStream(clientServer.getOutputStream());
			outStream.write(message.data);
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

	// Getter function for ID
	public NodeID getNodeID() {
		return identifier;
	}

	public HashMap<Integer, List<String>> getNodeInfo() {
		return nodeInfo;
	}

	public HashMap<String, NodeID> getReverseNodeInfo() {
		return reverseNodeInfo;
	}

	public Listener getListener() {
		return listener;
	}
}
