public class Message {

    private String everything="";
    private String content="";
    private String sender="";
    private String senderIp="";
    private String senderName="";
    private String command="";

    public Message(String mess){

        everything = mess;
        //System.out.println(mess);
        content = mess.split("sender:")[0];
        sender = mess.split("sender:")[1];
        senderIp = sender.split(":")[0];
        senderName = sender.split(":")[1];
        command = content.split(" ")[0];

    }

    public Message(){

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
        //System.out.println(command+"check waaar spatie komt");
        return command.equals(s);
    }

    /*public boolean has(String s){
        return content.contains(s);
    }*/

    public boolean has(String s){
        return content.startsWith(s);
    }

    public boolean isEmpty(){
        return (everything.equals(""));
    }
}
