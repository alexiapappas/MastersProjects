import React from 'react';

function InputWidget({label, placeholder, inputRef}) {
    
    return (
        <div>
            <label> {label}</label>
            <input type = "text" ref = {inputRef} placeholder = {placeholder} />
        </div>
    )
}

export default InputWidget