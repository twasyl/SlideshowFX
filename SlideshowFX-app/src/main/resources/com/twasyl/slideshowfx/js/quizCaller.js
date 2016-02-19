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