import { useState, useRef } from "react";
import InputWidget from "./InputWidget";

function LoginPage({logInOut}) {
    const userText = useRef();
    const roomText = useRef();

    function handleJoinRoom() {
        logInOut();
        const userName = userText.current.value;
        const roomName = roomText.current.value;

        if (userName && roomName){
            logInOut(userName, roomName);
        } else {
            alert("Please enter both username and room name")
        }
    }

    return (
        <div id = "homePage">
            <InputWidget label = "Username: " inputRef = {userText} />
            <InputWidget label = "Room Name: " inputRef = {roomText} />
            <button id = "Join" onClick = {handleJoinRoom}> Join Room </button>
        </div>
    );
}

export default LoginPage;