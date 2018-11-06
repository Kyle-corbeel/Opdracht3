import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.Naming;
import java.rmi.NotBoundException;

public class MyClient {

    public static void main(String[] args) throws IOException, NotBoundException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Welkom bij bank met veel geld, gelieve un naam + achternaam in te geven:");
        String s = br.readLine();
        String[] naam = s.split(" ");

        Login theServer = (Login) Naming.lookup("rmi://localhost/myserver");
        Boolean cont = true;
        while(cont)
        {
            System.out.println("Wat wilt u doen:");
            System.out.println("1:Controleer balans:");
            System.out.println("2:Voeg geld toe");
            System.out.println("3:Haal geld af");
            System.out.println("4:Sluit af");
            s = br.readLine();
            if(s.equals("1")){
                System.out.println("Uw saldo bedraagt: " +theServer.getBalance());
            }
            if(s.equals("2")){
                System.out.println("Hoeveel wenst u te storten?");
                String amount = br.readLine();
                System.out.println("Uw nieuwe saldo is: " +theServer.deposit(Integer.parseInt(amount)));
            }
            if(s.equals("3")){
                System.out.println("Hoeveel wenst u af te nemen?");
                String amount = br.readLine();
                System.out.println("Uw nieuwe saldo is: " +theServer.withdraw(Integer.parseInt(amount)));
            }
            if(s.equals("4")){
                cont = false;
            }

        }

    }
}
