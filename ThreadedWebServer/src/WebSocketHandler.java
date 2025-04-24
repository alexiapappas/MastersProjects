import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import static java.lang.Math.pow;

//Class responsible for handling WebSocket connections and communication
public class WebSocketHandler {
    Room room_;
    String user_;
    Socket socket_;

    //Constructor that initialized the WebSocketHandler with a socket
    WebSocketHandler(Socket socket) {
        socket_ = socket;
    }

    //Generates a secret key for WebSocket connecting using SHA-1 hashing
    public static String generateSecretKey(String key) throws NoSuchAlgorithmException, IOException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        return Base64.getEncoder().encodeToString(md.digest((key + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11").getBytes()));
    }

    //Starts the WebSocket connection and handles incoming messages
    public void startWSConnection(Socket socket) throws IOException {
        DataInputStream in = new DataInputStream(socket.getInputStream());
        boolean connected = true;

        //Continuously listen for incoming messages
        while (connected) {
            //Check if there are bytes available to red
            if (in.available() > 0) {
                String message;
                try {
                    message = readFrame(in);
                    System.out.println("RECIEVED: " + message);
                    System.out.println(this.user_);
                    System.out.println(this.room_);
                } catch (Exception e){
                    System.out.println("Failed to read frame");
                    continue;
                }
                //Split the incoming messages into command and content
                String [] pieces = message.split(" ", 2);
                String type = pieces[0];
                //Handle different message types
                //Get or create room, create join response message and send to client
                if (type.equals("join")){
                    String[] userAndRoom = pieces[1].split(" ");
                    String roomName = userAndRoom[1];
                    user_ = userAndRoom[0];
                    room_ = RoomManager.getRoom(roomName, socket);
                    Message joinResponse = new Message("join", user_, room_.getRoomName());
                    sendClientMessage(socket_, joinResponse.toString(), false);
                    room_.addMessage(joinResponse.toString());

                //Create leave message, remove client from the room and send leave response
                } else if (type.equals("leave")){
                    Message leaveResponse = new Message("leave");
                    room_.removeClient(socket);
                    sendClientMessage(socket_, leaveResponse.toString(), true);
                //Create the message, iterate through connected clients and send messge to each client
                } else if (type.equals("message")){
                    Message sendMessage = new Message("message", user_, room_.getRoomName(), pieces[1]);
                    for (Socket client : room_.getClients()){
                        sendClientMessage(client, sendMessage.toString(), false);
                        room_.addMessage(sendMessage.toString());
                    }
                }
            }
        }
    }

    //Reads a frame from the input stream
    static String readFrame(DataInputStream in) throws Exception {
        //Read the first two bytes for the frame header
        byte[] header = in.readNBytes(2);

        //Extract the FIN bit and opcode from the header, close connection if opcode indicates
        boolean finBit = (header[0] & 0x80) > 0;
        int opcode = header[0] & 0x0F;
        if (opcode == 0x8) {
            throw new Exception("connection closed");
        }

        //Check if the message is masked
        boolean masked = (header[1] & 0x80) != 0;

        //Get the payload length
        long len = header[1] & 0x7F;

        //Determine the payload length based on the value
        if (len == 126) {
            len = in.readUnsignedShort();
        }
        else if (len == 127) {
            len = in.readLong();
        }

        //Prepare a mask array and read the mask if the message is masked
        byte[] mask = new byte[4];
        if (masked){
            mask = in.readNBytes(4);
        }

        //Read the actual message bytes
        byte[] message = in.readNBytes((int) len);

        //Unmask the message if it was masked
        if (masked) {
            for (int i = 0; i < len; i++) {
                message[i] ^= mask[i % 4];
            }
        }

        //Return the message as a string
        return new String (message);
    }

    //Creates a WebSocket frame from a payload
    public static byte[] createFrame(String payload, boolean close) throws IOException {
        //Create output stream for the frame
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte FIN_Opcode;

        //Determine the FIN bit and opcode based on whether it's a closing frame
        if(!close){
            FIN_Opcode = (byte)0x81;
        } else {
            FIN_Opcode = (byte)0x80;
        }
        outStream.write(FIN_Opcode);

        //Only returns int. Should I ever expect longer text as the frame allows?
        //Java strings can only hold the same amount of chars as Integer.MAX_VALUE
        //Get the length of the payload
        int payLen = payload.length();

        //Write the length of the payload
        if (payLen <= 125){
            outStream.write((byte)payLen);
        } else if (payLen <= pow(2, 16) - 1) {
            outStream.write((byte)126);
            outStream.write((short)payLen);
        } else {
            outStream.write((byte)127);
            outStream.write(0); // Write zero first to fill 64 bit extended length
            outStream.write(payLen); // remaining 32 bits
        }

        //Write the payload bytes to the output stream
        outStream.write(payload.getBytes());

        //Return the byte array representation of the frame
        return outStream.toByteArray();
    }

    //Sends a message to a client over the WebSocket connection
    public void sendClientMessage(Socket socket, String message, boolean close) throws IOException {
        HttpResponse.writeResponse(socket, createFrame(message, false));
    }
}
