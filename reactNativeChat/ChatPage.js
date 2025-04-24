import {useState} from 'react';
import {View, Text, TextInput, Button, StyleSheet, ScrollView} from 'react-native';

export default function ChatPage({logInOut, messageLog, ws, joinedRoom}) {
    const [message, setMessage] = useState('');
    
    function handleSendMessage(){
        if (message) {
            ws.current.send(`message ${message}`);
            setMessage('');
        }
    }

    function handleLeave(){
        ws.current.send("leave");
        logInOut();
    }

    return(
        <View style = {styles.chatRoom}>
            <View>
                <Text style = {styles.roomHeader}>{joinedRoom}</Text>
            </View>
            <ScrollView style = {styles.messages}> 
                {messageLog.map((message, index) => <Text key={index}>{message}</Text>)}
            </ScrollView>
            <View>
                <TextInput defaultValue = {message} type = "text" placeholder = "Type message here..." onChangeText = {newMessage => setMessage(newMessage)}/>
                <Button title = "Send" onPress = {handleSendMessage}/>
                <Button title = "Leave" onPress = {handleLeave}/>
            </View>
        </View>
    );
}

const styles = StyleSheet.create({
    messages: {
        padding: 10,
        width: 300,
        height: 400,
        borderWidth: 1,     
        borderColor: '#ccc',
        borderRadius: 10,
        backgroundColor: '#f9f9f9',
        marginHorizontal: 'auto',
        overflow: 'hidden',
        shadowColor: '#000',
        shadowOffset: {
            width: 0,
            height: 4,
        },
        shadowOpacity: 0.1,
        shadowRadius: 10,
        elevation: 2,
    },
    chatRoom: {
        marginHorizontal: 'auto',
        padding: 20,
        backgroundColor: '#ffffff',
        borderRadius: 10,
        shadowColor: '#000',
        shadowOffset: {
            width: 0,
            height: 4,
        },
        shadowOpacity: 0.1,
        shadowRadius: 15,
        elevation: 3,
    },
    roomHeader: {
        marginHorizontal: 'auto',
        maxWidth: 700,
        padding: 20,
        backgroundColor: '#ffffff',
        borderRadius: 15,
        alignItems: 'center',
    },
    headerText: {
        fontSize: 18,
        textAlign: 'center',
        fontFamily: 'Arial',
    },
});