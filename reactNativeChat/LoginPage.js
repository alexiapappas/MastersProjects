import {useState} from 'react';
import {View, Button, StyleSheet} from 'react-native';
import InputWidget from './InputWidget';

export default function LoginPage({logInOut}) {
    const styles = StyleSheet.create({
        homePage: {
          marginHorizontal: 'auto',
          maxWidth: 700,
          padding: 20,
        },
        button: {
          borderRadius: 5,
          paddingVertical: 10,
          paddingHorizontal: 20,
          alignItems: 'center',
          elevation: 3,
          color: '#ffffff'
        },
        buttonText: {
          color: '#ffffff',
          fontSize: 16,
        },
        textBox: {
            border: 'black',
            borderWidth: 1,
            padding: 3,
        }
        });

    const [userName, setUserName] = useState('');
    const [roomName, setRoomName] = useState('');

    function handleJoinRoom() {

        console.log(userName);
        console.log(roomName);

        if (userName && roomName){
            logInOut(userName, roomName);
        } else {
            alert("Please enter both username and room name")
        }
    }

    return (
        <View style = {styles.homePage}>
            <InputWidget  labelName="Username: " placeholder="Enter user name" inputValue={userName} valueSetter={setUserName} />
            <InputWidget labelName="Room Name: " placeholder="Enter room name" inputValue={roomName} valueSetter={setRoomName} />
            <Button title = "Join Room" onPress = {handleJoinRoom}/> 
        </View>
    );
}