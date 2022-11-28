import java.io.IOException;
import java.net.ServerSocket;

//Servern uppgift är att lyssna efter förfrågningar från clienter och skapa kopplingen
public class Server implements Runnable{ //Klassen kan köras samtidigt flera gånger, threads

    @Override
    public void run(){
        try {  //Fånga eventuella fel
            ServerSocket server = new ServerSocket(9999);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    class ConnectionHandler implements Runnable{

        @Override
        public void run() {

        }
    }
}
