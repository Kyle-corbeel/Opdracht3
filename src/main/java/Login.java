import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Login extends Remote {
    int getBalance() throws RemoteException;
    int withdraw(int amount) throws RemoteException;
    int deposit(int amount) throws RemoteException;
}