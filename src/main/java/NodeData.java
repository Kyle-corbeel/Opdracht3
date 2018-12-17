import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class NodeData {
    private static String nodeName="";
    private int nextNode;
    private int previousNode;
    private boolean hasNeighbours = false;
    private int myHash=0;
    private String ip;
    public static NodeData instance;


    private NodeData(String name) {

    }

    public static NodeData getInstance(){
        return instance;
    }

    public void initNodeData(String name){
        try {
            final DatagramSocket socket = new DatagramSocket();                 //Haalt IP van host
            ip = InetAddress.getLocalHost().toString().split("/")[1];
            //System.out.println(ip);
        } catch (Exception e) {
            e.printStackTrace();
        }
        nodeName = ip + ":" + name;

        /*
        try {
            final DatagramSocket socket = new DatagramSocket();                 //Haalt IP van host
            ip = getIp();
            //System.out.println(ip);
        } catch (Exception e) {
            e.printStackTrace();
        }
        nodeName = ip + ":" + name;*/
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

    public String getIp() {
        String ip = "";
        try {
            NetworkInterface eth = NetworkInterface.getByName("eth0");
            Enumeration<InetAddress> adresNUM = eth.getInetAddresses();
            while(adresNUM.hasMoreElements() && ip.equals(""))
            {
                InetAddress addr = adresNUM.nextElement();
                if(addr instanceof Inet4Address && !addr.isLoopbackAddress())
                {
                    ip = addr.getHostAddress();
                }
            }
            return "" + ip;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
