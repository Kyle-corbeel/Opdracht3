import java.io.*;
import java.net.*;
import java.rmi.RemoteException;
import java.util.ArrayList;

public class TopologyHandler extends Thread{

    final static String INET_ADDR = "224.0.0.3";
    final static int PORT = 8888;
    private MulticastSocket clientSocket;

    private NodeData data;

    private RmiHandler rmiHandler;
    private boolean running = true;
    private boolean setup = false;

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
            fileStartup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void fileStartup() {
        replicateFiles(listFiles());

    }

    private void replicateFiles(ArrayList<String> fileNames) {
        String replicateNode="";
        boolean send = true;

        try {
            for (String fileName : fileNames) {

                String owner = rmiHandler.getOwner(fileName);
                if(owner.equals(data.getMyName())){
                    replicateNode = rmiHandler.getIpFromHash(data.getPreviousNode());
                    if(replicateNode.equals(data.getMyName())){
                        send = false;
                    }
                }else {
                    replicateNode = owner;
                }
                if(send==true) {
                    sendMulticast("Replicate " + fileName + " " + replicateNode);
                    //sendToTCP(replicateNode, fileName);
                }
            }

        }catch(RemoteException e){
            e.printStackTrace();
        }
    }

    private ArrayList<String> listFiles() {
        File folder = new File("files");
        File[] listOfFiles = folder.listFiles();
        System.out.println("Listing local files..");

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                System.out.println("Local File: " + listOfFiles[i].getName());
            } else if (listOfFiles[i].isDirectory()) {
                System.out.println("Local Directory " + listOfFiles[i].getName());
            }
        }

        ArrayList<String> fileNames = new ArrayList<String>();
        for(File f : listOfFiles){
            fileNames.add(f.getName());
        }

        return fileNames;

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
            if(mess.getContent().contains("Replicate")){
                if(mess.getContent().split(" ")[2].equals(data.getMyName())){
                    getFromTCP(mess.getContent().split(" ")[1]);
                    sendMulticast("TCPopen"+mess.getContent().split(" ")[1]+" "+mess.getSender());
                }
            }
            if(mess.getContent().contains("TCPopen")){
                sendToTCP(mess.getSender(), mess.getContent().split(" ")[1]);
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
            System.out.println(msg);
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
        setup = false;
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

    public void sendToTCP(String receiver, String fileName){
        String hostName = receiver.split(":")[0];
        int portNumber = 4444;

        try {
            Socket echoSocket = new Socket("localhost", portNumber);
            PrintWriter out = new PrintWriter(echoSocket.getOutputStream(), true);
            //BufferedReader in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
            String line = null;

            // FileReader reads text files in the default encoding.
            FileReader fileReader =
                    new FileReader(fileName);

            // Always wrap FileReader in BufferedReader.
            BufferedReader bufferedReader =
                    new BufferedReader(fileReader);

            while ((line = bufferedReader.readLine()) != null) {
                out.println(line);
                System.out.println(line);
            }
            // Always close files.
            bufferedReader.close();
            System.out.println("Done sending file..");
            /*while (in.readLine() != null) {
                System.out.println(in.readLine());
            }
            System.exit(0);*/
        } catch (Exception e) {
            e.printStackTrace();
        }

}


    public void getFromTCP(String fileName){
        int portNumber = 4444;//Integer.pars    eInt(args[0]);
        try {
            File f = new File("C:\\Users\\Wouter\\Desktop\\Testfiles\\"+fileName);
            ServerSocket serverSocket = new ServerSocket(4444/*Integer.parseInt(args[0]*/);
            Socket clientSocket = serverSocket.accept();
            //PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                BufferedWriter writer = new BufferedWriter(new FileWriter(f));
                writer.write(inputLine);
                writer.newLine();
                //System.out.println(inputLine);
                //out.println(inputLine);
                writer.close();
            }
            System.out.println("Done downloading file..");
        } catch (IOException e) {
            System.out.println("Exception caught when trying to listen on port "
                    + portNumber + " or listening for a connection");
            System.out.println(e.getMessage());
        }
    }
}


