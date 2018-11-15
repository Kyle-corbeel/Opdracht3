import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class ApplicationThread extends Thread {

    private String name;

    public ApplicationThread(String nodeName) {
        this.name = nodeName;

    }

    public void run() {
        //Connection to rmi
        Login theServer = null;
        try {
            theServer = (Login) Naming.lookup("rmi://localhost/myserver");
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String s = br.readLine();
            MulticastPublisher publisher = new MulticastPublisher();

            Boolean cont = true;
            theServer.register(name);
            while (cont) {
                System.out.println("Wat wilt u doen:");
                System.out.println("1:Get file owner");
                System.out.println("2:Send multicast");
                //System.out.println("2: Delete dude");

                System.out.println("4:Sluit af");
                s = br.readLine();
                if (s.equals("1")) {
                    System.out.println("Wat is de filenaam waarvan u de owner wilt weten?");
                    String fName = br.readLine();
                    System.out.println("De fileowner is " + theServer.getOwner(fName));
                }
                if (s.equals("2")) {
                    System.out.println("Geef messagetekst: ");
                    String mess = br.readLine();
                    publisher.multicast(mess + "\tsender:" +name);
                }
            /*if(s.equals("2")){
                System.out.println("Wie wenst u te verwijderen?");
                String dude = br.readLine();
                System.out.println("Verwijderd:" +theServer.remove(dude));
            }*/
                if (s.equals("4")) {
                    cont = false;
                }

            }
        } catch (Exception e) {
            e.printStackTrace();

        }
    }
}
