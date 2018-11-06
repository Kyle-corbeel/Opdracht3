import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class MyServer implements Login {
    protected MyServer() throws RemoteException {
    }


    public int getBalance() throws RemoteException {
        return(0);
    }

    public int withdraw(int amount) throws RemoteException {
        return(0);
    }

    public int deposit(int amount) throws RemoteException {
        return(0);
    }

    public static void main(String args[]) {
        try {
            MyServer obj = new MyServer();
            Login stub = (Login) UnicastRemoteObject.exportObject(obj, 0);
            Registry r = LocateRegistry.createRegistry(1099);
            r.bind("myserver", stub);
            System.out.println("bankserver is ready");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
