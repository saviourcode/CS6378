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
        ObjectInputStream inStream;
        try {
            inStream = new ObjectInputStream(serverClient.getInputStream());
            while (true) {
                try {
                    System.out.println("Waiting for data from the socket ...");
                    Message msg = (Message) inStream.readObject();
                    System.out.println("Passed this block " + msg);
                    listener.receive(msg);
                } catch (java.net.SocketException ex) {
                    System.out.println(ex);
                    System.out.println("Calling broken now");
                    inStream.close();
                    listener.broken(clientNodeID);
                    break;
                } catch (Exception ex) {
                    continue;
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}
