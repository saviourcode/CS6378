import java.net.*;
import java.util.*;

public class Server implements Runnable {
    private static final Exception NullPointerException = null;
    int PortNum;
    NodeID NodeID;
    Node ServerNode;
    Listener listener;
    private HashMap<Integer, List<String>> nodeInfo = new HashMap<>();
    private HashMap<String, NodeID> reverseNodeInfo = new HashMap<>();
    private boolean shouldStop = false;

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
            // server.setSoTimeout(1000);
            System.out.println("Server::run-> Server Started ....");
            while (!shouldStop) {
                try {
                    Socket serverClient = server.accept(); // server accept the client connection request
                    serverClient.setKeepAlive(true);
                    SocketAddress remoteSocketAddress = serverClient.getRemoteSocketAddress();
                    String ClientIP = remoteSocketAddress.toString().substring(1, 13);
                    System.out.println("Server::run-> >> " + "Client No: " + reverseNodeInfo.get(ClientIP).getID() + " " + ClientIP
                            + " started!");
                    if (reverseNodeInfo.get(ClientIP) == null) {
                        System.out.println("Server::run-> Client IP not found in our map");
                        throw NullPointerException;
                    } else {
                        ClientHandler sct = new ClientHandler(serverClient, reverseNodeInfo.get(ClientIP), listener); 
                        sct.start();
                    }
                } catch (java.net.SocketTimeoutException e) {
                    System.out.println("Server::run-> Socket Timed Out");
                    continue;
                }

            }

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void stop() {
        shouldStop = true;
    }
}
