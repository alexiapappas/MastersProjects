import java.net.Socket;
import java.util.ArrayList;

//Class representing a chat room that holds clients and message history
public class Room {
    private String roomName_;
    private ArrayList<Socket> clients_ = new ArrayList<>();
    private ArrayList<String> messageHistory = new ArrayList<>();

    //Constructor to initialize the room with a name
    Room(String room) {
        roomName_ = room;
    }

    //Add a room to the RoomManager's list of rooms
    public synchronized void addRoom(Room room) {
        RoomManager.Rooms.add(room);
    }

    //Getter method to retrieve room name
    public String getRoomName(){
        return roomName_;
    }

    //Getter method to retrieve the list of clients in the room
    public ArrayList<Socket> getClients(){
        return clients_;
    }

    //Method to connect a new client to the room
    public void connectClient(Socket client) {
        clients_.add(client);
    }

    //Method to remove a client from the room
    public void removeClient(Socket client) {
        clients_.remove(client);
    }

    //Method to add a message to the room's message history
    public void addMessage(String message) {
        messageHistory.add(message);
    }
}