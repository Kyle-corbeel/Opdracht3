import java.io.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collections;
import java.util.HashMap;

public class MyServer implements Login {
    static HashMap<Integer, String> ipMap;
    protected MyServer() throws RemoteException {
    }


    public Boolean register(String ip) throws RemoteException {
        int hash;
        hash = Math.abs(ip.hashCode()) % 327680;
        ipMap.put(hash, ip);
        return true;
    }

    public Boolean remove(String ip) throws RemoteException {
        int hash;
        hash = Math.abs(ip.hashCode()) % 327680;
        ipMap.remove(hash);
        return true;
    }

    public String getOwner(String fileName) throws RemoteException {
        int hash;
        int closeKey = 0;
        hash = Math.abs(fileName.hashCode()) % 327680;
        for (Integer key: ipMap.keySet()){

            if(key<hash)
            {
                if(key>closeKey)
                {
                    closeKey = key;
                }
            }
        }
        if(closeKey ==0 )
        {
            closeKey = Collections.max(ipMap.keySet());
        }
        return ipMap.get(closeKey);
    }

    public static void main(String args[]) {
        try {
            File ipFile = new File("IpMap.xml");
            if(ipFile.exists())
            {
                loadFile(ipFile);
            }
            saveFile(ipFile);
            MyServer obj = new MyServer();
            Login stub = (Login) UnicastRemoteObject.exportObject(obj, 0);
            Registry r = LocateRegistry.createRegistry(1099);
            r.bind("myserver", stub);
            System.out.println("Naming server is ready");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void saveFile(File saveFile) throws IOException {
        Writer writer = new BufferedWriter(new FileWriter(saveFile));
        for (Integer hash: ipMap.keySet()){

            String key =hash.toString();
            String value = ipMap.get(hash).toString();
            writer.write(key + " " + value +"\n");



        }
    }

    private static void loadFile(File loadFile) throws IOException {
        HashMap<Integer, String> ipTemp = new HashMap<Integer, String>();
        BufferedReader br = new BufferedReader(new FileReader(loadFile));
        String line;
        while ((line = br.readLine()) != null)
        {
            String[] splitLine = line.split(" ");
            ipTemp.put(Integer.parseInt(splitLine[0]), splitLine[1]);
        }
        ipMap = ipTemp;
    }
}
