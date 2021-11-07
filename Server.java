import java.net.*;
import java.util.*;

public class Server implements Runnable {

    // Local variables to store the constructor data such as portno., NodeID, ServerNode, listener, NodeInfo Table and reverseNodeInfo Table
    private static final Exception NullPointerException = null;
    int PortNum;
    NodeID NodeID;
    Node ServerNode;
    Listener listener;
    private HashMap<Integer, List<String>> nodeInfo = new HashMap<>();
    private HashMap<String, NodeID> reverseNodeInfo = new HashMap<>();

    // local variable to stop the server thread from running
    private boolean shouldStop = false;

    // Server Thread Constructor
    Server(Node ServerNode) {
        this.ServerNode = ServerNode;
        this.nodeInfo = ServerNode.getNodeInfo();
        this.NodeID = ServerNode.getNodeID();
        this.reverseNodeInfo = ServerNode.getReverseNodeInfo();
        this.PortNum = Integer.parseInt(nodeInfo.get(NodeID.getID()).get(1));
        this.listener = ServerNode.getListener();
    }

    // Method to run the server thread
    public void run() {
        try {
            // Create a listening socket with a timeout of 1000ms
            ServerSocket server = new ServerSocket(this.PortNum);
            server.setSoTimeout(1000);
            System.out.println("Server::run-> Server Started ....");
            while (!shouldStop) {
                try {
                    // Wait for client socket to join in
                    Socket serverClient = server.accept();
                    // Keep the socket connection alive
                    serverClient.setKeepAlive(true);
                    SocketAddress remoteSocketAddress = serverClient.getRemoteSocketAddress();
                    String ClientIP = remoteSocketAddress.toString().substring(1, 13);
                    System.out.println("Server::run-> >> " + "Client No: " + reverseNodeInfo.get(ClientIP).getID() + " " + ClientIP + " started!");
                    // Check if that reverseNodeInfo IP address exists in the reverseNodeInfo map or not
                    if (reverseNodeInfo.get(ClientIP) == null) {
                        System.out.println("Server::run-> Client IP not found in our map, Please re-run");
                        throw NullPointerException;
                    } else {
                        System.out.println("Creating a ClientHandler Thread");
                        // Create a seperate ClientHandler thread for the just created socket
                        ClientHandler sct = new ClientHandler(serverClient, reverseNodeInfo.get(ClientIP), listener); 
                        sct.start();
                    }
                } catch (java.net.SocketTimeoutException e) {
                    System.out.println("Server::run-> Socket Timed Out");
                    continue;
                }

            }

            // Close the server socket when everything is done
            server.close();

        } catch (Exception e) {
            // Check for any exception
            System.out.println(e);
        }
    }

    // Method to make the server thread stop from listening
    public void stop() {
        shouldStop = true;
    }
}
