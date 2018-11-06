import java.io.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

public class MyServer implements Login {
    static HashMap<String, Integer> ipMap;
    protected MyServer() throws RemoteException {
    }


    public Boolean register(String ip) throws RemoteException {
        int hash;
        hash = Math.abs(ip.hashCode()) % 327680;

        return true;
    }

    public String getOwner(String fileName) throws RemoteException {
        return null;
    }

    public static void main(String args[]) {
        try {
            File ipFile = new File("IpMap.xml");
            if(ipFile.exists())
            {
                load(ipFile);
            }
            saveFile(ipFile);
            MyServer obj = new MyServer();
            Login stub = (Login) UnicastRemoteObject.exportObject(obj, 0);
            Registry r = LocateRegistry.createRegistry(1099);
            r.bind("myserver", stub);
            System.out.println("bankserver is ready");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void saveFile(File saveFile) throws IOException {
        Writer writer = new BufferedWriter(new FileWriter(saveFile));
        for (String name: ipMap.keySet()){

            String key =name.toString();
            String value = ipMap.get(name).toString();
            writer.write(key + " " + value +"\n");



        }
    }

    private static void load(File loadFile) throws IOException {
        HashMap<String, Integer> ipTemp = new HashMap<String, Integer>();
        BufferedReader br = new BufferedReader(new FileReader(loadFile));
        String line;
        while ((line = br.readLine()) != null)
        {
            String[] splitLine = line.split(" ");
            ipTemp.put(splitLine[0], Integer.parseInt(splitLine[1]));
        }
        ipMap = ipTemp;
    }
}
