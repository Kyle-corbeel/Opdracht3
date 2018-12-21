import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPHandler {


    public TCPHandler(){

    }

    public void sendToTCP(String receiver, String fileName) {
        String hostName = receiver.split(":")[0];
        int portNumber = 4444;

        try {
            Socket echoSocket = new Socket("localhost", portNumber);
            PrintWriter out = new PrintWriter(echoSocket.getOutputStream(), true);
            //BufferedReader in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
            String line = null;

            // FileReader reads text files in the default encoding.
            FileReader fileReader =
                    new FileReader(fileName);

            // Always wrap FileReader in BufferedReader.
            BufferedReader bufferedReader =
                    new BufferedReader(fileReader);

            while ((line = bufferedReader.readLine()) != null) {
                out.println(line);
                //System.out.println(line);
            }
            // Always close files.
            bufferedReader.close();
            System.out.println("Done sending file..\n");
            out.close();
            echoSocket.close();
            /*while (in.readLine() != null) {
                System.out.println(in.readLine());
            }
            System.exit(0);*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getFromTCP(String fileName){
        int portNumber = 4444;//Integer.pars    eInt(args[0]);
        try {
            File f = new File("Files\\Replicated\\"+fileName);
            ServerSocket serverSocket = new ServerSocket(4444/*Integer.parseInt(args[0]*/);
            Socket clientSocket = serverSocket.accept();
            //PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String inputLine;
            BufferedWriter writer = new BufferedWriter(new FileWriter(f));
            while ((inputLine = in.readLine()) != null) {
                writer.write(inputLine);
                writer.newLine();
                //System.out.println(inputLine);
                //out.println(inputLine);
            }
            writer.close();
            System.out.println("Done downloading file..\n");
            clientSocket.close();
            serverSocket.close();
        } catch (IOException e) {
            System.out.println("Exception caught when trying to listen on port "
                    + portNumber + " or listening for a connection");
            System.out.println(e.getMessage());
        }
    }
}
