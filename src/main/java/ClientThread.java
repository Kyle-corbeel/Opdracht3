import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ClientThread extends Thread{

    private volatile boolean running = true;
    private Message mess;
    private NodeData data;
    private NetworkHandler net;
    private MulticastReceiver multi;
    MulticastSender multis;
    boolean initialised = false;
    Login theServer = null;
    RmiHandler rmi = RmiHandler.getInstance();



    public ClientThread(NodeData data) {        //Works as a background thread, processing multicasts
        this.data = data;
    }

    public void run(){
        net = new NetworkHandler(data);
        multi = new MulticastReceiver(data);
        multis = new MulticastSender(data);

        networkSetup();
        rmi.rmiStartup(data);
        FileHandler file = new FileHandler(data);

        while(running){

            mess=null;

            do{
                mess=multi.receiveMulticast();
            }while(mess==null);

            System.out.println("Received a message: "+mess);
            running = net.processMulticast(mess);
            file.processMulticast(mess);
        }
    }

    public void networkSetup(){
        net.sendBootstrap();


        while(running && !net.isSetup()){

            mess=null;

            do{
                mess=multi.receiveMulticast();
            }while(mess==null);

            System.out.println("Received a message: "+mess);
            running = net.processMulticast(mess);
        }
    }

    public void toggleRunning(){
        if(running){
            running=false;
        }else{
            running=true;
        }
    }

    public void nodeFailure(String nodeID)
    {
        try {
            String neighboursFail = rmi.getNeighboursFail(nodeID);
            multis.sendMulticast("Shut " +neighboursFail);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

}
