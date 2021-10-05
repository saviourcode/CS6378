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
                System.out.println(inStream);
                byte[] data = new byte[100];
                inStream.read(data);
                System.out.println("Passed this block " + data);
                Message msg = new Message(clientNodeID, data);
                listener.receive(msg);
                inStream.close();
            } catch (java.net.SocketException ex) {
                listener.broken(clientNodeID);
                System.out.println(ex);
                break;
            }
            catch (Exception ex){
                continue;
            }
        }
        
    }
}
