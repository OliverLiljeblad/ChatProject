import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server implements Runnable{ //The class can be executed multiple times, many threads

    private ArrayList<ConnectionHandler> connections; //List of clients
    private ServerSocket server;
    private boolean done;

    public Server(){
        connections = new ArrayList<>();
        done = false;
    }
    @Override
    public void run(){ //Must have this since runnable need something to run
        try {
            server = new ServerSocket(9999); //Creates new server socket for the server
            while(!done) {
                Socket client = server.accept(); //Accepting connection creates a socket
                ConnectionHandler handler = new ConnectionHandler(client); //Creates new handler for every client
                connections.add(handler); //Adds the client to the list of clients
            }
        } catch (IOException e) { //Catching IO errors
            // TODO: handle
        }
    }

    public void broadcast(String message){  //Broadcasts the messege to the client
         for (ConnectionHandler ch : connections){ //Foreach loop
             if (ch != null){
                 ch.sendMessege(message);
             }
         }
    }

    public void shutdown() {
        try {
            if (!server.isClosed()) {
                server.close;
            }
            for (ConnectionHandler ch : connections){
                ch.shutdown();
            }
        } catch (IOException e){
            // ignore
        }
    }

    class ConnectionHandler implements Runnable{ //The class that handles the client connection
        private Socket client;
        private BufferedReader in; // "In" will "get" the information from the client
        private PrintWriter out; //When the server will write something to client, out
        private String nickname;
        public ConnectionHandler(Socket client){ //Several connections take more handlers, Client = client
            this.client = client;
        }
        @Override
        public void run() {
            try {  //Deal with the client
                out = new PrintWriter(client.getOutputStream(), true); //Send automatic to the client
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                out.println("Please enter a nickname: "); //Sends message to the client
                nickname = in.readLine(); //Save the input, from client, as nickname
                System.out.println(nickname + " connected"); //Server log
                broadcast(nickname + " joined the chat!");
                String message;
                while ((message = in.readLine()) != null) {
                    if (message.startsWith("/nick ")){
                        String[] messageSplit = message.split(" ", 2); //After space is nickname
                        if (messageSplit.length == 2){
                            broadcast(nickname + " renamed themselves to "+ messageSplit[1]);
                            System.out.println(nickname + " renamed themselves to "+ messageSplit[1]); //Log
                            nickname = messageSplit[1];
                            out.println("Successfully changed nickname to " + nickname);
                        }else {
                            out.println("No nickname provided!");
                        }
                    } else if (message.startsWith("/quit")){
                        // TODO: quit
                    } else {
                        broadcast(nickname + ": " + message); //Sends message to all clients
                    }
                }
            } catch (IOException e) {
                // TODO: handle
            }
        }
        public void sendMessege(String message){ //Sends message
            out.println(message);
        }
    }
}
