import LoginPage from './LoginPage';
import ChatPage from './ChatPage';
import {useState, useRef, useEffect} from 'react';
import {View, Text, StyleSheet} from 'react-native';

export default function App() {

  const styles = StyleSheet.create({
    homePage: {
      marginHorizontal: 'auto',
      maxWidth: 700,
      padding: 20,
      backgroundColor: '#ffffff',
      borderRadius: 15,
      shadowColor: '#000',
      shadowOffset: {
        width: 0,
        height: 6,
      },
      shadowOpacity: 0.1,
      shadowRadius: 20,
      elevation: 5,
      alignItems: 'center',
      textAlign: 'center',
      fontfamily: 'Arial',
    },
    button: {
      borderRadius: 5,
      paddingVertical: 10,
      paddingHorizontal: 20,
      alignItems: 'center',
      elevation: 3,
    },
    buttonText: {
      color: '#ffffff',
      fontSize: 16,
    }
    }); 

  const [loginStatus, setLoginStatus] = useState(false);
  const [userName, setUserName] = useState("");
  const [roomName, setRoomName] = useState("");
  const [messageLog, setMessageLog] = useState([]);
  const ws = useRef(null);
  let joinedRoom = useRef(null);
  let isConnected = false;

  useEffect(() => {
    ws.current = new WebSocket("ws://10.0.2.2:8050");
    
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
    }

    ws.current.onclose = (event) => {
          isConnected = false;
          ws.current.close();
          console.log("Connection to WebSocket closed");
    }
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
    <View styles={styles.homePage}>
      <Text styles={styles.ChatPage}> Chat App </Text>
      <View>
        {loginStatus ? 
        <ChatPage logInOut = {logInOut} userName = {userName} roomName = {roomName} messageLog={messageLog} ws={ws} joinedRoom={roomName}/> :
        <LoginPage logInOut = {logInOut}/>
        }
      </View>
    </View>
  );
}