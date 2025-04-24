let x = document.getElementById("xVal");
let y = document.getElementById("yVal");
let submit = document.getElementById("Calculate");
let result = document.getElementById("Result");

let ws = new WebSocket("ws://localhost:8080");

let isConnected = false;

ws.onopen = function(){
    isConnected = true;
    console.log("Connection established");
};

ws.onmessage = function(messageEvent) {
    result.textContent = "Result = " + messageEvent.data;
};

ws.onerror = function(e) {};

ws.onclose = function (e) {
    console.log("Connection closed");
};

submit.addEventListener("click", function () {});

submit.addEventListener("click", function () {
    let xValue = Number(x.value);
    let yValue = Number(y.value);

    if (!(isNaN(xValue) || isNaN(yValue))) {
        fetch("https://localhost:8080/calculate?x=" + x.value + "&y=" + y.value) //this is a task to go to the webserver and get something
            .then(response => { //the '.then' means the promise (which is the fetch) has been fulfilled
                if (!response.ok) { //either you could not find the server or something else bad happened
                    console.log("There was an error");
                }
                return response.text();
            }) .then(data => {
                alert(data);
                result.textContent = "Result = " + data;
        });
        if (isConnected){
            ws.send(xValue + " " + yValue);
        }
    }
});

// submit.addEventListener("click", function() {
//     let ajaxRequest = new XMLHttpRequest();
//     let xValue = Number(x.value);
//     let yValue = Number(y.value);
//
//     if(!(isNaN(xValue) || isNaN(yValue))) {
//         ajaxRequest.open("GET", "https://localhost:8080/calculate?x=" + x.value + "&y=" + y.value);
//         ajaxRequest.addEventListener("load", function() {
//             result.textContent = "Result = " + this.responseText; //'this' is the ajaxRequest
//         });
//
//         ajaxRequest.addEventListener("error", function() {
//             console.log(this.response);
//         });
//     } else {
//         alert("Please only enter numbers")
//     }
//     ajaxRequest.send();
// });