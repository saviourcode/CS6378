import java.net.*;
import java.io.*;

class ClientHandler extends Thread {
    Socket serverClient;
    NodeID clientNodeID;
    int squre;
    Listener listener;

    ClientHandler(Socket inSocket, NodeID clientNodeID, Listener listener) {
        this.serverClient = inSocket;
        this.clientNodeID = clientNodeID;
        this.listener = listener;
    }

    public void run() {
        while(true)
        {
            try {
                DataInputStream inStream = new DataInputStream(serverClient.getInputStream());
                byte[] data = new byte[100];
                System.out.println("Waiting for data from the socket ...");
                inStream.read(data);
                System.out.println("Passed this block " + data);
                Message msg = new Message(clientNodeID, data);
                listener.receive(msg);
                inStream.close();
            } catch (java.net.SocketException ex) {
                System.out.println(ex);
                System.out.println("Calling broken now");
                listener.broken(clientNodeID);
                break;
            }
            catch (Exception ex){
                continue;
            }
        }
        
    }
}
