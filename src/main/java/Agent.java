import java.io.Serializable;
import java.lang.instrument.Instrumentation;
import java.util.HashMap;

public class Agent implements Serializable, Runnable{
    private HashMap<String,Boolean> Fileslist;

    public static void premain(String agentargs, Instrumentation inst)
    {

    }

    private HashMap<String,Boolean> searchfiles()
    {

        return null;
    }

    public void run() {

    }
}
