import com.sun.security.ntlm.Client;

import java.net.*;


/** Basic information:
  *This class was created to handle the sending and recieiving of the multicasts. (on client side)
  * */

public class ClientMulticast {

    final static String INET_ADDR = "224.0.0.251"; //Specifiek voor de PI's
    final static int PORT = 4567;
    public static String nodeName, ip;
    private MulticastSocket clientSocket;
    private static final ClientMulticast instance = new ClientMulticast(); //Final zorgt ervoor dat er maar 1 enkele instance is

    private ClientMulticast(){
    }

    public static ClientMulticast getInstance(){

        return instance;
    }


    //[Start]: Initialising the multicaster
    public void initReceiver(String temp, String tempip) {
        try {
            final DatagramSocket socket = new DatagramSocket();                 //Haalt IP van host
            ip = InetAddress.getLocalHost().toString().split("/")[1];
            //System.out.println(ip);
        } catch (Exception e) {
            e.printStackTrace();
        }
        nodeName = ip + ":" + temp;


        /*
        nodeName = temp;
        ip = tempip;
        nodeName = ip+":"+nodeName;
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
        }*/
    }
    //[End]: done initilialising



    //[Start]: Sending a multicast method
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
    //[End]: Method of sending multicast

    //[Start]: Recieving of multicast method
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
            //System.out.println("Null Message");
            return new Message(null);
        }
        Message mess = new Message(new String(buf, 0, buf.length));
        //System.out.println(mess);
        if(mess.getSender().equals(nodeName)){
            return null;
        }else {
            return mess;         //steek in buffer
        }
    }
    //[End]: Reciecing of multicast method



}
