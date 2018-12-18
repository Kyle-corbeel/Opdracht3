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
        //[START]:Startup
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Welkom bij RMI filesharing, gelieve uw naam in te geven:");
        String naam = br.readLine();
        //[END]:Startup

        //[Start]:Get instances of the classes (using singleton)
        NodeData data = NodeData.getInstance();
        data.initNodeData(naam);

        ClientMulticast multi = ClientMulticast.getInstance();    //Maakt singleton aan van multicastclass
        multi.initReceiver(naam, data.getIp());                                                       //Initialiseert receiver

        TopologyHandler topo = new TopologyHandler();          //Voegt IP en gekozen naam samen tot Bv, 143.169.252.202:Wouter
        topo.start();

        ApplicationThread app = new ApplicationThread();
        app.start();
        //[END]: Getting instances complete.


        while(!topo.setup){

        }

        //[Start]: Loop in which our client will keep existing
        while (running) {

            Message mess = multi.receiveMulticast();
            topo.processMultiCast(mess);

            if (app.hasCommand()) { //The applicattionThread as got a command from user input.
                String appCommand = app.getCommand();
                System.out.println("hascommand");

                //[Start]: Command 1: Shutdown
                if (appCommand.equals("Shutdown")) { //IF the command is to shutdown the node we will shut all the threads.
                    System.out.println("Shutdown in client loop");
                    /*TODO:Shut de instances?*/
                    app.stopThread();
                    topo.shutdownProtocol(); //stuurt een multicast van shutdown
                    running = false; //Put running on false so the client stops existing
                }
                //[End]: If everything goes right, the client will stop existing and node will shut down.

                //[Start]: Command 2: get owner of file.
                if (appCommand.contains("getFileOwner")) {
                    System.out.println("getFileOwner loop");
                    System.out.println(TopologyHandler.rmiHandler.getOwner(appCommand.split(":")[1]));

                }
                //[End]: Will get owner of file

                //[Start]: Will ping a certain node and handle failure.
                if (appCommand.contains("sendPing")) {
                    if(!sendPing(appCommand.split(":")[1]))
                    {
                        topo.nodeFailure(appCommand.split(":")[1] +":" +appCommand.split(":")[2]);
                    }

                }
                //[End]: If it goes trough -> no problem, if not-> nodeFailureProtocol.
            }
        }
        //[End]:Client stops existing
    }


    //[Start]: Method that will send a ping to a user inputted node.
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
    //[End]: method

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
