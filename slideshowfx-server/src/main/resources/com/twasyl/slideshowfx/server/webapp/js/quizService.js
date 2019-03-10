function getNoQuizActiveHTML() {
    return '<span id="no-quiz-container" class="width-to-container fa-stack fa-2x"><i class="fas fa-question fa-stack-1x fa-fw"></i><i class="fas fa-ban fa-stack-2x fa-fw"></i></span>';
}

function requestCurrentQuiz() {
    var request = "{ \"service\" : \"slideshowfx.quiz.current\", \"data\" : {} }";
    socket.send(request);
}

function manageGetCurrentQuiz(data) {

    if (undefined != data) {

        var answerType = data.correctAnswers > 1 ? "checkbox" : "radio";
        var quizElement = "<div id=\"" + data.id + "\">";
        quizElement += "<p><strong>Question :</strong> <span class=\"quiz-question\">" + data.question.text + "</span></p>";

        for (var answerJson in data.answers) {
            answerJson = data.answers[answerJson];
            quizElement += "<input type=\"" + answerType + "\" value=\"" + answerJson.id + "\" name=\"answers\" class=\"quiz-answer\">" + answerJson.text + "</input><br />";
        }

        var cookieQuizAnswered = getCookie('quiz-' + data.id);
        if (cookieQuizAnswered === "" && cookieQuizAnswered !== 'answered') {
            quizElement += "<button id=\"answer-quiz-button\" onclick=\"sendQuizAnswer();\" class=\"custom-button width-to-container\">Answer !</button>";
        } else {
            quizElement += "<button class=\"custom-button width-to-container\" onclick=\"javascript:alert('Do not try to cheat :)');\">You have already answered this quiz !</button>";
        }

        quizElement += "</div>";

        var quizDiv = document.getElementById("quiz-container");
        quizDiv.innerHTML = quizElement;
    }
}

function sendQuizAnswer() {
    var answers = document.querySelectorAll('.quiz-answer');
    var quizId = document.querySelector('#quiz-container>div').id;

    var json = '{ \"quizId\" : ' + quizId + ', \"answers\": [';
    var answersId = [];

    for (var buttonIndex in answers) {
        if (answers[buttonIndex].checked) {
            answersId[answersId.length] = Number(answers[buttonIndex].value);
        }
    }

    json += answersId.toString() + '] }';

    var url = "http://${slideshowfx_server_ip}:${slideshowfx_server_port}/slideshowfx/quiz/" + Number(quizId) + "/answer";

    var request = new XMLHttpRequest();
    request.onreadystatechange = function() {
        // The request is finished
        if (request.readyState == 4) {
            var quizDiv = document.getElementById('quiz-container');
            if (request.status == 200) {
                quizDiv.innerHTML = 'Thank you for your participation';

                // Add a cookie that indicates if the quiz has been answered
                document.cookie = "quiz-" + quizId + "=answered";
            } else {
                quizDiv.innerHTML = 'An error occurred. Try again';
            }
        }
    };

    request.open("POST", url, false);
    request.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
    request.send("answer=" + window.btoa(unescape(encodeURIComponent(json))));
}

function manageQuizStarted(data) {
    var decodedQuiz = decodeURIComponent(escape(window.atob(data["encoded-quiz"])));
    manageGetCurrentQuiz(JSON.parse(decodedQuiz));
}

function manageQuizStopped(data) {
    var quizDiv = document.getElementById("quiz-container");
    quizDiv.innerHTML = getNoQuizActiveHTML();
}