import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.DatagramSocket;
import java.net.UnknownHostException;
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
            //System.out.println("In de shutdownIF"+app.hasCommand());
            if (app.hasCommand()) {
                String appCommand = app.getCommand();
                System.out.println("hascommand");
                if (appCommand.equals("Shutdown")) {
                    System.out.println("Shutdown in client loop");
                    app.stopThread();
                    topo.shutdownProtocol(); //stuurt een multicast van shutdown
                    running = false;
                }
                if (appCommand.contains("getFileOwner")) {
                    System.out.println("getFileOwner loop");
                    System.out.println(TopologyHandler.rmiHandler.getOwner(appCommand.split(":")[1]));

                }
                if (appCommand.contains("sendPing")) {
                    if(!sendPing(appCommand.split(":")[1]))
                    {
                        topo.nodeFailure(appCommand.split(":")[1] +":" +appCommand.split(":")[2]);
                    }

                }
            }
        }
    }

    public static boolean sendPing(String ipAddress)
            throws UnknownHostException, IOException
    {
        InetAddress IP = InetAddress.getByName(ipAddress);
        System.out.println("Ping to: " + ipAddress);
        if (IP.isReachable(5000)) {
            System.out.println("Ping successful");
            return (true);
        }
        else{
            System.out.println("Ping failed");
            return(false);
        }
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
