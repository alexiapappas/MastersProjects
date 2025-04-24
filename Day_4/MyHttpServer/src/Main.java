import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;


public class Main {


    public static void main(String[] args) throws IOException {

        ServerSocket serverSocket = new ServerSocket(8080);
        serverSocket.setReuseAddress(true);

        while (true) {
            Socket socket;
            String response;

            try {
                socket = serverSocket.accept();
                try {
                    response = ServerFunctions.createResponse(socket);
                    ServerFunctions.writeResponse(socket, response);
                } catch (FileNotFoundException e){
                    response = "File not found or file does not exist. From error: " + e.getMessage();
                    ServerFunctions.writeResponse(socket, response);
                }
            } catch (IOException e){
                // Log this and don't sent to client
                System.out.println("Lost connection or unable to make a connection with the client. From error: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}