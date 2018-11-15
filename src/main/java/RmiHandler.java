import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.HashMap;

public class RmiHandler {

    String nodeName="";
    Login theServer = null;
    HashMap<Integer,String> ipMap;

    public RmiHandler(String nodeName) {
        this.nodeName=nodeName;
    }



    public void initialise(String serverIp){

        try {
            theServer = (Login) Naming.lookup("rmi://"+serverIp+"/myserver");
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String s = br.readLine();
            MulticastPublisher publisher = new MulticastPublisher(nodeName);

            Boolean cont = true;
            ipMap = theServer.register(nodeName);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public HashMap<Integer,String> getMap(){
        for (Integer ipName: ipMap.keySet()){
                System.out.println(ipName);
        }
        return ipMap;
        
    }

}
