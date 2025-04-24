import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Scanner;

public class ServerFunctions {

    public static String parseInput(Socket socket) throws IOException {
        InputStream reader = socket.getInputStream();
        Scanner scanInput = new Scanner(reader);
        String clientRequest = scanInput.nextLine();
        String fileName = clientRequest.split(" ")[1];
        return fileName;
    }

    public static String readfile(File resource) throws FileNotFoundException {
        Scanner inStream = new Scanner(resource);
        String fileContent = inStream.nextLine();
        while (inStream.hasNext()) {
            fileContent = fileContent + inStream.nextLine();
        }
        return fileContent;
    }

    public static String createResponse(Socket socket) throws IOException {
        String requestedFile = parseInput(socket);

        if (requestedFile.equals("/"))
            requestedFile = "index.html";

        File file = new File("Resources" + requestedFile);

        if (file.exists()) {
            String content = readfile(file);
            String httpResponse =
                    """
                    HTTP/1.1 200 OK
                    Content-Type: text/html
                    Connection: close\r\n
                    """;

            String response = httpResponse + content;
            return response;
        }

        String httpResponse =
                """
                HTTP/1.1 404 File Not Found
                Connection: close\r\n
                File not found. 
                """;

        return httpResponse;
    }

    static void writeResponse(Socket socket, String response) {
        // Try writing 3 times
        for (int i=0; i < 3; i++){
            try {
                System.out.println(response);
                socket.getOutputStream().write(response.getBytes());
                break;
            } catch (IOException e) {
                if (i == 2) {
                    System.out.println("Unable to provide client with response");
                    e.printStackTrace();
                }
            }
        }
        try {
            socket.getOutputStream().flush();
            socket.close();
        }
        catch (IOException e){
            System.out.println(String.format("Unable to close {}:{} socket", socket.getInetAddress(), socket.getPort()));
            e.printStackTrace();
        }
    }
}
