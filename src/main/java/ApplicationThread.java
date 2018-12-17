import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ApplicationThread extends Thread {

    private String name;
    public volatile boolean hasMessage=false;
    public volatile String message ="";
    protected volatile boolean cont = true;



    public ApplicationThread() {

    }

    public void initialise(){

    }

    public void run() {
        //Connection to rmi
        //Login theServer = null;

        try {
            //System.out.println("tot hier");
            //theServer = (Login) Naming.lookup("rmi://localhost/myserver");
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            //String s = br.readLine();


            //theServer.register(name);

            while (cont) {
                System.out.println("Wat wilt u doen:");
                System.out.println("1:Get file owner");
                System.out.println("2:Ping a node");

                System.out.println("4:Sluit af");
                String s = br.readLine();
                if (s.equals("1")) {
                    System.out.println("Wat is de filenaam waarvan u de owner wilt weten?");
                    String fName = br.readLine();
                    //System.out.println("De fileowner is " + theServer.getOwner(fName));
                    message="getFileOwner"+":"+fName;
                    hasMessage=true;
                }
                if(s.equals("2")){
                    System.out.println("Welke node wilt u pingen:");
                    String pingIP = br.readLine();
                    message = "sendPing:" +pingIP;
                    hasMessage = true;
                }
                if (s.equals("4")) {
                    //cont = false;
                    message="Shutdown";
                    hasMessage=true;
                   System.out.println("In de shutdownCase");
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

    public void stopThread(){
        cont = false;
    }
}
