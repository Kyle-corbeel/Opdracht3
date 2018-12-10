import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class ServerMulticast {

    final static String INET_ADDR = "224.0.0.251"; //Specifiek voor de Pi's
    final static int PORT = 8888;
    public String nodeName;
    private MulticastSocket clientSocket;

    public ServerMulticast(String nodeName) {
        this.nodeName = nodeName;
        initReceiver();
    }

    public void initReceiver() {
        InetAddress address = null;
        try {
            address = InetAddress.getByName(INET_ADDR);     // Create a new Multicast socket (that will allow other sockets/programs
            clientSocket = new MulticastSocket(PORT);       // to join it as well.
            clientSocket.joinGroup(address);                //Join the Multicast group.
            //clientSocket.setReuseAddress(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMulticast(String content) {
        try {
            //Thread.sleep(10);
            InetAddress addr = InetAddress.getByName(INET_ADDR);
            DatagramSocket serverSocket = new DatagramSocket();
            String nodeCorrect = nodeName.split("/")[1];
            //System.out.println(nodeCorrect);
            String msg = content + "\tsender:" + nodeCorrect;
            /**TODO
             Zet de juiste Naam en IP op de lijn hierboven. dees geeft probleme
             **/
            // Create a packet that will contain the data
            // (in the form of bytes) and send it.
            DatagramPacket msgPacket = new DatagramPacket(msg.getBytes(), msg.getBytes().length, addr, PORT);
            serverSocket.send(msgPacket);
            //System.out.println("Handler sent packet with msg: " + msg);
            System.out.println(msg);
            //Thread.sleep(500);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public Message receiveMulticast() {
        byte[] buf = new byte[256];
        try {
            DatagramPacket msgPacket = new DatagramPacket(buf, buf.length);     // Receive the information and print it.
            clientSocket.receive(msgPacket);
            //System.out.println("out");
            //System.out.println("Socket 1 received msg: " + msg);

        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return new Message(new String(buf, 0, buf.length));
    }
}
