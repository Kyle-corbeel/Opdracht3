import java.net.*;

public class ClientMulticast {

    final static String INET_ADDR = "224.0.0.251"; //Specifiek voor de PI's
    final static int PORT = 4567;
    public static String nodeName, ip;
    private MulticastSocket clientSocket;
    public static ClientMulticast instance;

    private ClientMulticast(){
    }

    public static ClientMulticast getInstance(){

        return instance;
    }



    public void initReceiver(String temp, String tempip) {
        nodeName = temp;
        ip = tempip;
        InetAddress address = null;
        try {
            address = InetAddress.getByName(INET_ADDR); // Create a new Multicast socket (that will allow other sockets/programs
            clientSocket = new MulticastSocket(PORT);   // to join it as well.
            //Join the Multicast group.
            clientSocket.setNetworkInterface(NetworkInterface.getByInetAddress(InetAddress.getByName(ip)));
            clientSocket.joinGroup(address);
            clientSocket.setReuseAddress(true);
            //clientSocket.setSoTimeout(5000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMulticast(String content) {
        // InetAddress addr = null;

        try {
            InetAddress addr = InetAddress.getByName(INET_ADDR);
            DatagramSocket serverSocket = new DatagramSocket();                     // Create a packet that will contain the data
            String msg = content + "\tsender:" + nodeName + "#";            // (in the form of bytes) and send it.
            DatagramPacket msgPacket = new DatagramPacket(msg.getBytes(), msg.getBytes().length, addr, PORT);
            serverSocket.send(msgPacket);
            System.out.println("Handler sent packet with msg: " + msg);
            Thread.sleep(500);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public Message receiveMulticast() {
        byte[] buf = new byte[256];
        try {
            InetAddress address = InetAddress.getByName(INET_ADDR);
            /*clientSocket = new MulticastSocket(PORT); //Join the Multicast group.
            clientSocket.joinGroup(address);
            clientSocket.setReuseAddress(true);
            clientSocket.setSoTimeout(1000);.*/
            DatagramPacket msgPacket = new DatagramPacket(buf, buf.length);
            clientSocket.receive(msgPacket);

            //System.out.println("Socket 1 received msg: " + msgPacket);

        } catch (Exception ex) {
            //ex.printStackTrace();
            System.out.println("left loop");
            return new Message(null);
        }
        Message mess = new Message(new String(buf, 0, buf.length));
        //System.out.println(mess);
        if(mess.getSender().equals(ip+":"+nodeName)){
            return null;
        }else {
            return mess;         //steek in buffer
        }
    }



}
