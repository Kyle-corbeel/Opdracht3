import java.io.*;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class MyServer implements Login {
    static HashMap<Integer, String> ipMap;
    static File ipFile;
    static String ip;
    static String naam = "NameServer";

    protected MyServer() {
    }

    public static void main(String args[]) {
        boolean running = true;
        Message message;


        try {
            //Checking or creating IpMap
            ipMap = new HashMap<Integer, String>();
            ipFile = new File("IpMap.xml");
            /*if(ipFile.exists())
            {
                loadFile(ipFile);
            }*/
            ip = getIp();
            String nodeName =ip+":"+naam;

            //Opstarten multicast
            MulticastPublisher publisher = new MulticastPublisher(nodeName);
            MulticastReceiver receiver = new MulticastReceiver(nodeName);

            //Opstarten RMI
            MyServer obj = new MyServer();
            Login stub = (Login) UnicastRemoteObject.exportObject(obj, 0);
            Registry r = LocateRegistry.createRegistry(1099);
            r.bind("myserver", stub);
            System.out.println("Naming server is ready");


            while(running)
            {

                message = receiver.check();
                System.out.println(message);
                if(message != null)
                {

                    if (message.commandIs("Bootstrap")) {
                        publisher.multicast("BootServerReply " + countNodes());
                        addToMap(message.getSender());
                    }
                    if (message.commandIs("Shut")) {
                        remove(message.getSender());
                    }
                    if (message.commandIs("Failed")) {


                    }

                }

            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getIp()
    {
        try{
            String ip = new String();
            final DatagramSocket socket = new DatagramSocket();
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);        //Haalt IP van host
            ip = socket.getLocalAddress().getHostAddress();
            return ip;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }

    }


    /*public HashMap<Integer, String> register(String nodeName) throws RemoteException {
        int hash;
        hash = Math.abs(nodeName.hashCode()) % 327680;
        ipMap.put(hash, nodeName);
        try {
            saveFile(ipFile);
            System.out.println("Saved: Hash: "+hash+"\tHost: "+nodeName);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ipMap;
    }*/

    public static int hash(String nodeName){
        int hash;
        hash = Math.abs(nodeName.hashCode()) % 327680;
        return hash;
    }

    public static boolean addToMap(String sender){
        int hash = hash(sender);
        ipMap.put(hash,sender);
        try {
            saveFile(ipFile);
            System.out.println("Added Hash: "+hash+"\tHost: "+sender);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    public static boolean remove(String ip){
        int hash = hash(ip);
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

    private static void saveFile(File saveFile) throws IOException {
        Writer writer = new BufferedWriter(new FileWriter(saveFile));
        for (Integer hash: ipMap.keySet()){

            String key =hash.toString();
            String value = ipMap.get(hash).toString();
            writer.write(key + " " + value +"\n");



        }
        System.out.println("Saved xml-file..");
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

        System.out.println("Loaded existing xml-file..");
    }

    private static int countNodes(){
        return ipMap.size();
    }
}
