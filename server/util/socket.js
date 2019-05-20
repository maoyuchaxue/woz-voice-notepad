

const net = require('net');
var to_wizard_socket = null;
var to_client_socket = null;

module.exports = {
    init: function() {

        var net_server = net.createServer(function (socket) {

            to_client_socket = socket;
            console.log('user connected');

            if (to_wizard_socket) {
                to_wizard_socket.emit('text', "用户已连接");
            }
                
            socket.on('data', (data) => {
                console.log('device input:' + data.length + " " + data);
                to_wizard_socket.emit('text', data.toString());
            });
            
            socket.on('end', () => {
                console.log('user disconnected');
                
                if (to_wizard_socket) {
                    to_wizard_socket.emit('text', "用户已断开连接");
                }
            });
        
            socket.on('error', () => {
                console.log('socket error');
                if (to_wizard_socket) {
                    to_wizard_socket.emit('text', "用户已断开连接");
                }
            });
        });   
        
        net_server.listen(9000, () => {
            console.log('net server bound');
        });
    },

    wizard_socket: function(socket) {
        to_wizard_socket = socket;
        socket.on('read_res', function(msg) {
            to_client_socket.write(msg + "//");
            console.log( "read res " + msg);
        });

        socket.emit('text', "用户消息在这里显示");
    }
}