/*
 * Copyright 2014 Thierry Wasylczenko
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

function requestCurrentQuizz() {
    var request = "{ \"service\" : \"slideshowfx.quizz.current\", \"data\" : {} }";
    socket.send(request);
}

function manageGetCurrentQuizz(data) {

    var quizzDiv = document.getElementById("quizz-container");

    if(undefined != data) {

        var answerType = data.correctAnswers > 1 ? "checkbox" : "radio";
        var quizzElement = "<div id=\"" + data.id + "\">";
        quizzElement += "<p><strong>Question :</strong> <span class=\"quizz-question\">" + data.question.text + "</span></p>";

        for(var answerJson in data.answers) {
            answerJson = data.answers[answerJson];
            quizzElement += "<input type=\"" + answerType + "\" value=\"" + answerJson.id + "\" name=\"answers\" class=\"quizz-answer\">" + answerJson.text + "</input><br />";
        }

        var cookieQuizzAnswered = getCookie('quizz-' + data.id);
        if(cookieQuizzAnswered === "" && cookieQuizzAnswered != 'answered') {
            quizzElement += "<button id=\"answer-quizz-button\" onclick=\"sendQuizzAnswer();\" class=\"custom-button width-to-container\">Answer !</button>";
        } else {
            quizzElement += "<button class=\"custom-button width-to-container\" onclick=\"javascript:alert('Do not try to cheat :)');\">You have already answered this quizz !</button>";
        }

        quizzElement += "</div>";

        quizzDiv.innerHTML = quizzElement;
    }
}

function sendQuizzAnswer() {
    var answers = document.querySelectorAll('.quizz-answer');
    var quizzId = document.querySelector('#quizz-container>div').id;

    var json = '{ \"quizzId\" : ' + quizzId + ', \"answers\": [';
    var answersId = [];

    for(var buttonIndex in answers) {
        if(answers[buttonIndex].checked) {
            answersId[answersId.length] = Number(answers[buttonIndex].value);
        }
    }

    json += answersId.toString() + '] }';

    var url = "http://${slideshowfx_server_ip}:${slideshowfx_server_port}/slideshowfx/quizz/" + Number(quizzId) + "/answer";

    var request = new XMLHttpRequest();
    request.onreadystatechange = function() {
        // The request is finished
        if(request.readyState == 4) {
            var quizzDiv = document.getElementById('quizz-container');
            if(request.status == 200) {
                quizzDiv.innerHTML = 'Thank you for your participation';

                // Add a cookie that indicates if the quizz has been answered
                document.cookie = "quizz-" + quizzId + "=answered";
            } else {
                quizzDiv.innerHTML = 'An error occured. Try again';
            }
        }
    };

    request.open("POST", url, false);
    request.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
    request.send("answer=" + window.btoa(unescape(encodeURIComponent(json))));
}

function manageQuizzStarted(data) {
    var decodedQuizz = decodeURIComponent(escape(window.atob(data["encoded-quizz"])));
    manageGetCurrentQuizz(JSON.parse(decodedQuizz));
}

function manageQuizzStopped(data) {
    var quizzDiv = document.getElementById("quizz-container");

    if(undefined != data) {
        quizzDiv.innerHTML = data.message;
    }
}