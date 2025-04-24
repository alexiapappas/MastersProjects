import java.io.IOException;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;

//This is the runnable created when a Thread is instantiated upon a new client connection
public class ClientHandlerRunnable implements Runnable {
    Socket socket_;

    //Constructor to initialize the CHR with a socket
    ClientHandlerRunnable(Socket socket) {
        socket_ = socket;
    }

    //The run method is executed when the thread starts. First create a HttpRequest object to parse the incoming request, get the requested file
    //name and log it. Then create the HTTP response based on the request and write the response back to the client. Check if the request is for
    //a WebSocket connection and if it is, start the WS connection. If it's a regular HTTP request, close the socket after responding.
    @Override
    public void run() {
        try {
            HttpRequest request = new HttpRequest(socket_);
            String fileName = request.getFileName();
            System.out.println(fileName);
            byte [] response = HttpResponse.createResponse(request, fileName);
            HttpResponse.writeResponse(socket_, response);
            if (request.hasHeader("Sec-WebSocket-Key")) {
                WebSocketHandler wsHandler = new WebSocketHandler(socket_);
                wsHandler.startWSConnection(socket_);
            } else {
                socket_.close();
            }
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}