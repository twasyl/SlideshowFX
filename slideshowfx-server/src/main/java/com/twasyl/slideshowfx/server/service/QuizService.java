package com.twasyl.slideshowfx.server.service;

import com.twasyl.slideshowfx.server.SlideshowFXServer;
import com.twasyl.slideshowfx.server.beans.quiz.Quiz;
import com.twasyl.slideshowfx.server.beans.quiz.QuizResult;
import com.twasyl.slideshowfx.server.bus.EventBus;
import com.twasyl.slideshowfx.utils.TemplateProcessor;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.ext.web.Router;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.twasyl.slideshowfx.server.SlideshowFXServer.*;
import static com.twasyl.slideshowfx.server.service.IServicesCode.*;

/**
 * This class provides the quiz services.
 *
 * @author Thierry Wasylczenko
 * @version 1.1-SNAPSHOT
 * @since SlideshowFX 1.0
 */
public class QuizService extends AbstractSlideshowFXService {
    private static final Logger LOGGER = Logger.getLogger(QuizService.class.getName());

    public static final String SERVICE_QUIZ_ON_RESULT = "service.quiz.onResult";

    private static final String URL = "/slideshowfx/quiz";
    private Quiz currentQuiz = null;

    /**
     * The results of all quiz. The key of this Map represents the ID of the {@link Quiz}, the value the {@link QuizResult} which
     * contains all correct and wrong answers.
     */
    private final Map<Long, QuizResult> results = new HashMap<>();

    @Override
    public void start() {

        this.updatedRouteMatcher();

        this.register(SERVICE_QUIZ_START, this.buildStartQuizHandler())
                .register(SERVICE_QUIZ_STOP, this.buildStopQuizHandler())
                .register(SERVICE_QUIZ_CURRENT, buildGetCurrentQuizHandler());
    }

    private void updatedRouteMatcher() {
        final Router router = SlideshowFXServer.getSingleton().getRouter();

        // URL for answering a quiz
        router.post(URL.concat("/:quizid/answer")).handler(routingContext -> {
            int statusCode = 500;

            try {
                if (currentQuiz != null && currentQuiz.getId() == Long.parseLong(routingContext.request().getParam("quizid"))) {

                    final String stringAnswer = routingContext.request().getFormAttribute("answer");
                    final JsonObject jsonAnswer = new JsonObject(new String(Base64.getDecoder().decode(stringAnswer)));
                    final JsonArray answersArray = jsonAnswer.getJsonArray("answers");

                    final Long[] answers = new Long[answersArray.size()];
                    int index = 0;
                    for (Object object : answersArray) {
                        answers[index++] = ((Number) object).longValue();
                    }

                    boolean isCorrect = currentQuiz.checkAnswers(answers);
                    final QuizResult result = results.get(currentQuiz.getId());

                    if (result != null) {
                        if (isCorrect) result.addCorrectAnswer();
                        else result.addWrongAnswer();
                    }

                    statusCode = 200;
                } else {
                    statusCode = 406;
                    routingContext.response().setStatusCode(406).end();
                }
            } finally {
                routingContext.response().setStatusCode(statusCode).end();
            }
        });
        // Get the JavaScript resources
        router.get("/slideshowfx/quiz/js/quizService.js").handler(routingContext -> {
            final LocalMap<String, String> templateTokens = this.vertx.sharedData().getLocalMap(SHARED_DATA_TEMPLATE_TOKENS);

            final Configuration configuration = TemplateProcessor.getJsConfiguration(QuizService.class);

            final Map tokenValues = new HashMap();
            tokenValues.put(templateTokens.get(SHARED_DATA_SERVER_HOST_TOKEN), SlideshowFXServer.getSingleton().getHost());
            tokenValues.put(templateTokens.get(SHARED_DATA_SERVER_PORT_TOKEN), SlideshowFXServer.getSingleton().getPort() + "");

            try (final StringWriter writer = new StringWriter()) {
                final Template template = configuration.getTemplate("quizService.js");
                template.process(tokenValues, writer);

                writer.flush();

                routingContext.response().putHeader("Content-Type", "application/javascript").setStatusCode(200).setChunked(true).end(Buffer.buffer(writer.toString()));
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Error when a client tried to access the chat", e);

                routingContext.response().setStatusCode(500).end();
            } catch (TemplateException e) {
                LOGGER.log(Level.WARNING, "Error when processing the chat template", e);
                routingContext.response().setStatusCode(500).end();
            }
        });
    }

    /**
     * Build a handler that will start the quiz.
     *
     * @return The handler for starting a quiz.
     */
    private Handler<Message<JsonObject>> buildStartQuizHandler() {
        return message -> {
            final JsonObject object = message.body();
            final String quizString = new String(Base64.getDecoder().decode(object.getString("encoded-quiz")));

            this.currentQuiz = Quiz.build(quizString);

            // Add the Quiz to the results. If the Quiz already exists, it won't be erased
            if (!this.results.containsKey(this.currentQuiz.getId())) {
                final QuizResult quizResult = new QuizResult();
                quizResult.setQuiz(this.currentQuiz);

                this.results.put(this.currentQuiz.getId(), quizResult);
            }

            final JsonObject encodedQuiz = new JsonObject()
                    .put("encoded-quiz", Base64.getEncoder().encodeToString(this.currentQuiz.toJSON().encode().getBytes()));
            final JsonObject reply = this.buildResponse(SERVICE_QUIZ_START, RESPONSE_CODE_QUIZ_STARTED, encodedQuiz);
            this.sendResponseToWebSocketClients(reply);

            EventBus.getInstance().broadcast(SERVICE_QUIZ_ON_RESULT, this.results.get(this.currentQuiz.getId()));

            message.reply(reply);
        };
    }

    private Handler<Message<JsonObject>> buildStopQuizHandler() {
        return message -> {
            final JsonObject object = message.body();
            final Long quizId = object.getLong("id");

            // Ensure the ID is equal to the current quiz
            if (this.currentQuiz != null && this.currentQuiz.getId() == quizId) {
                this.currentQuiz = null;

                final JsonObject reply = this.buildResponse(SERVICE_QUIZ_STOP, RESPONSE_CODE_QUIZ_STOPPED, "The quiz has been stopped");
                this.sendResponseToWebSocketClients(reply);
            }

            message.reply(this.buildResponse(SERVICE_QUIZ_STOP, RESPONSE_CODE_QUIZ_STOPPED, "Quiz stopped"));
        };
    }

    private Handler<Message<JsonObject>> buildGetCurrentQuizHandler() {
        return message -> {
            if (this.currentQuiz != null) {
                message.reply(this.buildResponse(SERVICE_QUIZ_CURRENT, RESPONSE_CODE_QUIZ_RETRIEVED, this.currentQuiz.toJSON()));
            } else {
                message.reply(this.buildResponse(SERVICE_QUIZ_CURRENT, RESPONSE_CODE_QUIZ_NOT_ACTIVE, "No quiz active"));
            }
        };
    }
}
