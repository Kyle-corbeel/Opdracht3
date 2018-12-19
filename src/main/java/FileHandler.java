import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class FileHandler extends MulticastSender{

    boolean initialised = false;
    Login theServer = null;
    TCPHandler tcp;
    Queue<String> repliQueue = new LinkedList<>();
    ArrayList<String> replicatedNames = new ArrayList<String>();
    ArrayList<String> fileNames;
    FileAddCheckThread fileChecker;

    public FileHandler(NodeData d){
        super(d);
        rmiStartup();
        tcp = new TCPHandler();
        fileStartup();
        //fileChecker = new FileAddCheckThread(data);
    }

    /*TODO
    Replication -> UPDATE (thread checkt constant of een nieuwe file werd aangemaakt)
    Replication -> SHUTDOWN
     */

    public boolean processMulticast(Message mess) {
        boolean processed = true;

        switch (mess.getCommand()) {
            case "Replicate":
                if(mess.getParameters()[1].equals(data.getNodeID())){
                    System.out.println("Request received : opening TCP socket");
                    tcp.getFromTCP(mess.getParameters()[0]);
                }
                break;
            case "Bootstrap":
                newNodeReplication();
                break;
            case "Filechange":
                ArrayList<String> newFileNames = listFiles("Local");
                for(String s : newFileNames) {
                    if (!fileNames.contains(s)) {
                        replicateFile(s,whereToReplicate(s));
                    }
                }
                break;
            default:
                processed = false;
                break;
        }
        return processed;
    }

    private void rmiStartup() {
        try {
            theServer = (Login) Naming.lookup("rmi://"+data.getServerIP()+"/myserver");
            initialised = true;
        } catch (NotBoundException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void fileStartup() {
        fileNames = listFiles("Local");
        for(String f : fileNames){
            replicateFile(f, whereToReplicate(f));
        }

    }

    private void replicateFile(String f, String receiver){
        if (receiver == null) {
            System.out.println("File "+f+" doesnt have to be replicated");
        }else {
            sendMulticast("Replicate " + f + " " + receiver);
            tcp.sendToTCP(receiver, "Files\\Local\\"+f);
        }
    }

    private void newNodeReplication() {
        ArrayList<String> replicatedFileNames = listFiles("Replicated");
        for(String f : replicatedFileNames){
            if(!getOwner(f).equals(data.getNodeID())){
                replicateFile(f, getOwner(f));
                System.out.println("Deleted : "+removeFile(f));
            }
        }

        /*TODO
        Check if replication is handled correctly when a new node enters the network
         */

    }

    private boolean removeFile(String f){
        boolean deleted = false;

        try {
            deleted = Files.deleteIfExists(Paths.get(f));
        } catch (IOException e) {
            e.printStackTrace();
        }


        return deleted;
        /*TODO
        Nagaan of deze removefile werkt
         */
    }

    private ArrayList<String> listFiles(String directory) {
        File folder = new File("Files\\"+directory);
        ArrayList<String> fileNames = new ArrayList<String>();
        File[] listOfFiles = folder.listFiles();

        System.out.println("Listing local files..");

        for (int i = 0; i < listOfFiles.length; i++) {
            System.out.println("Local File: " + listOfFiles[i].getName());
        }

        for(File f : listOfFiles){
            fileNames.add(f.getName());
            //repliQueue.add(f.getName());         //First time the files get checked, so they have to be replicated
        }
        System.out.println();
        return fileNames;
    }

    private String whereToReplicate(String name) {
        String replicateNode="";
        boolean send = true;

        String owner = getOwner(name);
        if(owner.equals(data.getNodeID())){
            replicateNode = getIDFromHash(data.getPreviousNode());
            if(replicateNode.equals(data.getNodeID())){
                send = false;
            }
        }else {
            replicateNode = owner;
        }
        if(send==true) {
            return replicateNode;
        }else {
            return null;
        }
    }

    public String getOwner(String fileName){
        String owner="";
        try {
            owner = theServer.getOwner(fileName);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return owner;
    }

    public String getIDFromHash(int hash){
        String ID="";
        try {
            ID = theServer.getIDFromHash(hash);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return ID;
    }


}
