import org.w3c.dom.Node;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class FileAddCheckThread extends Thread {

    ArrayList<String> oldFiles, newFiles;
    boolean changed;
    NodeData data;
    MulticastSender multis;

    public FileAddCheckThread(NodeData data){
        this.data = data;
        multis = new MulticastSender(data);
    }


    public void run(){
        oldFiles = listFiles("Local");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        newFiles = listFiles("Local");
        changed = oldFiles.equals(newFiles);
        if(changed){
            multis.sendMulticast("FileChange");
        }

    }

    public boolean filesChanged(){
        return changed;
    }

    private ArrayList<String> listFiles(String directory) {
        File folder = new File("Files/"+directory);
        ArrayList<String> fileNames = new ArrayList<String>();
        File[] listOfFiles = folder.listFiles();

        System.out.println("Listing local files..");

        for (int i = 0; i < listOfFiles.length; i++) {
            System.out.println("Local File: " + listOfFiles[i].getName());
        }

        for(File f : listOfFiles){
            fileNames.add(f.getName());
        }
        System.out.println();
        return fileNames;
    }

}
