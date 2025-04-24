import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) throws IOException {

        ServerSocket serverSocket = new ServerSocket(8080); //8080 is the port address the client will access. prepares to accept requests from many different clients


        while (true) { //helps accept requests indefinitely
            Socket socket = serverSocket.accept(); //listens for a connection to be made to this socket and accepts it. the method blocks all the proceeding code until a connection is made

            InputStream input = socket.getInputStream();
            Scanner scanner = new Scanner(input);
            System.out.println(scanner.nextLine());

            System.out.println(socket.getInetAddress()); //returns the client IP address
            System.out.println(socket.getPort()); //returns the client port address

            socket.getOutputStream().write("Welcome Client".getBytes()); //send the client a message from the server



        }
    }
}