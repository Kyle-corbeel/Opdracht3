import org.w3c.dom.Node;

import java.io.IOException;
import java.net.*;

public class MulticastSender {

    public NodeData data;
    InetAddress address;

    final static String INET_ADDR = "224.0.0.251";
    final static int PORT = 4567;
    private MulticastSocket clientSocket;

    public MulticastSender(NodeData data){
        this.data = data;

        try {
            address = InetAddress.getByName(INET_ADDR); // Create a new Multicast socket (that will allow other sockets/programs
            clientSocket = new MulticastSocket(PORT);   // to join it as well.
            clientSocket.setNetworkInterface(NetworkInterface.getByInetAddress(InetAddress.getByName(data.generateIP())));
            clientSocket.joinGroup(address);            //Join the Multicast group.

            clientSocket.setReuseAddress(true);
            //clientSocket.setSoTimeout(5000);
        }catch (UnknownHostException e){
            e.printStackTrace();
        }catch(SocketException d){
            d.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMulticast(String content) {
        // InetAddress addr = null;

        try {
            DatagramSocket serverSocket = new DatagramSocket();                     // Create a packet that will contain the data
            String msg = content + "\tsender:" + data.getNodeID() + "#";            // (in the form of bytes) and send it.
            System.out.println("Sending: "+msg);
            DatagramPacket msgPacket = new DatagramPacket(msg.getBytes(), msg.getBytes().length, address, PORT);
            serverSocket.send(msgPacket);
            Thread.sleep(500);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
