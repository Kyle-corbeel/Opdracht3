import java.net.*;

public class MulticastReceiver {

    final static String INET_ADDR = "224.0.0.251";
    final static int PORT = 4567;
    private MulticastSocket clientSocket;
    private NodeData data;
    InetAddress address = null;


    public MulticastReceiver(NodeData data){
        this.data = data;

        try {
            address = InetAddress.getByName(INET_ADDR); // Create a new Multicast socket (that will allow other sockets/programs
            clientSocket = new MulticastSocket(PORT);   // to join it as well.
            clientSocket.setNetworkInterface(NetworkInterface.getByInetAddress(InetAddress.getByName(data.generateIP())));
            clientSocket.joinGroup(address);            //Join the Multicast group.

            clientSocket.setReuseAddress(true);
            //clientSocket.setSoTimeout(5000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Message receiveMulticast() {
        byte[] buf = new byte[256];

        try {
            //clientSocket.setSoTimeout(1000);
            DatagramPacket msgPacket = new DatagramPacket(buf, buf.length);
            clientSocket.receive(msgPacket);

        } catch (Exception ex) {
            return new Message(null);
        }
        Message mess = new Message(new String(buf, 0, buf.length));
        //System.out.println("voor de if " +mess.getSenderID() + " " +data.getNodeID());


        if(mess.getSenderID().equals(data.getNodeID())) {
            System.out.println("Eigen message " +mess.getCommand());
            if(mess.getCommand().equals("Shut") || mess.getCommand().equals("FileChange")){               //Doesnt send own message through, except for shutdown
                System.out.println("in den andere if");
                return mess;
            }else {
                return null;
            }
        }


        return mess;         //steek in buffer
    }


}
