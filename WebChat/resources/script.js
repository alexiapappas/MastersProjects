let name_ = document.getElementById("uName");
let room_ = document.getElementById("roomName");
let join = document.getElementById("Join");
let send = document.getElementById("Send");
let leave = document.getElementById("Leave");
let signInBlock = document.getElementById("homePage");
let chatRoom = document.getElementById("chatRoom");
let message = document.getElementById("messages");
let messageInput = document.getElementById("messageInput");

let ws = new WebSocket("ws://localhost:8080");
let isConnected = false;

ws.onopen = function(){
    isConnected = true;
    console.log("Connection established");
};

ws.onerror = function(e) {
    console.log("WebSocket error: ", e)
};

ws.onclose = function (e) {
    isConnected = false;
    console.log("Connection closed");
};

ws.onmessage = function (messageEvent) {
    let info = JSON.parse(messageEvent.data);
    console.log(info);

    switch (info.type) {
        case "message":
            let newMessage = document.createElement("p");
            newMessage.textContent = info.user + ": " + info.message;
            message.appendChild(newMessage);
            break;
        case "join":
            signInBlock.style.display = "none";
            chatRoom.style.display = "block";
            room_.value = null;
            name_.value = null;
            break;
    }
}

function isLower(myString){
    return !myString.match(".*[^a-z].*");
}

join.addEventListener("click", function () {
    let name = name_.value;
    let room = room_.value;

    if ((!(isLower(name))) || (!(isLower(room)))){
        alert("Invalid: the username and room name must be comprised of only lowercase letters")
    } else if ((name === "") || (room === "")){
        alert("Invalid: the username and room cannot be empty")
    } else {
        ws.send("join " + name + " " + room);
        console.log(name + " joined the chat: " + room);
    }
});

function sendMessage(){
    const message = messageInput.value;

    if (message){
        ws.send("message " + message);
        messageInput.value = ""; //clear the input after sending
    } else {
        alert("Please enter a message to send.");
    }
}

send.addEventListener("click", sendMessage);

leave.addEventListener("click", function () {
    ws.send("leave");
    signInBlock.style.display = "block";
    chatRoom.style.display = "none";
    while(message.firstChild)
        message.removeChild(message.firstChild);
});