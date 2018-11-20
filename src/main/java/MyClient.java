import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.net.DatagramSocket;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collections;
import java.util.HashMap;

public class MyClient {
    private static String nodeName="";
    private static int nextNode=327680;
    private static int previousNode=0;
    private static boolean running = true;
    private static int myHash=0;
    private static String myHashString="";

    public void main(String[] args) throws IOException, NotBoundException {
        //Startup
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Welkom bij RMI filesharing, gelieve uw naam in te geven:");
        String naam = br.readLine();
        String ip = "";


        //get IP
        try {
            final DatagramSocket socket = new DatagramSocket();
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);        //Haalt IP van host
            ip = socket.getLocalAddress().getHostAddress();
            System.out.println(ip);
        } catch (Exception e) {
            e.printStackTrace();
        }
        nodeName = ip + ":" + naam;           //Voegt IP en gekozen naam samen tot Bv, 143.169.252.202:Wouter
        myHash = hash(nodeName);
        myHashString = Integer.toString(myHash);



        //Start threads
        MulticastReceiver receiver = new MulticastReceiver(nodeName);
        ApplicationThread app = new ApplicationThread(nodeName);
        receiver.start();
        app.start();

        //Bootstrap
        MulticastPublisher publisher = new MulticastPublisher(nodeName);
        RmiHandler rmiHandler = new RmiHandler(nodeName);

        publisher.multicast("Bootstrap");


        //BootstrapReplyHandler
        while (running) {
            try {
                if (receiver.hasMessage()) {
                    Message message = receiver.getMessage();

                    if (message.contains("Bootstrap")) {
                        int hash = hash(message.getSender());
                        if(isPrevious(hash)){
                            previousNode = hash;
                        }else if(isNext(hash)){
                            publisher.multicast("BootstrapReply "+Integer.toString(nextNode));
                            nextNode = hash;            //Respond to new node that i'm his previous, and that next is now his next
                        }
                    }
                    if ((!rmiHandler.hasServer()) && message.contains("BootstrapReply") && message.getSenderName().equals("NameServer)")) {
                        String serverIp = message.getSenderIp();
                        rmiHandler.initialise(serverIp);
                        if(Integer.parseInt(message.getContent().split(" ")[1]) < 1){
                            nextNode=myHash;
                            previousNode=myHash;
                        }
                    }
                    if(message.contains("BootstrapReply") && !(message.getSenderName()).equals("NameServer)")){
                        previousNode = hash(message.getSender());
                        nextNode = Integer.parseInt(message.getContent().split(" ")[1]);
                    }
                    if(message.contains("Shut") && message.contains(myHashString)){
                        if(message.getContent().split(" ")[1].equals(myHashString)){           //Ik ben previous node van de shutdowner
                            nextNode = Integer.parseInt(message.getContent().split(" ")[2]);
                        }else{                                                                       //Ik ben de next node van de shutdowner
                            previousNode = Integer.parseInt(message.getContent().split(" ")[1]);
                        }

                    }
                }


                if (app.hasCommand()) {
                    String command = app.getCommand();
                    if (command.equals("shutdown")) {
                        publisher.multicast("Shut "+previousNode+" "+nextNode);
                        running=false;
                    }
                }
            } catch (Exception e) {
                //FAILURE
                //hier komt de multicast
                publisher.multicast("Failed"); //FAILURE 1)
            }
        }

        //shutdown
        //Deel 6 van oefening 2 hebben we niet echt gedaan (doorsturen van prev en next node).

    }

    private boolean isNext(int hash) {

        if((myHash < hash) && (hash < nextNode)){
            return true;
        }else{
            return false;
        }

    }

    private boolean isPrevious(int hash) {

        if((hash < myHash) && (previousNode < hash)){
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

    private void shutdown(MulticastPublisher publisher) throws IOException {
        publisher.multicast("shutdown:"+previousNode+":"+nextNode); //shutdown:ip(prev):nam(prev):ip(next):nam(next)  sender:ip(send):nam(send)
    }
}
