import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ApplicationThread extends Thread {

    private String name;
    private static boolean hasMessage=false;
    protected String message ="";



    public ApplicationThread(String nodeName) {
        this.name = nodeName;

    }

    public void initialise(){

    }

    public void run() {
        //Connection to rmi
        Login theServer = null;
        try {
            //System.out.println("tot hier");
            //theServer = (Login) Naming.lookup("rmi://localhost/myserver");
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            //String s = br.readLine();
            MulticastPublisher publisher = new MulticastPublisher(name);

            Boolean cont = true;
            //theServer.register(name);

            while (cont) {
                System.out.println("Wat wilt u doen:");
                System.out.println("1:Get file owner");
                System.out.println("2:Send multicast");

                System.out.println("4:Sluit af");
                String s = br.readLine();
                if (s.equals("1")) {
                    hasMessage=true;
                    System.out.println("Wat is de filenaam waarvan u de owner wilt weten?");
                    String fName = br.readLine();
                    //System.out.println("De fileowner is " + theServer.getOwner(fName));
                    message="getFileOwner"+":"+fName;
                }
                if (s.equals("2")) {
                    hasMessage=true;
                    System.out.println("Geef messagetekst: ");
                    String mess = br.readLine();
                    publisher.multicast(mess);
                    message="sendMessText"+":"+mess;
                }
                if (s.equals("4")) {
                    hasMessage=true;
                    cont = false;
                    message="shutdown";
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("An error has occured");

        }
    }

    public boolean hasCommand(){
        return hasMessage;
    }

    public String getCommand(){
        hasMessage = false;
        return message;
    }
}
