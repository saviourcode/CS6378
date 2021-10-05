import java.net.*;
import java.util.*;
public class Server extends Thread {
    int PortNum;
    NodeID NodeID;
    Node ServerNode;
    Listener listener;
    private HashMap<Integer, List<String>> nodeInfo = new HashMap<>();
    private HashMap<String, NodeID> reverseNodeInfo = new HashMap<>();
    private Exception NullPointerException;

    Server(Node ServerNode) {
        this.ServerNode = ServerNode;
        this.nodeInfo = ServerNode.getNodeInfo();
        this.NodeID = ServerNode.getNodeID();
        this.reverseNodeInfo = ServerNode.getReverseNodeInfo();
        this.PortNum = Integer.parseInt(nodeInfo.get(NodeID.getID()).get(1));
        this.listener = ServerNode.getListener();
    }

    public void run() {

        try {
            ServerSocket server = new ServerSocket(this.PortNum);
            System.out.println("Server Started ....");
            while (true) {
                Socket serverClient = server.accept(); // server accept the client connection request
                SocketAddress remoteSocketAddress = serverClient.getRemoteSocketAddress();
                String ClientIP = remoteSocketAddress.toString().substring(1,13);
                System.out.println(" >> " + "Client No: " + reverseNodeInfo.get(ClientIP).getID() + " " + ClientIP + " started!");
                if (reverseNodeInfo.get(ClientIP) == null) {
                    System.out.println("Client IP not found in our map");
                    throw NullPointerException;
                }
                ClientHandler sct = new ClientHandler(serverClient, reverseNodeInfo.get(ClientIP), listener); // Send the request to seperate thread
                sct.start();
            }

        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
