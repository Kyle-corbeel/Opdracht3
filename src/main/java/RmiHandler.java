import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.rmi.Naming;
import java.rmi.RemoteException;

public class RmiHandler {

    String nodeName="";
    Login theServer = null;

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
            theServer.register(nodeName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
