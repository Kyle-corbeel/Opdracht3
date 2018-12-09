import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.DatagramSocket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class MyClient {

    private static boolean running = true;


    public static void main(String[] args) throws IOException {
        //Startup
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Welkom bij RMI filesharing, gelieve uw naam in te geven:");
        String naam = br.readLine();


        TopologyHandler topo = new TopologyHandler(naam);          //Voegt IP en gekozen naam samen tot Bv, 143.169.252.202:Wouter
        topo.start();

        //Bootstrap
        //RmiHandler rmiHandler = new RmiHandler(nodeName);

        //Start threads
        ApplicationThread app = new ApplicationThread();
        app.start();

        //BootstrapReplyHandler
        while (running) {
            if(app.hasCommand()){
                if(app.getCommand().equals("shutdown")){
                    app.stopThread();
                    topo.shutdownProtocol(); //stuurt een multicast van shutdown
                    running=false;
                }
            }
            /*

            Message message = receiver.check();
            if(message != null) {
                System.out.println("Client:" + message);

                if (message.commandIs("Bootstrap")) {
                    System.out.println("tester");
                    int hash = hash(message.getSender());
                    if (isPrevious(hash)) {
                        previousNode = hash;
                    } else if (isNext(hash)) {
                        publisher.multicast("BootNodeReply " + nextNode);
                        nextNode = hash;            //Respond to new node that i'm his previous, and that next is now his next
                    }
                    printPreviousAndNext();
                }
                if (!hasNeighbours && message.commandIs("BootServerReply")) {
                    String serverIp = message.getSenderIp();
                    rmiHandler.initialise(serverIp);

                    if (Integer.parseInt(message.getContent().split(" ")[1]) < 1) {
                        nextNode = myHash;
                        previousNode = myHash;
                        printPreviousAndNext();
                        hasNeighbours = true;
                    }

                }
                if (!hasNeighbours && message.commandIs("BootNodeReply")) {
                    previousNode = hash(message.getSender());
                    nextNode = Integer.parseInt(message.getContent().split(" ")[1]);
                    printPreviousAndNext();
                    hasNeighbours = true;
                }
                if (message.commandIs("Shut") && message.commandIs(myHashString)) {
                    if (message.getContent().split(" ")[1].equals(myHashString)) {           //Ik ben previous node van de shutdowner
                        nextNode = Integer.parseInt(message.getContent().split(" ")[2]);
                    } else {                                                                       //Ik ben de next node van de shutdowner
                        previousNode = Integer.parseInt(message.getContent().split(" ")[1]);
                    }

                }
            }



            if (app.hasCommand()) {
                String command = app.getCommand();
                if (command.equals("shutdown")) {
                    publisher.multicast("Shut " + previousNode + " " + nextNode);
                    app.stopThread();
                    running = false;
                }
            }
        } catch (Exception e) {
            //FAILURE
            //hier komt de multicast
            e.printStackTrace();
            publisher.multicast("Failed"); //FAILURE 1)
            break;
        }*/


        }

    /*
    private static boolean isNext(int hash) {

        if(myHash == nextNode){
            return true;
        }else if(((myHash < hash) || nextNode < myHash) && (hash < nextNode)){
            return true;
        }else{
            return false;
        }

    }

    private static boolean isPrevious(int hash) {

        if(myHash == nextNode){
            return true;
        }else if((hash < myHash || nextNode < myHash) && (previousNode < hash)){
            previousNode = hash;
            return true;
        }else{
            return false;
        }

    }

    public static int hash(String nodeName) {
        int hash;
        hash = Math.abs(nodeName.hashCode()) % 327680;
        return hash;
    }

    private static void shutdown(MulticastPublisher publisher) throws IOException {
        publisher.multicast("shutdown:"+previousNode+":"+nextNode); //shutdown:ip(prev):nam(prev):ip(next):nam(next)  sender:ip(send):nam(send)
    }

    public static String getPrevious(){
        return "Previous: "+previousNode;
    }

    public static String getNext(){
        return "Next: "+nextNode;
    }

    public static void printPreviousAndNext(){
        System.out.println(getPrevious()+"\t"+getNext());
    }*/
    }
}
