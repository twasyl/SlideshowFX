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

function quiz(src, encodedQuiz) {
    if(sfxServer) {
        var icon = src.querySelector('i');
        var quizIsStopped = icon.className.indexOf('fa-play') != -1;

        if(quizIsStopped) {
            var data = '{ "service" : "slideshowfx.quiz.start", "data" : { "encoded-quiz" : "' + encodedQuiz + '" } }';
            sfxServer.callService(data);

            icon.className = 'fa fa-stop fa-fw';
            icon.title = 'Stop the quiz';
        } else {
            var decodedQuiz = decodeURIComponent(escape(window.atob(encodedQuiz)));
            var data = '{ "service" : "slideshowfx.quiz.stop", "data" : { "id" : ' + JSON.parse(decodedQuiz).id + ' } }';
            sfxServer.callService(data);

            icon.className = 'fa fa-play fa-fw';
            icon.title = 'Start the quiz';
        }
    }
}