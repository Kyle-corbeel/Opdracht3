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
            clientSocket.setSoTimeout(10);
        }catch(Exception e){
            e.printStackTrace();
        }
    }


    public void run(){
        while(running) {
            processMultiCast(receiveBroadcast());
        }

    }

    public void processMultiCast(Message m){

    }

    public void sendBroadcast(String content){
        InetAddress addr = null;

        try {
            addr = InetAddress.getByName(INET_ADDR);
            DatagramSocket serverSocket = new DatagramSocket();
                String msg = content+"\tsender:";
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

    public Message receiveBroadcast() {
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
            previousNode = hash;
        } else if (isNext(hash)) {//Er wordt gecheckt of deze nieuwe node eventueel onze hogerebuur is.
            publisher.multicast("BootNodeReply " + nextNode); //Indien dit het geval is laten we dit weten aan deze node.
            nextNode = hash;
        }
    }

    public void enterNetwork()//Zal opgeroepen worden wanneer we zelf in het netwerk willen toetreden.
    {
        publisher.multicast("Bootstrap");//Laat het netwerk weten dat je wenst toe te treden.
        Boolean setup = false;
        while(!setup)//Blijf wachten totdat de server en eventueel andere nodes reageren
        {
            if ((previousNode == myHash) && message.commandIs("BootServerReply")) { //Wanneer de server antwoordt kunnen we binden op de RMI
                String serverIp = message.getSenderIp();
                rmiHandler.initialise(serverIp);

                if (Integer.parseInt(message.getContent().split(" ")[1]) < 1) { //Indien er nog geen andere nodes zijn moeten we niet wachten op nodereplies
                    setup = true;
                }
            }
            if ((previousNode == myHash) && message.commandIs("BootNodeReply")) {//Indien er al nodes aanwezig zijn zal de previousNode dit laten weten.
                previousNode = hash(message.getSender());
                nextNode = Integer.parseInt(message.getContent().split(" ")[1]);
                setup = true;
            }
        }
        System.out.println("Entered network\tprevious node: " +previousNode +"\tnext node: " +nextNode);
    }


    public static int hash(String nodeName) {
        int hash;
        hash = Math.abs(nodeName.hashCode()) % 327680;
        return hash;
    }
    public int getPreviousNode() {
        return previousNode;
    }

    public int getNextNode() {
        return nextNode;
    }

    public boolean isNext(int hash) {

        if(myHash == nextNode){ //Indien dit naar zichzelf verwijst passen we zowiezo aan.
            return true;
        }else if(((myHash < hash) || nextNode < myHash) && (hash < nextNode)){
            return true;
        }else{
            return false;
        }

    }

    public boolean isPrevious(int hash) {

        if(myHash == nextNode){ //Indien dit naar zichzelf verwijst passen we zowiezo aan.
            return true;
        }else if((hash < myHash || previousNode > myHash) && (previousNode < hash)){
            previousNode = hash;
            return true;
        }else{
            return false;
        }

    }
}

