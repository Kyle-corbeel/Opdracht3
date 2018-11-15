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
    private static String nextNode="";
    private static String previousNode="";
    static boolean running = true;

    public void main(String[] args) throws IOException, NotBoundException {
        //Startup
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Welkom bij RMI filesharing, gelieve uw naam in te geven:");
        String s = br.readLine();
        String naam = s;
        String ip="";
        String message="";
        //get IP
        try{
            final DatagramSocket socket = new DatagramSocket();
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);        //Haalt IP van host
            ip = socket.getLocalAddress().getHostAddress();
            System.out.println(ip);
        }catch(Exception e){
            e.printStackTrace();
        }
        nodeName=ip+":"+naam;




        //Start threads
        MulticastReceiver receiver = new MulticastReceiver(nodeName);
        ApplicationThread app = new ApplicationThread(nodeName);
        receiver.start();
        app.start();

        //Bootstrap
        MulticastPublisher publisher = new MulticastPublisher(nodeName);
        publisher.multicast("Bootstrap");
        RmiHandler rmiHandler = new RmiHandler(nodeName);

        //BootstrapReplyHandler
        while(running){
            if(receiver.hasMessage()){
                message = receiver.getMessage();
                if(message.contains("BootstrapReply")){
                    rmiHandler.initialise(message.split("\tsender:")[1].split(":")[0]);
                    //rearrange();
                    //hij calculate zijn positie
                    getNextNode(rmiHandler); //nextnode
                    getPreviousNode(rmiHandler); //previous node
                }
                if(message.contains("shutdown")){
                    if(previousNode.equals(message.split("\tsender:")[1].split(":")[0])){
                        previousNode=message.split("\tsender:")[0].split(":")[1];
                    }
                    if(nextNode.equals((message.split("\tsender:")[1].split(":")[0]))){
                        nextNode=message.split("\tsender:")[0].split(":")[3];
                    }
                    running=false;
                }
            }
            if((app.hasCommand())){
                String command = app.getMessage();
                if(command.equals("shutdown")){
                    shutdown(publisher);
                }
                if(command.contains("sendMessText")){
                    //gebeurt in applicationthread.
                }
                if(command.contains("getFileOwner")){
                    rmiHandler.getOwner(command.split(":")[1]);
                }
            }
        }

        //shutdown
        //Deel 6 van oefening 2 hebben we niet echt gedaan (doorsturen van prev en next node).


        /*while(cont)
        {
            System.out.println("Wat wilt u doen:");
            System.out.println("1:Get file owner");
            System.out.println("2:Send multicast");
            //System.out.println("2: Delete dude");

            System.out.println("4:Sluit af");
            s = br.readLine();
            if(s.equals("1")){
                System.out.println("Wat is de filenaam waarvan u de owner wilt weten?");
                String fName = br.readLine();
                System.out.println("De fileowner is " +theServer.getOwner(fName));
            }
            if(s.equals("2")){
                System.out.println("Geef messagetekst: ");
                String mess = br.readLine();
                publisher.multicast(mess+"\tsender:"+ip+":"+naam);
            }
            *//*if(s.equals("2")){
                System.out.println("Wie wenst u te verwijderen?");
                String dude = br.readLine();
                System.out.println("Verwijderd:" +theServer.remove(dude));
            }*//*
            if(s.equals("4")){
                cont = false;
            }

        }*/

    }

    private static void getNextNode(RmiHandler rmiboi){
        HashMap<Integer,String> ipMap = rmiboi.getMap();
        int hash;
        int closeKey = 327680;
        hash = hash(nodeName);
        for (Integer key: ipMap.keySet()){

            if(key>hash)
            {
                if(key<closeKey)
                {
                    closeKey = key;
                }
            }
        }

        if(closeKey ==327680)
        {
            closeKey = Collections.min(ipMap.keySet());
        }
        nextNode=ipMap.get(closeKey);
    }
    public static void getPreviousNode(RmiHandler rmiboi){
        HashMap<Integer,String> ipMap=rmiboi.getMap();
        int hash;
        int closeKey = 0;
        hash = hash(nodeName);
        for (Integer key: ipMap.keySet()){

            if(key<hash)
            {
                if(key>closeKey)
                {
                    closeKey = key;
                }
            }
        }

        if(closeKey ==0 )
        {
            closeKey = Collections.max(ipMap.keySet());
        }
        previousNode=ipMap.get(closeKey);
    }

    public static int hash(String nodeName) {
        int hash;
        hash = Math.abs(nodeName.hashCode()) % 327680;
        return hash;
    }

    public static void multiCastToIP(){

    }

    private void shutdown(MulticastPublisher pub) throws IOException {
        pub.multicast("shutdown:"+previousNode+":"+nextNode); //shutdown:ip(prev):nam(prev):ip(next):nam(next)  sender:ip(send):nam(send)
    }
}
