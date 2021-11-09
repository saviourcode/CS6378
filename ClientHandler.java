import java.net.*;
import java.io.*;
import java.util.*;

class ClientHandler extends Thread {

    // Local variables to store client socket information
    Socket serverClient;
    NodeID clientNodeID;
    Listener listener;

    // Constructor to initialise the clientHandler thread
    ClientHandler(Socket inSocket, NodeID clientNodeID, Listener listener) {
        this.serverClient = inSocket;
        this.clientNodeID = clientNodeID;
        this.listener = listener;
    }

    // Method to start the Client Handler thread
    public void run() {
        while (true) {
            try {
                // Get the input stream from the socket
                ObjectInputStream inStream = new ObjectInputStream(serverClient.getInputStream());
                // System.out.println("CH::run-> Waiting for data from the socket ...");
                // Read the message object
                Message msg = (Message) inStream.readObject();
                // Convert it into the payload and get the data and source ID
                // Payload p = Payload.getPayload(msg.data);
                // System.out.println("CH::run-> Hop Number " + p.getHop());
                // List<Integer> rt = p.getRoutingTable();
                // for(int i = 0; i< rt.size(); i++ )
                // {
                //     System.out.print(rt.get(i) + " ");
                // }
                // System.out.println();
                // Pass the message object to the listener.receive method to notifyall
                listener.receive(msg);
            } catch (java.net.SocketException ex) {
                System.out.println(ex);
                // System.out.println("CH::run-> Calling broken now");
                // If the socket is closed then make don't make the socket as alive
                try {
                    serverClient.setKeepAlive(false);
                } catch (SocketException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                // Call the broken to notify all the other neighbour to also close the socket
                listener.broken(clientNodeID);
                break;
            } catch (Exception ex) {
                System.out.println(ex);
                // If EOF Exception is raised then also call the broken method to close the other socket connection
                listener.broken(clientNodeID);
                break;
            }
        }
    }
}
