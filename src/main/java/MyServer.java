import java.io.*;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collections;
import java.util.HashMap;

public class MyServer implements Login {
    static HashMap<Integer, String> ipMap;
    static File ipFile;

    protected MyServer() throws RemoteException {
    }


    public Boolean register(String nodeName) throws RemoteException {
        int hash;
        hash = Math.abs(nodeName.hashCode()) % 327680;
        ipMap.put(hash, nodeName);
        try {
            saveFile(ipFile);
            System.out.println("Saved: Hash: "+hash+"\tHost: "+nodeName);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    public int hash(String nodeName)throws RemoteException {
        int hash;
        hash = Math.abs(nodeName.hashCode()) % 327680;
        return hash;
    }

    public Boolean remove(String ip) throws RemoteException {
        int hash;
        hash = Math.abs(ip.hashCode()) % 327680;
        ipMap.remove(hash);
        try {
            saveFile(ipFile);
            System.out.println("Removed: Hash: "+hash+"\tHost: "+ip);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    public String getOwner(String fileName) throws RemoteException {
        int hash;
        int closeKey = 0;
        hash = Math.abs(fileName.hashCode()) % 327680;
        System.out.println(hash);
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
        System.out.println(closeKey);
        return ipMap.get(closeKey);
    }

    public static void main(String args[]) {
        try {
            ipFile = new File("IpMap.xml");
            if(ipFile.exists())
            {
                loadFile(ipFile);
            }
            MyServer obj = new MyServer();
            MulticastPublisher publisher = new MulticastPublisher();
            MulticastReceiver receiver = new MulticastReceiver("NameServer");
            receiver.start();
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
        writer.close();
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
