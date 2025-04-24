import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

//Main class to start the server and handle incoming client connections
public class Main {
    public static void main(String[] args) throws IOException {

        //Create a ServerSocket listening on port 8080
        ServerSocket serverSocket = new ServerSocket(8050);
        serverSocket.setReuseAddress(true);

        //Infinite loop to accept incoming client connections
        while (true) {
            //Declare a Socket to handle the client connection
            Socket client;

            try {
                //Accept a new client connection and create a Socket for it
                client = serverSocket.accept();

                //Create a new CHR to handle the client's requests
                ClientHandlerRunnable chR = new ClientHandlerRunnable(client);

                //Create a new thread to run the CHR
                Thread ssThread = new Thread(chR);

                //Start the thread to handle the client
                ssThread.start();
                System.out.println("Thread: " + ssThread.threadId());
            } catch (IOException e) {
                // Log the exception and continue without sending a response to the client
                System.out.println("Lost connection or unable to make a connection with the client. From error: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}