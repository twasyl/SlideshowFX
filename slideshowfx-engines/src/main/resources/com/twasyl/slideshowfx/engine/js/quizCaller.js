function quiz(src, encodedQuiz) {
    if (sfxServer) {
        var quizIsStopped = src.className === 'start-quiz';

        if (quizIsStopped) {
            var sibling = src.parentElement.querySelector("span.stop-quiz");
            var data = '{ "service" : "slideshowfx.quiz.start", "data" : { "encoded-quiz" : "' + encodedQuiz + '" } }';
            sfxServer.callService(data);
        } else {
            var sibling = src.parentElement.querySelector("span.start-quiz");
            var decodedQuiz = decodeURIComponent(escape(window.atob(encodedQuiz)));
            var data = '{ "service" : "slideshowfx.quiz.stop", "data" : { "id" : ' + JSON.parse(decodedQuiz).id + ' } }';
            sfxServer.callService(data);
        }

        src.style.display = "none";
        sibling.style.display = "inline";
    }
}