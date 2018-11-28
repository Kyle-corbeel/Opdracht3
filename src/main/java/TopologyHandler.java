import java.io.IOException;
import java.net.*;

public class TopologyHandler extends Thread{

    final static String INET_ADDR = "224.0.0.3";
    final static int PORT = 8888;
    private MulticastSocket clientSocket;

    private NodeData data;

    private RmiHandler rmiHandler;
    private boolean running = true;

    public TopologyHandler(String nodeNameT) {

        this.rmiHandler = new RmiHandler(nodeNameT);
        NodeData data = new NodeData(nodeNameT);
        data.setMyHash(hash(nodeNameT));
        data.setPreviousNode(data.getMyHash());
        data.setNextNode(data.getMyHash());
        initReceiver();
        enterNetwork();

    }

    public void initReceiver() {
        InetAddress address = null;
        try {
            address = InetAddress.getByName(INET_ADDR);
            // Create a new Multicast socket (that will allow other sockets/programs
            // to join it as well.
            clientSocket = new MulticastSocket(PORT);
            //Join the Multicast group.
            clientSocket.joinGroup(address);
            clientSocket.setReuseAddress(true);
        }catch(Exception e){
            e.printStackTrace();
        }
    }


    public void run(){
        while(running) {
            processMultiCast(receiveMulticast());
        }

    }

    public void processMultiCast(Message m){

    }

    public void sendMulticast(String content){
        InetAddress addr = null;

        try {
            addr = InetAddress.getByName(INET_ADDR);
            DatagramSocket serverSocket = new DatagramSocket();
                String msg = content+"\tsender:"+data.getMyName();
                // Create a packet that will contain the data
                // (in the form of bytes) and send it.
                DatagramPacket msgPacket = new DatagramPacket(msg.getBytes(), msg.getBytes().length, addr, PORT);
                serverSocket.send(msgPacket);
                //System.out.println("Handler sent packet with msg: " + msg);
                Thread.sleep(500);
        } catch (Exception ex) {
            ex.printStackTrace();
            }
    }

    public Message receiveMulticast() {
        byte[] buf = new byte[256];
        try {

                // Receive the information and print it.
                DatagramPacket msgPacket = new DatagramPacket(buf, buf.length);
                clientSocket.receive(msgPacket);
                ;
                //System.out.println("Socket 1 received msg: " + msg);


        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return new Message(new String(buf, 0, buf.length));
    }



    public void newNode(String sender) //Zal opgeroepen worden wanneer de multicast een bootstrap detecteert.
    {
        int hash = hash(sender);
        if (isPrevious(hash)) { //Er wordt gecheckt of deze nieuwe node eventueel onze lagere buur is.
            data.setPreviousNode(hash);
        } else if (isNext(hash)) {//Er wordt gecheckt of deze nieuwe node eventueel onze hogerebuur is.
            sendMulticast("Bootstrap"); //Indien dit het geval is laten we dit weten aan deze node.
            data.setNextNode(hash);
        }
    }

    public void enterNetwork()//Zal opgeroepen worden wanneer we zelf in het netwerk willen toetreden.
    {
        sendMulticast("Bootstrap");//Laat het netwerk weten dat je wenst toe te treden.
        Boolean setup = false;
        while(!setup)//Blijf wachten totdat de server en eventueel andere nodes reageren
        {
            Message message = receiveMulticast();
            if ((data.getPreviousNode() == data.getMyHash()) && message.commandIs("BootServerReply")) { //Wanneer de server antwoordt kunnen we binden op de RMI
                String serverIp = message.getSenderIp();
                rmiHandler.initialise(serverIp);
                if (Integer.parseInt(message.getContent().split(" ")[1]) < 1) { //Indien er nog geen andere nodes zijn moeten we niet wachten op nodereplies
                    setup = true;
                }
            }
            if ((data.getPreviousNode() == data.getMyHash()) && message.commandIs("BootNodeReply")) {//Indien er al nodes aanwezig zijn zal de previousNode dit laten weten.
               data.setPreviousNode(hash(message.getSender()));
                data.setNextNode(Integer.parseInt(message.getContent().split(" ")[1]));
                setup = true;
            }
        }
        System.out.println("Entered network\tprevious node: " +data.getPreviousNode() +"\tnext node: " +data.getNextNode());
    }


    public static int hash(String nodeName) {
        int hash;
        hash = Math.abs(nodeName.hashCode()) % 327680;
        return hash;
    }


    public boolean isNext(int hash) {

        if(data.getMyHash() == data.getNextNode()){ //Indien dit naar zichzelf verwijst passen we zowiezo aan.
            return true;
        }else if(((data.getMyHash() < hash) || data.getNextNode() < data.getMyHash()) && (hash < data.getNextNode())){
            return true;
        }else{
            return false;
        }

    }

    public boolean isPrevious(int hash) {

        if(data.getMyHash() == data.getNextNode()){ //Indien dit naar zichzelf verwijst passen we zowiezo aan.
            return true;
        }else if((hash < data.getMyHash() || data.getPreviousNode() > data.getMyHash()) && (data.getPreviousNode() < hash)){
            data.setPreviousNode(hash);
            return true;
        }else{
            return false;
        }

    }
}

