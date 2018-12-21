import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class ClientApplication extends MulticastSender implements RMINode{

    private String name;
    private static boolean hasMessage = false;
    protected String message = "";
    protected static  boolean cont = true;
    private RMINode rmiNode=null;

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
                System.out.println("1: Shut Down\n");
                String s = br.readLine();
                if (s.equals("1")) {
                    cont = false;
                    System.out.println("Shutting down this ClientThread..");
                    app.sendMulticast("ShutRequest");
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("An error has occured");

        }
    }
    private void RMInodeInit()
    {
        try {
            rmiNode = (RMINode) Naming.lookup("rmi://"+data.getNextNode()+"/mynextnode");
        } catch (NotBoundException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void RMIrequest() {
        Agent agent= new Agent();
        agent.run();
        rmiNode.RMIrequest();
    }
}