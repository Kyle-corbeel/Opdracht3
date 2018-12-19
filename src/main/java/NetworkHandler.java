public class NetworkHandler extends MulticastSender{

    private boolean setup = false;
    private boolean bootSent = false;

    public NetworkHandler(NodeData d){
        super(d);
    }

    public boolean processMulticast(Message mess){

        boolean running = true;

        if(setup) {

            switch (mess.getCommand()) {
                case "Bootstrap":
                    welcomeNewNode(mess.getSenderID());     //A new node wants to bootstrap, let's see who he is
                    break;
                case "BootReplyServer":
                    System.out.println("This node has already been setup, ignoring BootReplyServer");
                    break;
                case "BootReplyNode":
                    System.out.println("This node has already been setup, ignoring BootReplyNode");
                    break;
                case "ShutRequest":
                    if(mess.getSenderID().equals(data.getNodeID())) {
                        sendMulticast("Shut " + data.getPreviousNode() + " " + data.getNextNode());   //If shut request comes from self, send neighbours and shutdown
                        running = false;
                    }else{
                        System.out.println("Another node is shutting down, ignoring ShutRequest");
                    }
                    break;
                case "Shut":
                    if(Integer.parseInt(mess.getParameters()[0])==data.getMyHash()){                   //If shutrequest comes from someone else, check if i'm his next/previous
                        data.setNextNode(Integer.parseInt(mess.getParameters()[1]));        //If so, set new next to the shut next or new previous to the shut previous accordingly
                        printNeighbours();
                    }else if(Integer.parseInt(mess.getParameters()[1])==data.getMyHash()){
                        data.setPreviousNode(Integer.parseInt(mess.getParameters()[0]));
                        printNeighbours();
                    }
                    break;

                default:
                    running = true;
                    //System.out.println("Networkcommand not found (setup): "+mess);
            }

        }else{

            switch (mess.getCommand()) {
                case "BootReplyServer":
                    data.setServerIP(mess.getSenderIp());

                    if(Integer.parseInt(mess.getParameters()[0])<1){        //If i'm the first node, i am my own neighbours
                        setup = true;
                        data.setPreviousNode(data.getMyHash());
                        data.setNextNode(data.getMyHash());
                    }
                    break;
                case "BootReplyNode":
                    data.setPreviousNode(data.hash(mess.getSenderID()));
                    data.setNextNode(Integer.parseInt(mess.getParameters()[0]));
                    setup = true;
                    break;
                default:
                    running = true;
                    //System.out.println("Networkcommand not found (not setup): "+mess.toString());
            }
            if(setup){
                System.out.println("This Client has entered the network.\n\n");
                printNeighbours();
            }
        }
        System.out.println();
        return running;

    }

    public void sendBootstrap(){
        sendMulticast("Bootstrap");
        bootSent = true;
    }

    public void welcomeNewNode(String senderID)                                       //Welcomes a new node to the network
    {
        int hash = data.hash(senderID);
        if (isPrevious(hash)) {                                             //If the new node is my previous, set it
            data.setPreviousNode(hash);
            printNeighbours();
        }
        if (isNext(hash)) {                                                 //If the new node is my next, set it
            sendMulticast("BootReplyNode " + data.getNextNode());   //If so, send him our last next
            data.setNextNode(hash);
            printNeighbours();
        }
    }

    public boolean isNext(int hash) {

        if(data.getMyHash() == data.getNextNode()){                 //Indien dit naar zichzelf verwijst passen we zowiezo aan.
            return true;
        }else if((data.getMyHash()<hash) && (hash < data.getNextNode())){
            return true;
        }else if(data.getNextNode()<data.getMyHash() && hash >data.getMyHash() ){
            return true;
        }else if(data.getNextNode() < data.getMyHash() && hash < data.getNextNode()){
            return true;
        }else{
            return false;
        }

    }

    public boolean isPrevious(int hash) {

        if(data.getMyHash() == data.getNextNode()){                 //Indien dit naar zichzelf verwijst passen we zowiezo aan.
            return true;
        }else if((data.getPreviousNode() < hash) && (hash <data.getMyHash())){
            return true;
        }else if(data.getPreviousNode() > data.getMyHash() && hash < data.getMyHash()){
            return true;
        }else if(data.getPreviousNode() > data.getMyHash() && hash > data.getPreviousNode()){
            return true;
        }
        else{
            return false;
        }
    }

    public boolean isSetup(){
        return setup;
    }

    public void printNeighbours(){
        System.out.println("NEW NEIGHBOURS\n\tYour previous node is: "+data.getPreviousNode()+"\tYour next node is: "+data.getNextNode());
    }
}
