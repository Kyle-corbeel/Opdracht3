import java.io.*;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.HashMap;

public class ServerMapHandler extends MulticastSender{
    private HashMap<Integer, String> ipMap;
    private File ipMapFile;


    public ServerMapHandler(NodeData d){
        super(d);
        ipMap = new HashMap<Integer, String>();
        ipMapFile = new File("IpMap.xml");
        saveFile(ipMapFile);

    }

    public boolean processMulticast(Message mess){         //Decides what to do when certain messages arrive

        boolean processed = true;

        switch (mess.getCommand()) {
            case "Bootstrap":
                sendMulticast("BootReplyServer "+ipMap.size());
                addNode(mess.getSenderID());
                break;
            case "Shut":
                removeNode(mess.getSenderID());
                break;

                /*TODO
                Discovery -> Failure. Method aanmaken die van een node de previous en next uit de map haalt en deze doorstuurt.
                 */
            default:
                processed = false;
                //System.out.println("Mapcommand not found");
        }
        return processed;
    }

    public void addNode(String sender) {
        int hash = data.hash(sender);
        ipMap.put(hash, sender);
        saveFile(ipMapFile);
        System.out.println("Added Hash: " + hash + "\tHost: " + sender+"\n");
    }

    public boolean removeNode(String ip) {
        int hash = data.hash(ip);
        ipMap.remove(hash);
        saveFile(ipMapFile);
        System.out.println("Removed: Hash: " + hash + "\tHost: " + ip);
        return true;
    }

    private void saveFile(File saveFile){                   //Saves the IPmap to a file
        try {
            Writer writer = new BufferedWriter(new FileWriter(saveFile));

            for (Integer hash : ipMap.keySet()) {
                String key = hash.toString();
                String value = ipMap.get(hash).toString();
                writer.write(key + " " + value + "\n");
            }
            System.out.println("Saved xml-file..");
            writer.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public String getOwner(String fileName) throws RemoteException {
        int hash;
        int closeKey = 0;
        hash = Math.abs(fileName.hashCode()) % 327680;
        //System.out.println(hash);
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
        //System.out.println(closeKey);
        return ipMap.get(closeKey);
    }

    public String getIDFromHash(int hash) {
        return ipMap.get(hash);
    }

    public String getNeighboursFail(String nodeID) throws RemoteException {
        int nodeHash = data.hash(nodeID);
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
}
