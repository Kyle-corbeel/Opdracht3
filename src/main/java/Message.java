/**
 * Basic information:
 * This class is to split our messages into their parts so we can extract information straightforwardly.
 * Most of the methods here are straightforward
 */
public class Message {

    private String everything="";
    private String content="";
    private String sender="";
    private String senderIp="";
    private String senderName="";
    private String command="";
    private String[] parameters;
    private int parameterAmount=0;
    private boolean empty=true;

    public Message(String mess) {
        if (mess != null) {
            empty = false;
            everything = mess;
            content = mess.split("\tsender:")[0];
            sender = mess.split("sender:")[1].split("#")[0];
            senderIp = sender.split(":")[0];
            senderName = sender.split(":")[1];
            command = content.split(" ")[0];
            if(content.split(" ").length>1){
                String paramFull = content.split(" ",2)[1];
                parameters = paramFull.split(" ");
                parameterAmount = parameters.length;
                /*for(int i=0;i<parameterAmount;i++) {
                    System.out.println("Parameter "+i+":"+parameters[i]);
                }*/
            }

        }
    }

    public Message(){

    }

    public String[] getParameters() {
        return parameters;
    }

    public boolean isEmpty(){
        return empty;
    }

    public String getContent() {
        return content;
    }

    public String getSenderID() {
        return sender;
    }

    public String getSenderIp() {
        return senderIp;
    }

    public String getSenderName() {
        return senderName;
    }

    public String toString(){
        return everything;
    }

    public String getCommand(){
        return command;
    }

    public boolean commandIs(String s){
        return command.equals(s);
    }


    public int getNodeCount(){
        return Integer.parseInt(content.split(" ")[1].split("\t")[0]);
    }
}
