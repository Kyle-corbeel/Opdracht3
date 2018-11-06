import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Login extends Remote {
    Boolean register(String ip) throws RemoteException;
    String getOwner(String fileName) throws RemoteException;
}