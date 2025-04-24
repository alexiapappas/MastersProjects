import com.google.gson.Gson;

//Class representing a message with type, user, room, and content
public class Message {
    private String type;
    private String user;
    private String room;
    private String message;

    //Constructor for creating a Message with all properties
    Message(String type, String user, String room, String message) {
        setBaseVars(type, user, room);
        this.message = message;
    }

    //Constructor for creating a Message without a message content
    Message(String type, String user, String room) {
        setBaseVars(type, user, room);
        this.message = null; // Construct with omission of message
    }

    //Constructor for creating a Message with only a type
    Message(String type) {
        setBaseVars(type, null, null);
    }

    //Helper method to set common base properties for the Message
    private void setBaseVars(String type, String user, String room) {
        this.type = type;
        this.user = user;
        this.room = room;
    }

    //Converts the Message object to its JSON representation
    public String toString(){
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
