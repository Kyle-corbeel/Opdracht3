import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.net.DatagramSocket;

public class MyClient {

    public static void main(String[] args) throws IOException, NotBoundException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Welkom bij RMI filesharing, gelieve uw naam in te geven:");
        String s = br.readLine();
        String naam = s;

        Login theServer = (Login) Naming.lookup("rmi://localhost/myserver");
        Boolean cont = true;



        try{
            final DatagramSocket socket = new DatagramSocket();
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);        //Haalt IP van host
            String ip = socket.getLocalAddress().getHostAddress();
            System.out.println(ip);
            theServer.register(ip+":"+naam);
        }catch(Exception e){
            e.printStackTrace();
        }


        while(cont)
        {
            System.out.println("Wat wilt u doen:");
            System.out.println("1:Get file owner");
            //System.out.println("2: Delete dude");

            System.out.println("4:Sluit af");
            s = br.readLine();
            if(s.equals("1")){
                System.out.println("Wat is de filenaam waarvan u de owner wilt weten?");
                String fName = br.readLine();
                System.out.println("De fileowner is " +theServer.getOwner(fName));
            }
            /*if(s.equals("2")){
                System.out.println("Wie wenst u te verwijderen?");
                String dude = br.readLine();
                System.out.println("Verwijderd:" +theServer.remove(dude));
            }*/
            /*if(s.equals("3")){
                System.out.println("Hoeveel wenst u af te nemen?");
                String amount = br.readLine();
               // System.out.println("Uw nieuwe saldo is: " +theServer.withdraw(Integer.parseInt(amount)));
            }*/
            if(s.equals("4")){
                cont = false;
            }

        }

    }
}
