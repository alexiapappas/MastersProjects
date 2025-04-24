import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Scanner;

public class Cilent {

    public static void main (String[] args) throws IOException {

        Socket socket = new Socket("localhost", 8080); //needs the ip address and the host number to establish a connection

        String message = "Hello Network";

        socket.getOutputStream().write((message + "\n").getBytes()); //convert the data into bytes

        InputStream input = socket.getInputStream();
        Scanner in = new Scanner(input);
        while (true) {
            System.out.println(in.nextLine());
        }






    }

}
