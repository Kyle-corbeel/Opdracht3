import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ClientApplication extends MulticastSender{

    private String name;
    private static boolean hasMessage = false;
    protected String message = "";
    protected static  boolean cont = true;

    public ClientApplication(NodeData d){
        super(d);
    }


    public static void main(String[] args){

        NodeData data = new NodeData();
        ClientApplication app = new ClientApplication(data);
        ClientThread backgroundWorker = new ClientThread(data);
        backgroundWorker.start();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

            while (cont) {
                System.out.println("What action should be performed?");
                System.out.println("1: Shut Down\n2: Send Ping\n");
                String s = br.readLine();
                if (s.equals("1")) {
                    cont = false;
                    System.out.println("Shutting down this ClientThread..");
                    app.sendMulticast("Shut " +data.getPreviousNode() +" " +data.getNextNode());
                }if (s.equals("2")) {
                    System.out.println("What node would you like to ping");
                    s = br.readLine();
                    backgroundWorker.nodeFailure(s);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("An error has occured");

        }
    }
}