import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.HashMap;

//deze class dient als tussenpersoon tussen de server en de client.
//Hij zal verschillende elementen ophalen van de server en bijhouden zodat de client deze kan opvragen.

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



    public void initialise(String serverIp){

        try {
            theServer = (Login) Naming.lookup("rmi://"+serverIp+"/myserver");
            System.out.println(theServer.getOwner("test"));
            /** TODO
             *  PAS DE LOCALHOST HIERBOVE AAN !!
             */
            //Later localhost vervangen door serverIP
            //BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            //String s = br.readLine();

            //ipMap = theServer.register(nodeName);
            initialised=true;
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public HashMap<Integer,String> getTopology(){
        try {
            ipMap = theServer.getTopology();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        for (Integer ipName: ipMap.keySet()){
        System.out.println(ipName);
    }
        return ipMap;

}


    public String getOwner(String str) throws RemoteException {
        return theServer.getOwner(str);
    }

    public String getNeighboursFail(int nodeHash) throws RemoteException {
        System.out.println(nodeHash);
        return theServer.getNeighboursFail(nodeHash);
    }

    public boolean hasServer(){
        return initialised;
    }
}
