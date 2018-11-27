import java.io.IOException;

public class TopologyHandler {

    private String nodeName;
    private int previousNode;
    private int nextNode;
    private int myHash;

    private MulticastPublisher publisher;
    private RmiHandler rmiHandler;
    private MulticastReceiver receiver;

    public TopologyHandler(String nodeNameT) throws Exception {
        this.publisher = new MulticastPublisher(nodeName);
        this.rmiHandler = new RmiHandler(nodeName);
        this.receiver = new MulticastReceiver(nodeName);
        nodeName = nodeNameT;
        myHash = hash(nodeName);
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

