var stompClient = null;
var senderId = '6605a0c9d4eb89144cd0603f'; // Placeholder for sender ID
var chatId = '2deb71d7-1519-4052-9863-01bc0a7379a3'; // Placeholder for chat ID

function connect() {
    var socket = new SockJS('http://localhost:8080/api/ws'); // Update the URL to match your server configuration
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        console.log('Connected: ' + frame);
        stompClient.subscribe('/topic/chat/' + chatId, function (message) {
            showMessage(JSON.parse(message.body));
        });
    });
}

function sendMessage() {
    var messageContent = document.getElementById('messageInput').value.trim();
    if (messageContent && stompClient) {
        var chatMessage = {
            sender: senderId,
            chatId: chatId,
            content: messageContent
        };
        stompClient.send("/app/chat/" + chatId, {}, JSON.stringify(chatMessage));
        document.getElementById('messageInput').value = '';
    }
}

function showMessage(message) {
    var messages = document.getElementById('messages');
    var messageElement = document.createElement('div');
    messageElement.innerHTML = `<b>${message.sender}</b>: ${message.content}`;
    messages.appendChild(messageElement);
    messages.scrollTop = messages.scrollHeight; // Scroll to the bottom
}

window.addEventListener('load', connect);
