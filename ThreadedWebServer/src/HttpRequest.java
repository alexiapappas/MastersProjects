import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Scanner;

//A class for parsing the HTTP request from the client and seeing if they want a HTTP connection or an upgraded WebSocket
public class HttpRequest {
    String fileName_ = "";
    HashMap<String, String> requestHeader_ = new HashMap<>();

    //Constructor for reading the HTTP request from the client - get the input stream from the socket, use a scanner to read the input, parse
    //the first line of the request, read the headers until an empty line is encountered and then break. Split the header into key and value
    //and store the header in the map.
    HttpRequest(Socket socket) throws IOException {
        InputStream read = socket.getInputStream();
        Scanner scanner = new Scanner(read);
        this.parseFirstLine(scanner.nextLine());

        while (scanner.hasNextLine()) {
            String request = scanner.nextLine();
            if (request.matches("\\s*$"))
                break;
            String key = request.split(": ")[0];
            String value = request.split(": ")[1];
            requestHeader_.put(key, value);
            System.out.println(key + ": " + value);
        }
    }

    //Get the requested file name
    public String getFileName() {
        return fileName_;
    }

    //Parse the first line of the request to determine the requested file and set a default page if none is requested
    private void parseFirstLine(String firstLine) {
        System.out.println(firstLine);
        fileName_ = firstLine.split(" ")[1];
        if (fileName_.equals("/")) {
            fileName_ = "chat.html";
        }
    }

    //Check if a specific header exists in the request
    public boolean hasHeader(String header) {
        return requestHeader_.containsKey(header);
    }

    //Get the value of a specific header
    public String getHeader(String header) {
        return requestHeader_.get(header);
    }
}

