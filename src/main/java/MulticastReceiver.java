import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.concurrent.BlockingQueue;

public class MulticastReceiver{

    protected MulticastSocket socket = null;
    protected byte[] buf = new byte[256];
    protected String thisNode="";
    protected boolean gotMessage = false;
    protected String received ="";
    protected Message m;
    private InetAddress group = InetAddress.getByName("230.0.0.0");

    public MulticastReceiver(String nodeName) throws IOException {
        thisNode = nodeName;
        socket = new MulticastSocket(4446);
        socket.joinGroup(group);
    }



    public Message check() {
        m = new Message();
        String sender="";
        try {

            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            socket.receive(packet);
            received = new String(packet.getData(), 0, packet.getLength());         //Krijgt multicast bericht binnen
            if(received.length()>1 && received.contains("sender:")){
                m = new Message(received);
                if(!m.getSender().equals(thisNode)) {       //bericht enkel ontvangen indien deze node niet de zender is
                    gotMessage = true;
                    System.out.println("Receiver:"+m);
                    return(m);
                }
            }
/*            socket.leaveGroup(group);
            socket.close();*/
            return(null);
        }catch(IOException i) {
            i.printStackTrace();
            return(null);
        }

    }

    public Message getMessage(){
        gotMessage = false;
        return m;
    }
}