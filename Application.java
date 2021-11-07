import java.util.*;
import java.io.*;

class Application implements Listener {

    Node myNode;
    NodeID myID;

    boolean send_next = true;
    int count = 0;

    // Node ids of my neighbors
    NodeID[] neighbors;

    // Flag to check if connection to neighbors[i] has been broken
    boolean[] brokenNeighbors;

    // flag to indicate that the ring detection is over
    boolean terminating;

    String configFile;

    // If communication is broken with one neighbor, tear down the node
    public synchronized void broken(NodeID neighbor) {
    }

    // synchronized receive
    // invoked by Node class when it receives a message
    public synchronized void receive(Message message) {
        Payload p = Payload.getPayload(message.data);

        List<Integer> neigbh_rt = p.getRoutingTable();
        int hop = p.getHop();
        List<List<Integer>> rt = myNode.getRoutingTable();
        HashSet<Integer> st = new HashSet<>();

        for (int j = 0; j < rt.get(hop + 1).size(); j++) {
            st.add(rt.get(hop + 1).get(j));
        }

        for (int i = 0; i < neigbh_rt.size() - 1; i++) {

            if (!st.contains(neigbh_rt.get(i))) {
                rt.get(hop + 1).add(neigbh_rt.get(i));
            }

        }

        myNode.setRoutingTable(rt);

        count++;
        if (count == myNode.getNumNodes()) {
            send_next = true;
            count = 0;
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
		System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        neighbors = myNode.getNeighbors();

        List<List<Integer>> rt = new ArrayList<List<Integer>>();

        int numNode = myNode.getNumNodes();

        for (int i = 0; i < numNode; i++) {
            List<Integer> temp = new ArrayList<>();
            rt.add(temp);
        }
        List<Integer> nodeID = new ArrayList<>();
        for (int i = 0; i < neighbors.length; i++) {
            nodeID.add(neighbors[i].getID());
        }

        rt.add(nodeID);
        myNode.setRoutingTable(rt);

        brokenNeighbors = new boolean[neighbors.length];
        for (int i = 0; i < neighbors.length; i++) {
            brokenNeighbors[i] = false;
        }

        terminating = false;

        for (int i = 0; i < numNode - 1; i++) {
			System.out.println(i);
            Payload p = new Payload(rt.get(i), i);
            Message msg = new Message(myNode.getNodeID(), p.toBytes());
            myNode.sendToAll(msg);
			List<List<Integer>> myRt = myNode.getRoutingTable();
            for (int k = 0; k < myRt.size(); k++) {
                for (int j = 0; j < myRt.get(i).size(); j++) {
                        System.out.print(myRt.get(k).get(j) + " ");
                }
                System.out.println();
            }
            send_next = false;
            while (send_next == false) {
                continue;
            }
        }

        try {
            File myObj = new File(myID + "filename.txt");
            if (myObj.createNewFile()) {
                System.out.println("File created: " + myObj.getName());
                FileWriter myWriter = new FileWriter(myID + "filename.txt");
                List<List<Integer>> myRt = myNode.getRoutingTable();
                for (int i = 0; i < myRt.size(); i++) {
                    myWriter.write(i + ": ");
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

        // for(int i = 0; i < 100; i++)
        // {
        // sleep(1);
        // }

        for (int i = 0; i < neighbors.length; i++) {
            while (!brokenNeighbors[i]) {
                try {
                    // wait till we get a broken reply from each neighbor
                    wait();
                } catch (InterruptedException ie) {
                }
            }
        }
    }
}
