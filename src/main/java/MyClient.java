import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.net.DatagramSocket;

public class MyClient {

    public static void main(String[] args) throws IOException, NotBoundException {
        //Startup
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Welkom bij RMI filesharing, gelieve uw naam in te geven:");
        String s = br.readLine();
        String naam = s;
        String ip="";
        //get IP
        try{
            final DatagramSocket socket = new DatagramSocket();
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);        //Haalt IP van host
            ip = socket.getLocalAddress().getHostAddress();
            System.out.println(ip);
        }catch(Exception e){
            e.printStackTrace();
        }




        //Start threads
        MulticastReceiver receiver = new MulticastReceiver(ip+":"+naam);
        ApplicationThread app = new ApplicationThread(ip + ":" +naam);
        receiver.start();
        app.start();


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

    public static void multiCastToIP(){

    }
}
