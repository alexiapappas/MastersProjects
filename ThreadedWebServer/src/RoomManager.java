import java.net.Socket;
import java.security.InvalidParameterException;
import java.util.ArrayList;

//Class for managing chat rooms and client connections
public class RoomManager {
    static ArrayList<Room> Rooms = new ArrayList<>();

    //Retrieves an existing room by name or creates a new room if it doesn't exist
    public static Room getRoom(String roomName, Socket client) throws InvalidParameterException {
        for (Room room : RoomManager.Rooms) {
            if (roomName.equals(room.getRoomName())) {
                room.connectClient(client);
                return room;
            }
        }
        Room newRoom = new Room(roomName);
        Rooms.add(newRoom);
        newRoom.connectClient(client);

        return newRoom;
    }
}
