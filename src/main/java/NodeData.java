import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Basic information:
 * A class we made to store all of the data of the node.
 */
public class NodeData {
    private static String nodeID="";
    private static String nodeName="";
    private int nextNode;
    private int previousNode;
    private boolean hasNeighbours = false;
    private int nodeHash=0;
    private String nodeIP;
    private String serverIP="";

    public NodeData() {
        nodeIP = generateIP();

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("Welcome to the filesharing service!\nPlease insert your name: ");
            String naam = br.readLine();
            nodeName = naam;
            nodeID = nodeIP + ":" + nodeName;
            nodeHash = hash(nodeID);
            System.out.println("\n\t\tWELCOME\n\tYour hash is: "+nodeHash+"\n");

        }catch(IOException e){
            e.printStackTrace();
        }

    }

    public NodeData(boolean isServer){
        nodeIP = generateIP();
        serverIP = nodeIP;
        nodeName = "namingServer";
        nodeID = nodeIP + ":" + nodeName;
        nodeHash = hash(nodeID);

        System.out.println("Naming Server booted\nServer-IP: "+nodeIP);
        System.out.println("Awaiting nodes...");
    }

    private String generateIP() {
        String ip="";
        try {
            final DatagramSocket socket = new DatagramSocket();                 //Haalt IP van host
            ip = InetAddress.getLocalHost().toString().split("/")[1];
            //System.out.println(ip);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ip;
    }

    public static String getNodeName() {
        return nodeName;
    }

    public int getNextNode() {
        return nextNode;
    }

    public void setNextNode(int nextNode) {
        this.nextNode = nextNode;
    }

    public int getPreviousNode() {
        return previousNode;
    }

    public void setPreviousNode(int previousNode) {
        this.previousNode = previousNode;
    }

    public int getMyHash() {
        return nodeHash;
    }

    public String getNodeID(){
        return nodeID;
    }

    public String getNodeIP(){
        return nodeIP;
    }

    public String getServerIP() {
        return serverIP;
    }

    public void setServerIP(String serverIP) {
        this.serverIP = serverIP;
    }

    public int hash(String name) {
        int hash;
        hash = Math.abs(name.hashCode()) % 327680;
        return hash;
    }

    public void startRMI() {

    }
}
