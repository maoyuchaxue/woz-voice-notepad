
var data = {
    msgs: []
}

var socket = null;

function selectText() {
    if (document.selection) {
        return document.selection.createRange().text;     	 
    } else {    
        return window.getSelection().toString();	 
    }
}

receive_user_msg = function(user_msg) {
    console.log("received " + user_msg);
    if (user_msg.length > 0) {
        data.msgs.push(user_msg);
    }
}

disconnect_from_server = function() {
    console.log("disconnected");
}

clear_msg = function() {
    data.msgs = [];
}

send_read_result = function() {
    msg = selectText();
    if (msg.length == 0) {
        return ;
    }
    console.log('sending: ', msg);
    socket.emit('read_res', msg);
}

copy_to_clipboard = function() {
    document.execCommand('Copy');
}

index = function() {
    var vm = new Vue({
        el: '#root',
        data: data,
        methods: {
            sendReadResult: send_read_result,
            copyToClipboard: copy_to_clipboard
        },
    })
    
    socket = io();
    socket.on('text', receive_user_msg);
    socket.on('disconnect', disconnect_from_server);
}

