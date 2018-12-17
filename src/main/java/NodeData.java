import java.net.DatagramSocket;
import java.net.InetAddress;

public class NodeData {
    private static String nodeName="";
    private static String hostName="";
    private int nextNode;
    private int previousNode;
    private boolean hasNeighbours = false;
    private int myHash=0;
    private String ip;

    public NodeData(String name) {
        try {
            final DatagramSocket socket = new DatagramSocket();                 //Haalt IP van host
            ip = InetAddress.getLocalHost().toString().split("/")[1];
            //System.out.println(ip);
        } catch (Exception e) {
            e.printStackTrace();
        }
        hostName = name;
        nodeName = ip + ":" + name;
    }

    public static String getHostName() {
        return hostName;
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
        return myHash;
    }

    public void setMyHash(int myHash) {
        this.myHash = myHash;
    }

    public String getMyName(){
       // System.out.println(nodeName);
        return nodeName;
    }
}
