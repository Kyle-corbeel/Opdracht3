public class Message {

    private String everything="";
    private String content="";
    private String sender="";
    private String senderIp="";
    private String senderName="";

    public Message(String mess){

        everything = mess;
        content = mess.split("\tsender:")[0];
        sender = mess.split("\tsender:")[1];
        senderIp = sender.split(":")[0];
        senderName = sender.split(":")[1];

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

    public boolean has(String s){
        return content.contains(s);
    }

    public boolean isEmpty(){
        return (everything.equals(""));
    }
}
