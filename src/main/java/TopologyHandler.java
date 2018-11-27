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

    public void newNode(String sender)
    {
        int hash = hash(sender);
        if (isPrevious(hash)) {
            previousNode = hash;
        } else if (isNext(hash)) {
            publisher.multicast("BootNodeReply " + nextNode);
            nextNode = hash;
        }//Respond to new node that i'm his previous, and that next is now his next
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

        if(myHash == nextNode){
            return true;
        }else if(((myHash < hash) || nextNode < myHash) && (hash < nextNode)){
            return true;
        }else{
            return false;
        }

    }

    public boolean isPrevious(int hash) {

        if(myHash == nextNode){
            return true;
        }else if((hash < myHash || previousNode > myHash) && (previousNode < hash)){
            previousNode = hash;
            return true;
        }else{
            return false;
        }

    }
}

