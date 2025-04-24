import React from 'react';
import {Text, TextInput, View } from 'react-native';

function InputWidget({ labelName, placeholder, inputValue, valueSetter}) {
    
    return (
        <View>
            <Text>{labelName}</Text>
            <TextInput value = {inputValue} onChangeText = {valueSetter} placeholder = {placeholder} />
        </View>
    )
}

export default InputWidget