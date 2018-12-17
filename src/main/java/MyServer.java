import java.io.*;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;

public class MyServer implements Login {
    static HashMap<Integer, String> ipMap;
    static File ipFile;
    static String ip;
    static String naam = "myserver";
    static ServerMulticast multi;

    protected MyServer() {

    }

    public static void main(String args[]) {
        boolean running = true;
        Message message;


        try {
            ipMap = new HashMap<Integer, String>();
            ipFile = new File("IpMap.xml");     //Creating ipMap
            /*if(ipFile.exists())
            {
                loadFile(ipFile);
            }*/

            ip = getIp();
            System.out.println(ip);
            String nodeName = ip + ":" + naam;
            multi = new ServerMulticast(nodeName);       //Create multicast-object


            //Opstarten RMI
            MyServer obj = new MyServer();
            Login stub = (Login) UnicastRemoteObject.exportObject(obj, 0);
            System.out.println(System.getProperty("java.rmi.server.hostname"));
            System.setProperty("java.rmi.server.hostname",ip);
            System.out.println(System.getProperty("java.rmi.server.hostname"));
            Registry r = LocateRegistry.createRegistry(1099);
            r.bind("myserver", stub);
            System.out.println("Naming server is ready");

            while (running)        //Action loop
            {

                message = multi.receiveMulticast();

                if (message != null) {

                    if (message.commandIs("Bootstrap\t")) {
                        multi.sendMulticast("BootServerReply " + countNodes());
                        addToMap(message.getSender());
                        //System.out.println("in den bootstrap if");
                    }
                    if (message.getContent().contains("Shutdown")) {
                        remove(message.getSender());
                    }
                    if (message.commandIs("Failed")) {          //The server will act upon incoming Failure.

                    }
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getIp() {
        String ip = "";
        try {
            NetworkInterface eth = NetworkInterface.getByName("eth0");
            Enumeration<InetAddress> adresNUM = eth.getInetAddresses();
            while(adresNUM.hasMoreElements() && ip.equals(""))
            {
                InetAddress addr = adresNUM.nextElement();
                if(addr instanceof Inet4Address && !addr.isLoopbackAddress())
                {
                    ip = addr.getHostAddress();
                }
            }
            return "" + ip;
        } catch (Exception e) {
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

    public static int hash(String nodeName) {
        int hash;
        hash = Math.abs(nodeName.hashCode()) % 327680;
        return hash;
    }

    public static boolean addToMap(String sender) {
        int hash = hash(sender);
        ipMap.put(hash, sender);
        try {
            saveFile(ipFile);
            System.out.println("Added Hash: " + hash + "\tHost: " + sender);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    public static boolean remove(String ip) {
        int hash = hash(ip);
        ipMap.remove(hash);
        try {
            saveFile(ipFile);
            System.out.println("Removed: Hash: " + hash + "\tHost: " + ip);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    public HashMap<Integer, String> getTopology() throws RemoteException {
        return ipMap;
    }

    public String getOwner(String fileName) throws RemoteException {
        int hash;
        int closeKey = 0;
        hash = Math.abs(fileName.hashCode()) % 327680;
        System.out.println(hash);
        for (Integer key : ipMap.keySet()) {

            if (key < hash) {
                if (key > closeKey) {
                    closeKey = key;
                }
            }
        }

        if (closeKey == 0) {
            closeKey = Collections.max(ipMap.keySet());
        }
        System.out.println(closeKey);
        return ipMap.get(closeKey);
    }

    public String getNeighboursFail(int nodeHash) throws RemoteException {
        int previous = 0;
        int next = 327680;
        ipMap.remove(nodeHash);
        for (Integer key : ipMap.keySet()) { //Lagere buur vinden

            if (key < nodeHash) {
                if (key > previous) {
                    previous = key;
                }
            }
        }
        if (previous == 0 && !ipMap.containsKey(0)) {
            previous = Collections.max(ipMap.keySet());
        }

        for (Integer key : ipMap.keySet()) { //Hogere buur vinden

            if (key < nodeHash) {
                if (key < next) {
                    next = key;
                }
            }
        }
        if (next == 327680 && !ipMap.containsKey(327680)) {
            next = Collections.max(ipMap.keySet());
        }

        String returnSentence = (ipMap.get(previous) +" " +ipMap.get(next));
        return returnSentence;
    }

    private static void saveFile(File saveFile) throws IOException {
        Writer writer = new BufferedWriter(new FileWriter(saveFile));
        for (Integer hash : ipMap.keySet()) {

            String key = hash.toString();
            String value = ipMap.get(hash).toString();
            writer.write(key + " " + value + "\n");


        }
        System.out.println("Saved xml-file..");
        writer.close();
    }

    private static void loadFile(File loadFile) throws IOException {
        HashMap<Integer, String> ipTemp = new HashMap<Integer, String>();
        BufferedReader br = new BufferedReader(new FileReader(loadFile));
        String line;
        while ((line = br.readLine()) != null) {
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
