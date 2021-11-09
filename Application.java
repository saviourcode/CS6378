import java.util.*;
import java.io.*;

class Application implements Listener {

    Node myNode;
    NodeID myID;

    int count = 0;

    int round = 0;

    boolean recvData[][];

    //Flag to check if connection to neighbors[i] has been broken
	boolean[] brokenNeighbors;

    NodeID[] neighbors;
    int num_neighbors;
    int numNode;

    HashMap<Integer, List<Payload>> buffer = new HashMap<>();

    HashSet<Integer> st = new HashSet<>();

    String configFile;

    //If communication is broken with one neighbor, tear down the node
	public synchronized void broken(NodeID neighbor)
	{
		
	}

    // synchronized receive
    // invoked by Node class when it receives a message
    public synchronized void receive(Message message) {
        // System.out.println("Received Function Called");

        Payload p = Payload.getPayload(message.data);

        if(recvData[p.getHop()][message.source.getID()] == false){
            buffer.computeIfAbsent(p.getHop(), k -> new ArrayList<>()).add(p);
            recvData[p.getHop()][message.source.getID()] = true;
        }

        if (buffer.get(round).size() == num_neighbors) {
            // System.out.println("I Notified All");
            notifyAll();
        }
    }

    public void buildRoutingTable() {
        for (Payload p : buffer.get(round)) {

            List<Integer> neigbh_rt = p.getRoutingTable();
            int hop = p.getHop();

            List<List<Integer>> rt = myNode.getRoutingTable();

            // System.out.println("Neighbour RT for hop: " + hop);
            // for (int i = 0; i < neigbh_rt.size(); i++)
            //     System.out.print(neigbh_rt.get(i) + " ");
            // System.out.println();

            for (int i = 0; i < neigbh_rt.size(); i++) {
                if (!st.contains(neigbh_rt.get(i))) {
                    rt.get(hop + 1).add(neigbh_rt.get(i));
                    st.add(neigbh_rt.get(i));
                }
            }

            // System.out.println("My RT");
            // for (int i = 0; i < rt.size(); i++) {
            //     for (int j = 0; j < rt.get(i).size(); j++)
            //         System.out.print(rt.get(i).get(j) + " ");
            //     System.out.println();
            // }

            myNode.setRoutingTable(rt);
        }
    }

    // Constructor
    public Application(NodeID identifier, String configFile) {
        myID = identifier;
        this.configFile = configFile;
    }

    // Synchronized run. Control only transfers to other threads once wait is called
    public synchronized void run() {
        // Construct node
        myNode = new Node(myID, configFile, this);

        neighbors = myNode.getNeighbors();
        numNode = myNode.getNumNodes();
        num_neighbors = neighbors.length;

        recvData = new boolean[numNode][numNode];

        List<List<Integer>> rt = new ArrayList<List<Integer>>();
        for (int i = 0; i < numNode; i++)
            rt.add(new ArrayList<>());

        List<Integer> nodeID = new ArrayList<>();
        for (int i = 0; i < neighbors.length; i++) {
            nodeID.add(neighbors[i].getID());
            st.add(neighbors[i].getID());
        }
        st.add(myID.getID());

        rt.set(0, nodeID);

        myNode.setRoutingTable(rt);

        for (round = 0; round < numNode - 1; round++) {
            // System.out.println("Going to send for hop: " + round);

            Payload p = new Payload(myNode.getRoutingTable().get(round), round);
            Message msg = new Message(myNode.getNodeID(), p.toBytes());
            myNode.sendToAll(msg);

            try {
                wait();
                buildRoutingTable();
                // System.out.println("Final RT");
                // List<List<Integer>> myRt = myNode.getRoutingTable();
                // for (int k = 0; k < myRt.size(); k++) {
                //     for (int j = 0; j < myRt.get(k).size(); j++) {
                //         System.out.print(myRt.get(k).get(j) + " ");
                //     }
                //     System.out.println();
                // }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        for(int i = 0; i < numNode-1;i++)
        {
            Payload p = new Payload(myNode.getRoutingTable().get(i), i);
            Message msg = new Message(myNode.getNodeID(), p.toBytes());
            myNode.sendToAll(msg);
        }

        try {
            File myObj = new File(myID.getID() + "filename.txt");
            if (myObj.createNewFile()) {
                System.out.println("File created: " + myObj.getName());
                FileWriter myWriter = new FileWriter(myID.getID() + "filename.txt");
                List<List<Integer>> myRt = myNode.getRoutingTable();
                for (int i = 0; i < myRt.size(); i++) {
                    myWriter.write(i + 1 + ": ");
                    for (int j = 0; j < myRt.get(i).size(); j++) {
                        myWriter.write(myRt.get(i).get(j) + " ");
                    }
                    myWriter.write("\n");
                }
                myWriter.close();

            } else {
                System.out.println("File already exists.");
            }
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        myNode.tearDown();
    }
}
