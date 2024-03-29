import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.io.IOException;
import java.io.PrintWriter;

public class Client implements Runnable { //Runnable enables multiple clients to be able to run at the same time.

    private Socket client;
    private BufferedReader in;
    private PrintWriter out;
    private boolean done;

    //Runs the program.
    @Override
    public void run() {
        try {
            client = new Socket("127.0.0.1", 9999);                 //Standard Host IP, connects client to server.
            out = new PrintWriter(client.getOutputStream(), true);      //Client output.
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));        //User input.

            //Makes it possible to run several Clients at once.
            InputHandler inHandler = new InputHandler();
            Thread t = new Thread(inHandler);
            t.start();

            //Messages from server/other clients.
            String inMessage;
            while ((inMessage = in.readLine()) != null) {
                System.out.println(inMessage);
            }
        } catch (IOException e){
            shutdown();
        }
    }

    //Disconnects and closes the client.
    public void shutdown() {
        done = true;
        try {
            in.close();
            out.close();
            if (!client.isClosed()) {
                client.close();
            }
        } catch (IOException e) {
            // ignore
        }
    }

    //Manages inputs while done = false.
    class InputHandler implements Runnable {   //The class can be executed multiple times, many threads

        @Override
        public void run() {
            try {
                BufferedReader inReader = new BufferedReader(new InputStreamReader(System.in));     //Checks all inputs.
                while (!done) {
                    String message = inReader.readLine();
                    if (message.equals("/quit")) {      //Quits the program if message is quit.
                        out.println(message);
                        inReader.close();
                        shutdown();
                    } else {
                        out.println(message);           //Sends the message.
                    }
                }
            } catch (IOException e) {
                shutdown();
            }
        }
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }
}
