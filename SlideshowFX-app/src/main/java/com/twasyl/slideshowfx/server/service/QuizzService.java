/*
 * Copyright 2016 Thierry Wasylczenko
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

package com.twasyl.slideshowfx.server.service;

import com.twasyl.slideshowfx.beans.quizz.Quizz;
import com.twasyl.slideshowfx.beans.quizz.QuizzResult;
import com.twasyl.slideshowfx.controls.slideshow.SlideshowPane;
import com.twasyl.slideshowfx.server.SlideshowFXServer;
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

import static com.twasyl.slideshowfx.server.service.IServicesCode.*;

/**
 * This class provides the quizz services.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0.0
 */
public class QuizzService extends AbstractSlideshowFXService {
    private static final Logger LOGGER = Logger.getLogger(QuizzService.class.getName());

    private final String url = "/slideshowfx/quizz";
    private Quizz currentQuizz = null;

    /**
     * The results of all quizz. The key of this Map represents the ID of the Quizz, the value the QuizzResult which
     * contains all correct and wrong answers.
     */
    private final Map<Long, QuizzResult> results = new HashMap<>();

    @Override
    public void start() {

        this.updatedRouteMatcher();

        this.register(SERVICE_QUIZZ_START, this.buildStartQuizzHandler())
            .register(SERVICE_QUIZZ_STOP, this.buildStopQuizzHandler())
            .register(SERVICE_QUIZZ_CURRENT, buildGetCurrentQuizzHandler());
    }

    private void updatedRouteMatcher() {
        final Router router = SlideshowFXServer.getSingleton().getRouter();

        // URL for answering a quizz
        router.post(this.url.concat("/:quizzid/answer")).handler(routingContext -> {
            int statusCode = 500;

            try {
                if (currentQuizz != null && currentQuizz.getId() == Long.parseLong(routingContext.request().getParam("quizzid"))) {

                    final String stringAnswer = routingContext.request().getFormAttribute("answer");
                    final JsonObject jsonAnswer = new JsonObject(new String(Base64.getDecoder().decode(stringAnswer)));
                    final JsonArray answersArray = jsonAnswer.getJsonArray("answers");

                    final Long[] answers = new Long[answersArray.size()];
                    int index = 0;
                    for (Object object : answersArray) {
                        answers[index++] = ((Number) object).longValue();
                    }

                    boolean isCorrect = currentQuizz.checkAnswers(answers);
                    final QuizzResult result = results.get(currentQuizz.getId());

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
        router.get("/slideshowfx/quizz/js/quizzService.js").handler(routingContext -> {
            final LocalMap<String, String> templateTokens = this.vertx.sharedData().getLocalMap(SlideshowFXServer.SHARED_DATA_TEMPLATE_TOKENS);

            final Configuration configuration = TemplateProcessor.getJsConfiguration();

            final Map tokenValues = new HashMap();
            tokenValues.put(templateTokens.get(SlideshowFXServer.SHARED_DATA_SERVER_HOST_TOKEN).toString(), SlideshowFXServer.getSingleton().getHost());
            tokenValues.put(templateTokens.get(SlideshowFXServer.SHARED_DATA_SERVER_PORT_TOKEN).toString(), SlideshowFXServer.getSingleton().getPort() + "");

            try (final StringWriter writer = new StringWriter()) {
                final Template template = configuration.getTemplate("quizzService.js");
                template.process(tokenValues, writer);

                writer.flush();

                routingContext.response().putHeader("Content-Type", "application/javascript").setStatusCode(200).setChunked(true).write(Buffer.buffer(writer.toString())).end();
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
     * Build a handler that will start the quizz.
     * @return The handler for starting a quizz.
     */
    private Handler<Message<JsonObject>> buildStartQuizzHandler() {
        final Handler<Message<JsonObject>> handler = message -> {
            final JsonObject object = message.body();
            final String quizzString = new String(Base64.getDecoder().decode(object.getString("encoded-quizz")));

            QuizzService.this.currentQuizz = Quizz.build(quizzString);

            // Add the Quizz to the results. If the Quizz already exists, it won't be erased
            if (!QuizzService.this.results.containsKey(QuizzService.this.currentQuizz.getId())) {
                final QuizzResult quizzResult = new QuizzResult();
                quizzResult.setQuizz(QuizzService.this.currentQuizz);

                QuizzService.this.results.put(QuizzService.this.currentQuizz.getId(), quizzResult);
            }

            final JsonObject encodedQuizz = new JsonObject()
                    .put("encoded-quizz", Base64.getEncoder().encodeToString(this.currentQuizz.toJSON().encode().getBytes()));
            final JsonObject reply = this.buildResponse(SERVICE_QUIZZ_START, RESPONSE_CODE_QUIZZ_STARTED, encodedQuizz);
            this.sendResponseToWebSocketClients(reply);

            if(SlideshowPane.getSingleton() != null) SlideshowPane.getSingleton().publishQuizzResult(QuizzService.this.results.get(QuizzService.this.currentQuizz.getId()));

            message.reply(reply);
        };

        return handler;
    }

    private Handler<Message<JsonObject>> buildStopQuizzHandler() {
        final Handler<Message<JsonObject>> handler = message -> {
            final JsonObject object = message.body();
            final Long quizzId = object.getLong("id");

            // Ensure the ID is equal to the current quizz
            if(this.currentQuizz != null && this.currentQuizz.getId() == quizzId) {
                this.currentQuizz = null;


                final JsonObject reply = this.buildResponse(SERVICE_QUIZZ_STOP, RESPONSE_CODE_QUIZZ_STOPPED, "The quizz has been stopped");
                this.sendResponseToWebSocketClients(reply);
            }

            message.reply(this.buildResponse(SERVICE_QUIZZ_STOP, RESPONSE_CODE_QUIZZ_STOPPED, "Quizz stopped"));
        };

        return handler;
    }

    private Handler<Message<JsonObject>> buildGetCurrentQuizzHandler() {
        final Handler<Message<JsonObject>> handler = message -> {
            if(this.currentQuizz != null) {
                message.reply(this.buildResponse(SERVICE_QUIZZ_CURRENT, RESPONSE_CODE_QUIZZ_RETRIEVED, this.currentQuizz.toJSON()));
            } else {
                message.reply(this.buildResponse(SERVICE_QUIZZ_CURRENT, RESPONSE_CODE_QUIZZ_NOT_ACTIVE, "No quizz active"));
            }
        };

        return handler;
    }
}
