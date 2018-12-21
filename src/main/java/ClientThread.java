public class ClientThread extends Thread{

    private volatile boolean running = true;
    private Message mess;
    private NodeData data;
    private NetworkHandler net;
    private MulticastReceiver multi;



    public ClientThread(NodeData data) {        //Works as a background thread, processing multicasts
        this.data = data;
    }

    public void run(){
        net = new NetworkHandler(data);
        multi = new MulticastReceiver(data);
        MulticastSender multis = new MulticastSender(data);

        networkSetup();
        data.startRMI();
        FileHandler file = new FileHandler(data);

        while(running){

            mess=null;

            do{
                mess=multi.receiveMulticast();
            }while(mess==null);

            System.out.println("Received a message: "+mess);
            running = net.processMulticast(mess);
            file.processMulticast(mess);
        }
    }

    public void networkSetup(){
        net.sendBootstrap();


        while(running && !net.isSetup()){

            mess=null;

            do{
                mess=multi.receiveMulticast();
            }while(mess==null);

            System.out.println("Received a message: "+mess);
            running = net.processMulticast(mess);
        }
    }

    public void toggleRunning(){
        if(running){
            running=false;
        }else{
            running=true;
        }
    }

}
