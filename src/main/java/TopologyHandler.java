import java.io.IOException;
import java.net.*;
import java.rmi.RemoteException;

public class TopologyHandler extends Thread{

    final static String INET_ADDR = "224.0.0.3";
    final static int PORT = 8888;
    private MulticastSocket clientSocket;

    private NodeData data;

    private RmiHandler rmiHandler;
    private boolean running = true;

    /*TODO: Nagaan of clients nu correct hun previous & next krijgen.
      TODO: Nagaan of Shutdown al correct wordt gedaan
      TODO: Failure
     */


    public TopologyHandler(String nodeNameT) {
        this.rmiHandler = new RmiHandler(nodeNameT);
        data = new NodeData(nodeNameT);
        data.setMyHash(hash(data.getMyName()));
        //System.out.println(data.getMyHash()+" "+data.getMyName());
        data.setPreviousNode(data.getMyHash());
        data.setNextNode(data.getMyHash());
        initReceiver();
    }

    public void initReceiver() {
        InetAddress address = null;
        try {
            address = InetAddress.getByName(INET_ADDR); // Create a new Multicast socket (that will allow other sockets/programs
            clientSocket = new MulticastSocket(PORT);   // to join it as well.
            //Join the Multicast group.
            clientSocket.joinGroup(address);
            clientSocket.setReuseAddress(true);
            //clientSocket.setSoTimeout(5000);
            enterNetwork();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void run() {
        while (running) {
            //System.out.println("voor");
            processMultiCast(receiveMulticast());
            //System.out.println("na");
        }
    }

    public void processMultiCast(Message mess) {
        //System.out.println(mess.getContent().contains("BootServerReply"));
        if (!mess.isEmpty()) {
            if (mess.getContent().contains("Bootstrap")) {
                System.out.println(mess.getSender());
                newNode(mess.getSender());

                // System.out.println("if entered");
            }
            if(mess.getContent().contains("FailNode"))
            {
                updateNetwork(mess.getContent().split(" ")[1] +" " +mess.getContent().split(" ")[2]);
            }
        } else
            System.out.println("message empty");
    }

    public void sendMulticast(String content) {
        // InetAddress addr = null;

        try {
            InetAddress addr = InetAddress.getByName(INET_ADDR);
            DatagramSocket serverSocket = new DatagramSocket();                     // Create a packet that will contain the data
            String msg = content + "\tsender:" + data.getMyName() + "#";            // (in the form of bytes) and send it.
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

        return mess;         //steek in buffer
    }

    public void nodeFailure(String nodeName)
    {
        int failHash = hash(nodeName);
        try {
            String neighbours = rmiHandler.getNeighbours(failHash);
            sendMulticast("FailNode " +neighbours);
            updateNetwork(neighbours);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public void newNode(String sender)                                       //Zal opgeroepen worden wanneer de multicast een bootstrap detecteert.
    {
        int hash = hash(sender);
        // System.out.println("newNode entered");
        if (isPrevious(hash)) {                                             //Er wordt gecheckt of deze nieuwe node eventueel onze lagere buur is.
            data.setPreviousNode(hash);
            //System.out.println("newNode entered if 1");
        }
        if (isNext(hash)) {                                                 //Er wordt gecheckt of deze nieuwe node eventueel onze hogerebuur is.
            sendMulticast("BootNodeReply " + data.getNextNode());     //Indien dit het geval is laten we dit weten aan deze node.
            data.setNextNode(hash);
            System.out.println("newNode entered if 2");
        }
    }

    public void enterNetwork()                                              //Zal opgeroepen worden wanneer we zelf in het netwerk willen toetreden.
    {
        sendMulticast("Bootstrap");                                 //Laat het netwerk weten dat je wenst toe te treden.
        Message message = receiveMulticast();
        Boolean setup = false;
        while (!setup)                                                       //Blijf wachten totdat de server en eventueel andere nodes reageren
        {
            System.out.println(message);
            if ((data.getPreviousNode() == data.getMyHash()) && message.commandIs("BootServerReply")) { //Wanneer de server antwoordt kunnen we binden op de RMI
                String serverIp = message.getSenderIp();
                //System.out.println(serverIp);
                rmiHandler.initialise(serverIp);
                System.out.println(message.getNodeCount());                                                //drukt de reply van de server af!!!
                if (message.getNodeCount() < 1) {                                                          //Indien er nog geen andere nodes zijn moeten we niet wachten op nodereplies
                    setup = true;

                    System.out.println("in de if" + setup);
                }
            }
            if ((data.getPreviousNode() == data.getMyHash()) && message.commandIs("BootNodeReply")) {   //Indien er al nodes aanwezig zijn zal de previousNode dit laten weten.
                data.setPreviousNode(hash(message.getSender()));
                System.out.println(message.getSender() + " " + hash(message.getSender()));
                data.setNextNode(Integer.parseInt(message.getContent().split(" ")[1].split("\t")[0]));
                //System.out.println(message.getContent().split(" ")[1].split("\t")[0]);
                setup = true;
            }
            if (!setup) {
                message = receiveMulticast();
            }
        }
        System.out.println("Entered network\tprevious node: " + data.getPreviousNode() + "\tnext node: " + data.getNextNode());
    }

    public void updateNetwork(String neighbours) //Krijgt een set hashes door en checkt of hijzelf 1 van de 2 is
    {
        if(Integer.parseInt(neighbours.split(" ")[0]) == data.getMyHash())
        {
            data.setNextNode(Integer.parseInt(neighbours.split(" ")[1])); //Zoja zet hij de andere als next
        }
        if(Integer.parseInt(neighbours.split(" ")[1]) == data.getMyHash())
        {
            data.setPreviousNode(Integer.parseInt(neighbours.split(" ")[0])); //of als previous
        }
    }


    public static int hash(String nodeName) {
        int hash;
        hash = Math.abs(nodeName.hashCode()) % 327680;
        return hash;
    }


    public boolean isNext(int hash) {

        if(data.getMyHash() == data.getNextNode()){                 //Indien dit naar zichzelf verwijst passen we zowiezo aan.
            return true;
        }else if(((data.getMyHash() < hash) || data.getNextNode() < data.getMyHash()) && (hash < data.getNextNode())){
            return true;
        }else{
            return false;
        }

    }

    public boolean isPrevious(int hash) {

        if(data.getMyHash() == data.getNextNode()){                 //Indien dit naar zichzelf verwijst passen we zowiezo aan.
            return true;
        }else if((hash < data.getMyHash() || data.getPreviousNode() > data.getMyHash()) && (data.getPreviousNode() < hash)){
            data.setPreviousNode(hash);
            return true;
        }else{
            return false;
        }

    }
}

