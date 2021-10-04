import java.net.*;
import java.io.*;

class ClientHandler extends Thread {
    Socket serverClient;
    NodeID clientNodeID;
    int squre;
    Listener listener;

    ClientHandler(Socket inSocket,NodeID clientNodeID, Listener listener){
      this.serverClient = inSocket;
      this.clientNodeID = clientNodeID;
      this.listener = listener;
    }

    public void run(){
      try{
        DataInputStream inStream = new DataInputStream(serverClient.getInputStream());
        byte[] data = new byte[100];
        inStream.readFully(data);
        Message msg = new Message(clientNodeID, data);
        listener.receive(msg);
        // while(!clientMessage.equals("bye")){
        //   clientMessage=inStream.readUTF();
        //   System.out.println("From Client-" +clientNo+ ": Number is :"+clientMessage);
        //   squre = Integer.parseInt(clientMessage) * Integer.parseInt(clientMessage);
        //   serverMessage="From Server to Client-" + clientNo + " Square of " + clientMessage + " is " +squre;
        //   outStream.writeUTF(serverMessage);
        //   outStream.flush();
        // }
        inStream.close();
        serverClient.close();
      }catch(Exception ex){
        System.out.println(ex);
      }finally{
        System.out.println("Client -" + clientNodeID.getID() + " exit!! ");
      }
    }
  }