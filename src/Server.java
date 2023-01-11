import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Scanner;
import java.io.IOException;
import java.io.File;

public class Server implements Runnable{ //The class can be executed multiple times, many threads

    private ArrayList<ConnectionHandler> connections; //List of clients
    private ServerSocket server;
    private boolean done;
    private ExecutorService pool;

    public Server(){
        connections = new ArrayList<>();
        done = false;
    }
    @Override
    public void run(){ //Must have this since runnable need something to run
        try {
            server = new ServerSocket(9999); //Creates new server socket for the server
            pool = Executors.newCachedThreadPool(); //Makes threads reusable
            while(!done) {
                Socket client = server.accept(); //Accepting connection creates a socket
                ConnectionHandler handler = new ConnectionHandler(client); //Creates new handler for every client
                connections.add(handler); //Adds the client to the list of clients
                pool.execute(handler);
            }
        } catch (IOException e) { //Catching IO errors
            shutdown();
        }
    }

    public void broadcast(String message){  //Broadcasts the messege to the client
         for (ConnectionHandler ch : connections){ //Foreach loop
             if (ch != null){
                 ch.sendMessage(message);
             }
         }
    }

    public void shutdown() {   //Shuts down server and program
        try {
            done = true;
            pool.shutdown();
            if (!server.isClosed()) {
                server.close();
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
        private BufferedReader in; // "In" will get the information from the client
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
                    try {
                        FileWriter myWriter = new FileWriter("logFile.txt", true);
                        myWriter.write(message);
                        myWriter.close();
                    } catch (IOException e) {
                        System.out.println("Could not write to file");
                        e.printStackTrace();
                    }
                    if (message.startsWith("/nick ")) {
                        String[] messageSplit = message.split(" ", 2); //After space is nickname
                        if (messageSplit.length == 2) {
                            broadcast(nickname + " renamed themselves to " + messageSplit[1]);
                            System.out.println(nickname + " renamed themselves to " + messageSplit[1]); //Log
                            nickname = messageSplit[1];
                            out.println("Successfully changed nickname to " + nickname);
                        } else {
                            out.println("No nickname provided!");
                        }
                    } else if (message.startsWith("/quit")) {
                        broadcast(nickname + " left the chat!");
                        System.out.println(nickname + " left the chat:( ");
                        shutdown();
                    } else if (message.startsWith("/log")) {
                        try {
                            Scanner myReader = new Scanner(new File("logFile.txt"));
                            while (myReader.hasNextLine()) {
                                String data = myReader.nextLine();
                                System.out.println(data);
                                broadcast(data);
                            }
                            myReader.close();
                        } catch (IllegalStateException e) {
                            System.out.println("An error occurred.");
                            e.printStackTrace();
                        }
                    }else {
                        broadcast(nickname + ": " + message); //Sends message to all clients
                    }
                }
            } catch (IOException e) {
                shutdown();
            }
        }
        public void sendMessage(String message){ //Sends message
            out.println(message);
        }

        public void shutdown(){
            try {
                in.close();
                out.close();
                if (!client.isClosed()){
                    client.close();
                }
            } catch (IOException e){
                //ignore
            }
        }
    }

    public static void main(String[] args) {
        File logFile = new File("logFile.txt");
        try {
            if (logFile.createNewFile()) {
                System.out.println("File created: " + logFile.getName());
            } else {
                System.out.println("File already exists.");
                FileWriter myWriter = new FileWriter("logFile.txt");
                myWriter.write("Cleared log.");
                myWriter.close();
            }
        } catch (IOException e) {
            System.out.println("No can do");
            e.printStackTrace();
        }
        Server server = new Server();
        server.run();
    }
}
