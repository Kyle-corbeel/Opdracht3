import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collections;

public class Server implements Login{

    private static boolean running = true;
    private static Message mess;
    private static ServerMapHandler map;

    public static void main(String[] args){

        NodeData data = new NodeData(true);
        map = new ServerMapHandler(data);
        MulticastReceiver multi = new MulticastReceiver(data);

        rmiStartup();

        while(running){

            mess=null;

            do{
                mess=multi.receiveMulticast();
            }while(mess==null);

            System.out.println("Received a message: "+mess);

            map.processMulticast(mess);


        }
    }

    public static void rmiStartup(){
        Server obj = new Server();
        try{
        Login stub = (Login) UnicastRemoteObject.exportObject(obj, 0);
        Registry r = null;
        r = LocateRegistry.createRegistry(1099);
        r.bind("myserver", stub);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (AlreadyBoundException e) {
            e.printStackTrace();
        }

    }

    public String getOwner(String fileName) throws RemoteException {
        return map.getOwner(fileName);
    }

    public String getIDFromHash(int hash){
        return map.getIDFromHash(hash);
    }

    @Override
    public String getNeighboursFail(String nodeID) throws RemoteException {
        return map.getNeighboursFail(nodeID);
    }
}
