function requestChatHistory() {
    var request = "{ \"service\": \"slideshowfx.chat.attendee.history\", \"data\": {} }";
    socket.send(request);
}
/*
 * data represents the JSON object corresponding to the chat message.
 */
 function manageNewChatMessage(data) {
    var messagesDiv = document.getElementById("chat-messages");

    var htmlMessage = "<div class=\"chat-message\" id=\"" + data.id + "\">"
                + "<div id=\"" + data.id + "-check\" style=\"display: none; margin-right: 5px;\">"
                + "<i class=\"fa fa-check-circle-o fw\" style=\"color: green\"></i></div>"
                + "<span class=\"author\">" + data.author + " ";

    if( data.author == "I") {
        htmlMessage = htmlMessage + "say";
    } else {
        htmlMessage = htmlMessage + "said";
    }

    htmlMessage = htmlMessage + " :" + "</span><br />"
                + "<span class=\"message-content\">" + decodeURIComponent(escape(window.atob(data.content))) + "</span></div>";

    messagesDiv.innerHTML = messagesDiv.innerHTML + htmlMessage;

    if("answered" == data.status) {
        htmlMessage = document.getElementById(data.id);
        htmlMessage.className += "  question-answered";

        var imageDiv = document.getElementById(data.id + "-check");
        imageDiv.style.display = "inline";
    }
 }

/*
 * data represents the JSON object corresponding to the chat message.
 */
 function manageUpdateChatMessage(data) {
    var htmlMessage = document.getElementById(data.id);

    if("answered" == data.status) {
        htmlMessage.className += " question-answered";

        var imageDiv = document.getElementById(data.id + "-check");
        imageDiv.style.display = "inline";
    }
 }

 function sendChatMessage() {
     var attendeeMessage = document.getElementById("attendee-message-textarea").value;

     if(attendeeMessage === "") {
         alert("Enter a message");
     } else {
         var attendeeName = getAttendeeNameFromCookie();

         if(attendeeName === "") {
             attendeeName = "somebody";
         }

         var jsonMessage = "{ \"author\": \"" + attendeeName + "\", \"source\" : \"chat\", \"status\" : \"new\", \"content\": \"" + window.btoa(unescape(encodeURIComponent(attendeeMessage))) + "\"}";
         jsonMessage = "{ \"service\" : \"slideshowfx.chat.attendee.message.add\", \"data\" : " + jsonMessage + " }";

         socket.send(jsonMessage);

         document.getElementById("attendee-message-textarea").value = "";
     }
 }

 function displayChatHistory(data) {
    for(index in data) {
        manageNewChatMessage(data[index]);
    }
 }