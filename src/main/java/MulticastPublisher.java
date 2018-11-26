import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class MulticastPublisher {
    private DatagramSocket socket;
    private InetAddress group;
    private byte[] buf;
    private String name;

    public MulticastPublisher(String nodeName) throws IOException {

        this.name = nodeName;
        MulticastReceiver receiver = new MulticastReceiver(nodeName);

    }

    public void multicast(String multicastMessage) throws IOException {
        socket = new DatagramSocket();
        group = InetAddress.getByName("230.0.0.0");
        buf = (multicastMessage +" sender:" +name).getBytes();

        DatagramPacket packet
                = new DatagramPacket(buf, buf.length, group, 4446);
        waitRandom();
        socket.send(packet);
        System.out.println("Publisher:"+multicastMessage +" sender:" +name);
        socket.close();
    }

    public void waitRandom()  {
        try {
            Thread.sleep(((long)Math.random())*99 +1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
