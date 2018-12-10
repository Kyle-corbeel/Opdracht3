import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;

public interface Login extends Remote {
    //HashMap<Integer, String> register(String ip) throws RemoteException;
    String getOwner(String fileName) throws RemoteException;
    String getIpFromHash(int hash) throws RemoteException;
    //Boolean  remove(String ip) throws RemoteException;
}