public class Message {

    private String everything="";
    private String content="";
    private String sender="";
    private String senderIp="";
    private String senderName="";
    private String command="";
    private boolean empty=true;

    public Message(String mess) {
        if (mess != null) {
            empty = false;
            everything = mess;
            //System.out.println(mess);
            content = mess.split("sender:")[0];
            sender = mess.split("sender:")[1].split("#")[0];
            senderIp = sender.split(":")[0];
            senderName = sender.split(":")[1];
            command = content.split(" ")[0];
        }
    }

    public Message(){

    }
    public boolean isEmpty(){
        return empty;
    }

    public String getContent() {
        return content;
    }

    public String getSender() {
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

    public boolean commandIs(String s){
        return command.equals(s);
    }

    /*public boolean has(String s){
        return content.contains(s);
    }*/

    public boolean has(String s){
        return content.startsWith(s);
    }

    public int getNodeCount(){
       return Integer.parseInt(content.split(" ")[1].split("\t")[0]);
    }
}
