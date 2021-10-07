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
        while (true) {
            try {
                ObjectInputStream inStream = new ObjectInputStream(serverClient.getInputStream());
                System.out.println("CH::run-> Waiting for data from the socket ...");
                Message msg = (Message) inStream.readObject();
                Payload p = Payload.getPayload(msg.data);
                System.out.println("CH::run-> Passed this block " + p.messageType);
                listener.receive(msg);
            } catch (java.net.SocketException ex) {
                System.out.println(ex);
                System.out.println("CH::run-> Calling broken now");
                try {
                    serverClient.setKeepAlive(false);
                } catch (SocketException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                listener.broken(clientNodeID);
                break;
            } catch (Exception ex) {
                System.out.println(ex);
                continue;
            }
        }

    }
}
