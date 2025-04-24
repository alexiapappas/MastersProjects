import LoginPage from './LoginPage';
import ChatPage from './ChatPage';
import { useState, useRef, useEffect } from 'react';
import './App.css';
import './ChatPage.css';

function App() {
  const [loginStatus, setLoginStatus] = useState(false);
  const [userName, setUserName] = useState("");
  const [roomName, setRoomName] = useState("");
  const [messageLog, setMessageLog] = useState([]);
  let joinedRoom = useRef(null);
  const ws = useRef(null);
  let isConnected = false;

  useEffect(() => {
    ws.current = new WebSocket("ws://localhost:8080");
    
    ws.current.onopen = () => {
        isConnected = true;
        console.log("Connection to WebSocket established.")
    };

    ws.current.onmessage = (messageEvent) => {
        const info = JSON.parse(messageEvent.data);
        console.log(info)
        if (info.type === "message"){
            setMessageLog((prevLog) => [...prevLog, `${info.user}: ${info.message}`])
        }

        ws.current.onclose = (event) => {
          console.log(event);
        }
    };
  }, []);

  function logInOut(name = "", room = "") {
    if (name && room) {
      setUserName(name);
      setRoomName(room);
      setLoginStatus(true);
      ws.current.send(`join ${name} ${room}`)
      joinedRoom = {room}
    } else {
      setLoginStatus(false);
      setUserName("");
      setRoomName("");
    }
  }

  return (
    <div className="App">
      <header className="header">
        <p> Chat App </p>
      </header>
      <div>
        {loginStatus ? 
        <ChatPage logInOut = {logInOut} userName = {userName} roomName = {roomName} messageLog={messageLog} ws={ws} joinedRoom={roomName}/> :
        <LoginPage logInOut = {logInOut}/>
        }
      </div>
    </div>
  );
}

export default App;