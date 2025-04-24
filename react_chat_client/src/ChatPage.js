import { useEffect, useRef, useState } from "react";

function ChatPage({logInOut, userName, roomName, messageLog, ws, joinedRoom}) {
    const messageInput = useRef();
    
    function handleSendMessage(){
        const message = messageInput.current.value;
        if (message) {
            ws.current.send(`message ${message}`);
            messageInput.current.value = "";
        }
    }

    function handleLeave(){
        logInOut();
    }

    return(
        <div id = "chatRooom">
            <h1 id = "roomHeader"> {joinedRoom}</h1>
            <div id = "messages"> {
                messageLog.map((message, index) => <p key={index}>{message}</p>)
            }</div>
            <div>
                <input ref = {messageInput} type = "text" placeholder = "Type message here..." />
                <button id = "Send" onClick = {handleSendMessage}> Send </button>
                <button id = "Leave" onClick = {handleLeave}> Leave </button>
            </div>
        </div>
    );
}

export default ChatPage;