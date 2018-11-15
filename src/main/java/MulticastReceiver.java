import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class MulticastReceiver extends Thread {

    protected MulticastSocket socket = null;
    protected byte[] buf = new byte[256];
    protected String thisNode="";
    protected boolean gotMessage = false;
    protected String received ="";

    public MulticastReceiver(String s){
        thisNode = s;
    }



    public void run() {
        try {
            socket = new MulticastSocket(4446);
            InetAddress group = InetAddress.getByName("230.0.0.0");
            socket.joinGroup(group);
            String sender="";
            while (true) {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                received = new String(packet.getData(), 0, packet.getLength());

                if(received.length()>1 && received.contains("\tsender:")){
                    sender=received.split("\tsender:")[1];
                    if(!sender.equals(thisNode)) {
                        gotMessage = true;
                        System.out.println(received);
                        if (received.equals("end")) {
                            break;
                        }
                    }
                }

            }
            socket.leaveGroup(group);
            socket.close();
        }catch(IOException i) {
            i.printStackTrace();
        }

    }

    public boolean hasMessage(){
        return gotMessage;
    }

    public String getMessage(){
        gotMessage = false;
        return received;
    }
}