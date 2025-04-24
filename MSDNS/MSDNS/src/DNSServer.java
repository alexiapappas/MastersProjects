import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;


public class DNSServer {
    static final int server_port = 5678;
    static final String google_Forwarding = "8.8.8.8";
    static final int dns_port = 53;
    private static final DNSCache cache = new DNSCache();


    public static void main(String[] args) throws IOException {
        System.out.println("Listening at " + server_port);
        DatagramSocket serverSocket = new DatagramSocket(server_port);

        // Create a buffer to receive incoming packets - the max size of a DNS request is 512
        byte[] buffer = new byte[512];
        DatagramPacket clientPacket = new DatagramPacket(buffer, buffer.length);

        while (true) {
            serverSocket.receive(clientPacket);

            processRequest(serverSocket, clientPacket);
        }
    }


    private static void processRequest(DatagramSocket serverSocket, DatagramPacket clientPacket) throws IOException {
        // Extract received data packet
        byte[] requestData = clientPacket.getData();

        // Decode DNS message from raw byte array
        DNSMessage requestMessage = DNSMessage.decodeMessage(requestData);

        // Check if this is a DNS response (QR = 1)
        if (requestMessage.isResponse()) {
            processResponse(requestMessage);
            return;
        }

        // Otherwise, process as a query (QR = 0)
        processQuery(serverSocket, clientPacket, requestMessage);
    }


    private static void processResponse(DNSMessage responseMessage) {
        System.out.println("Received a response, processing...");

        // Get questions and answers from the response message
        DNSQuestion question = responseMessage.getQuestions();
        DNSRecord answer = responseMessage.getAnswers();

        // Store answers in the cache if they exist
        if (answer != null) {
            cache.add(question, answer);
        }
    }


    private static void processQuery(DatagramSocket serverSocket, DatagramPacket clientPacket, DNSMessage requestMessage) throws IOException {
        // Extract DNS question from the request
        DNSQuestion question = requestMessage.getQuestions();

        // Look up the first question in the cache
        DNSRecord[] cacheAnswers = new DNSRecord[1];
        cacheAnswers[0] = cache.getQuestion(question);

        // If the answer is found in the cache, return it to the client
        if (cacheAnswers != null) {
            System.out.println("Cache found - returning cached answer");
            sendResponse(serverSocket, clientPacket, requestMessage, cacheAnswers);
            return;
        }

        // If the answer is not found, forward query to Google
        System.out.println("Cache miss - forwarding request to " + google_Forwarding);
        byte[] responseData = forwardToGoogle(requestMessage.toBytes());

        if (responseData == null) {
            sendServFailResponse(serverSocket, clientPacket, requestMessage);
            return;
        }

        // Decode and store response received from Google
        DNSMessage responseMessage = DNSMessage.decodeMessage(responseData);
        processResponse(responseMessage);

        if (cacheAnswers == null || cacheAnswers.length == 0) {
            System.out.println("No answer received - sending NoDomain response");
            sendNoDomainResponse(serverSocket, clientPacket, requestMessage);
            return;
        }

        // Send response back to the original client
        DatagramPacket responsePacket = new DatagramPacket(responseData, responseData.length, clientPacket.getAddress(), clientPacket.getPort());
        serverSocket.send(responsePacket);
    }


    private static void sendServFailResponse(DatagramSocket serverSocket, DatagramPacket clientPacket, DNSMessage requestMessage) throws IOException {
        DNSMessage responseMessage = DNSMessage.buildResponse(requestMessage, new DNSRecord[0]);
        responseMessage.setResponseCode(2); // SERVFAIL RCode = 2

        byte[] responseData = responseMessage.toBytes();
        DatagramPacket responsePacket = new DatagramPacket(responseData, responseData.length, clientPacket.getAddress(), clientPacket.getPort());
        serverSocket.send(responsePacket);
    }


    private static void sendNoDomainResponse(DatagramSocket serverSocket, DatagramPacket clientPacket, DNSMessage requestMessage) throws IOException {
        DNSMessage responseMessage = DNSMessage.decodeMessage(requestMessage.toBytes());
        byte[] responseData = responseMessage.toBytes();
        responseMessage.setResponseCode(3); // NXDOMAIN RCode = 3

        DatagramPacket responsePacket = new DatagramPacket(responseData, responseData.length, clientPacket.getAddress(), clientPacket.getPort());
        serverSocket.send(responsePacket);
    }


    private static byte[] forwardToGoogle(byte[] requestData) throws IOException {
        DatagramSocket socket = new DatagramSocket();
        socket.setSoTimeout(10000); // 10-second timeout

        InetAddress googleDNS = InetAddress.getByName(google_Forwarding);

        // Create a packet with the DNS query to send to Google
        DatagramPacket forwardPacket = new DatagramPacket(requestData, requestData.length, googleDNS, dns_port);
        socket.send(forwardPacket);

        // Buffer to receive Google's response
        byte[] responseBuffer = new byte[512];
        DatagramPacket responsePacket = new DatagramPacket(responseBuffer, responseBuffer.length);

        try {
            socket.receive(responsePacket);
        } catch (SocketTimeoutException e) {
            System.out.println("Google DNS unreachable");
            return null;
        } finally {
            socket.close();
        }

        return responsePacket.getData();
    }


    private static void sendResponse(DatagramSocket serverSocket, DatagramPacket clientPacket, DNSMessage requestMessage, DNSRecord[] answer) throws IOException {
        DNSMessage responseMessage = new DNSMessage().buildResponse(requestMessage, answer);
        byte[] responseData = responseMessage.toBytes();

        // Create a packet containing the response data
        DatagramPacket responsePacket = new DatagramPacket(responseData, responseData.length, clientPacket.getAddress(), clientPacket.getPort());

        // Send the response back to the client
        serverSocket.send(responsePacket);
    }
}