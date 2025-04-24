import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;

//Class for writing back the response to the user after initial connection request
public class HttpResponse {

    //Reads the contents of the requested file into a byte array
    static byte[] readFile(String requestedFile) throws IOException {
        return Files.readAllBytes(Paths.get("Resources/" + requestedFile));
    }

    //Determines the content type based on the file extension
    public static String determineContentType(String resourceName) {
        if (resourceName.endsWith(".html"))
            return "text/html";
        if (resourceName.endsWith(".css"))
            return "text/css";
        if (resourceName.endsWith(".js"))
            return "application/javascript";
        if (resourceName.endsWith(".png"))
            return "image/png";
        if (resourceName.endsWith(".jpg") || resourceName.endsWith(".jpeg"))
            return "image/jpeg";
        // Default to plain text for unknown extensions
        return "text/plain";
    }

    //Creates an HTTP response based on the request and file name. Handles WebSocket upgrade requests as well as standard file responses.
    public static byte[] createResponse(HttpRequest requests, String fileName) throws IOException, NoSuchAlgorithmException {
        String response;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        //Check for WebSocket upgrade request
        if (requests.hasHeader("Sec-WebSocket-Key")) {
            String key = requests.getHeader("Sec-WebSocket-Key");
            String secret = WebSocketHandler.generateSecretKey(key);
            response = String.format("""
                    HTTP/1.1 101 Switching Protocols
                    Upgrade: websocket
                    Connection: Upgrade
                    Sec-WebSocket-Accept: %s\r\n
                    """, secret);
            System.out.println(response);
            baos.write(response.getBytes());
            return baos.toByteArray();
        }
        else {
            //Handle regular HTTP requests
            String contentType = determineContentType(fileName);
            String requestedFile = requests.getFileName();
            try {
                byte[] fileBytes = readFile(requestedFile);
                response = String.format("""
                        HTTP/1.1 200 OK
                        Content-Type: %s
                        Connection: close
                        Content-Length: %d\r\n
                        """, contentType, fileBytes.length);
                System.out.println(response);
                baos.write(response.getBytes());
                baos.write(fileBytes);
                return baos.toByteArray();
            } catch (IOException e) {
                e.printStackTrace();
                response = """
                        HTTP/1.1 404 File Not Found
                        Content-Type: %s
                        Connection: close\r\n
                        """;
                baos.write(response.getBytes());
                return baos.toByteArray();
            }
        }
    }


    //Writes the HTTP response back to the client via the socket
    static void writeResponse(Socket socket, byte[] response) throws IOException {
        OutputStream os = socket.getOutputStream();
        os.write(response);
        os.flush();
    }
}
