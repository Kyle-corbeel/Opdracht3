import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;

/**
 * This class is a unit between the server and the client.
 */
public class RmiHandler {


    boolean initialised = false;
    Login theServer = null;
    HashMap<Integer,String> ipMap;
    public static final RmiHandler instance = new RmiHandler();



    private RmiHandler() {

    }

    public static RmiHandler getInstance(){
        return instance;
    }



    public void rmiStartup(NodeData data) {
        if(!initialised){
        try {
            //theServer = (Login) Naming.lookup("rmi://"+data.getServerIP()+"/myserver");
            Registry r = LocateRegistry.getRegistry(data.getServerIP());
            theServer = (Login) r.lookup("Login");
            initialised = true;
        } catch (NotBoundException e) {
            e.printStackTrace();
            //} catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        }

    }


    public String getOwner(String fileName) throws RemoteException {
        return theServer.getOwner(fileName);
    }

    public String getIDFromHash(int hash)throws RemoteException{
        return theServer.getIDFromHash(hash);
    }

    public String getNeighboursFail(String nodeID) throws RemoteException {
        return theServer.getNeighboursFail(nodeID);
    }
}