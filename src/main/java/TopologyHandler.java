import java.io.IOException;
import java.net.*;
import java.rmi.RemoteException;

public class TopologyHandler extends Thread{



    private NodeData data;

    public static volatile RmiHandler rmiHandler;
    private volatile boolean running = true;
    public volatile boolean setup = false;

    /*TODO: Nagaan of clients nu correct hun previous & next krijgen.
      TODO: Nagaan of Shutdown al correct wordt gedaan
      TODO: Failure
     */

    ClientMulticast multi;


    public TopologyHandler() {
        this.rmiHandler = RmiHandler.getInstance();
        data = NodeData.getInstance();
        data.setMyHash(hash(data.getMyName()));
        //System.out.println(data.getMyHash()+" "+data.getMyName());
        data.setPreviousNode(data.getMyHash());
        data.setNextNode(data.getMyHash());
        multi = ClientMulticast.getInstance();

    }




    public void run() {
        enterNetwork();
        while(running){

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
                updateNetwork(mess);
            }
            //shutdown herkenning gebeurt hieronder.
            //hij moet zien of hij de previous of de next node was van degene die gaat shutten.
            if(mess.getContent().contains("Shutdown")) {
                //if (!(mess.getSender() == data.getMyName())) {//als ik niet de verstuurder ben (miss gaf dit fouten dus ik zet het er voor code compleetheid erbij)
                    if (hash(mess.getSender()) == data.getNextNode()) { //als de verstuurder MIJN volgende is dan moet ik zijn volgende krijgen!
                        data.setNextNode(hash(mess.getContent().split(" ")[2])); //zet zijn nextnode op mijn nextnode
                    }
                    if (hash(mess.getSender()) == data.getPreviousNode()) { //als de verstuurder mijn VORIGE is dan moet ik zijn vorige krijgen
                        data.setPreviousNode(hash(mess.getContent().split(" ")[1]));//zet zijn prev node op mijn prev node!
                    }
                //updateNetwork(mess.getContent().split(" ")[1] +" " +mess.getContent().split(" ")[2]);
                //}
            }
        } else
            System.out.println("message empty");
    }





    public void nodeFailure(String nodeName)
    {
        System.out.println("NodeName is : " +nodeName);
        int failHash = hash(nodeName);
        try {
            String neighbours = rmiHandler.getNeighboursFail(failHash);
            multi.sendMulticast("FailNode " +neighbours);
            Message failMess = new Message("FailNode " +neighbours +" sender:" +data.getMyName());
            System.out.println(failMess);
            updateNetwork(failMess);
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
            multi.sendMulticast("BootNodeReply " + data.getNextNode());     //Indien dit het geval is laten we dit weten aan deze node.
            data.setNextNode(hash);
            System.out.println("newNode entered if 2");
        }
    }

    public void enterNetwork()                                              //Zal opgeroepen worden wanneer we zelf in het netwerk willen toetreden.
    {
        multi.sendMulticast("Bootstrap");                                 //Laat het netwerk weten dat je wenst toe te treden.
        Message message = multi.receiveMulticast();
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

                    //System.out.println("in de if" + setup);
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
                message = multi.receiveMulticast();
            }
        }
        System.out.println("Entered network\tprevious node: " + data.getPreviousNode() + "\tnext node: " + data.getNextNode());
    }

    public void updateNetwork(Message mess) //Krijgt een set hashes door en checkt of hijzelf 1 van de 2 is
    {
        if (hash(mess.getSender()) == data.getNextNode()) { //als de verstuurder MIJN volgende is dan moet ik zijn volgende krijgen!
            data.setNextNode(hash(mess.getContent().split(" ")[2])); //zet zijn nextnode op mijn nextnode
        }
        if (hash(mess.getSender()) == data.getPreviousNode()) { //als de verstuurder mijn VORIGE is dan moet ik zijn vorige krijgen
            data.setPreviousNode(hash(mess.getContent().split(" ")[1]));//zet zijn prev node op mijn prev node!
        }

        /*if(Integer.parseInt(neighbours.split(" ")[0]) == data.getMyHash())
        {
            data.setNextNode(Integer.parseInt(neighbours.split(" ")[1])); //Zoja zet hij de andere als next
        }
        if(Integer.parseInt(neighbours.split(" ")[1]) == data.getMyHash())
        {
            data.setPreviousNode(Integer.parseInt(neighbours.split(" ")[0])); //of als previous
        }*/
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
    public void shutdownProtocol(){
        multi.sendMulticast("Shutdown "+data.getPreviousNode()+" "+data.getNextNode()); //vorm van bericht
        // SHUTDOWN previous next sender:ip ...
        System.exit(0);
    }
}

