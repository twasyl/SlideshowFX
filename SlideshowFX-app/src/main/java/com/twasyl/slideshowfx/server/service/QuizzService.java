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

package com.twasyl.slideshowfx.server.service;

import com.twasyl.slideshowfx.beans.quizz.Quizz;
import com.twasyl.slideshowfx.beans.quizz.QuizzResult;
import com.twasyl.slideshowfx.controls.SlideShowScene;
import com.twasyl.slideshowfx.server.SlideshowFXServer;
import com.twasyl.slideshowfx.utils.TemplateProcessor;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.vertx.java.core.Handler;
import org.vertx.java.core.VoidHandler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class provides the quizz services.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0.0
 */
public class QuizzService extends Verticle {
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


        this.vertx.eventBus().registerHandler("slideshowfx.quizz.start", buildStartQuizzHandler())
                             .registerHandler("slideshowfx.quizz.stop", buildStopQuizzHandler())
                             .registerHandler("slideshowfx.quizz.current", buildGetCurrentQuizzHandler());
    }

    private void updatedRouteMatcher() {
        final Map serverInfo = this.vertx.sharedData().getMap(SlideshowFXServer.SHARED_DATA_SERVERS);
        final SlideshowFXServer singleton = SlideshowFXServer.getSingleton();
        final RouteMatcher routeMatcher = (RouteMatcher) singleton.getHttpServer().requestHandler();

        // URL for answering a quizz
        routeMatcher.post(this.url.concat("/:quizzid/answer"), request -> {
            request.expectMultiPart(true);
            request.endHandler(new VoidHandler() {
                @Override
                protected void handle() {
                    int statusCode = 500;

                    try {
                        if (currentQuizz != null && currentQuizz.getId() == Long.parseLong(request.params().get("quizzid"))) {

                            final String stringAnswer = request.formAttributes().get("answer");
                            final JsonObject jsonAnswer = new JsonObject(new String(Base64.getDecoder().decode(stringAnswer)));
                            final JsonArray answersArray = jsonAnswer.getArray("answers");

                            final Long[] answers = new Long[answersArray.size()];
                            int index = 0;
                            for(Object object : answersArray) {
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
                            request.response().setStatusCode(406).end();
                        }
                    } finally {
                        request.response().setStatusCode(statusCode).end();
                    }
                }
            });
        })
        // Get the JavaScript resources
        .get("/slideshowfx/quizz/js/quizzService.js", request -> {
            final Map templateTokens = this.vertx.sharedData().getMap(SlideshowFXServer.SHARED_DATA_TEMPLATE_TOKENS);

            final Configuration configuration = TemplateProcessor.getJsConfiguration();

            final Map tokenValues = new HashMap();
            tokenValues.put(templateTokens.get(SlideshowFXServer.SHARED_DATA_SERVER_HOST_TOKEN).toString(), serverInfo.get(SlideshowFXServer.SHARED_DATA_HTTP_SERVER_HOST).toString());
            tokenValues.put(templateTokens.get(SlideshowFXServer.SHARED_DATA_SERVER_PORT_TOKEN).toString(), ((Integer) serverInfo.get(SlideshowFXServer.SHARED_DATA_HTTP_SERVER_PORT)).toString());

            try (final StringWriter writer = new StringWriter()) {
                final Template template = configuration.getTemplate("quizzService.js");
                template.process(tokenValues, writer);

                writer.flush();

                request.response().putHeader("Content-Type", "text/javascript").setStatusCode(200).setChunked(true).write(writer.toString()).end();
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Error when a client tried to access the chat", e);

                request.response().setStatusCode(500).end();
            } catch (TemplateException e) {
                LOGGER.log(Level.WARNING, "Error when processing the chat template", e);
                request.response().setStatusCode(500).end();
            }
        });
    }

    /**
     * Build a handler that will start the quizz.
     * @return The handler for starting a quizz.
     */
    private Handler<Message<JsonObject>> buildStartQuizzHandler() {
        final Handler<Message<JsonObject>> handler = message -> {
            message.reply();

            final JsonObject object = message.body();
            final String quizzString = new String(Base64.getDecoder().decode(object.getString("encoded-quizz")));

            QuizzService.this.currentQuizz = Quizz.build(quizzString);

            // Add the Quizz to the results. If the Quizz already exists, it won't be erased
            if (!QuizzService.this.results.containsKey(QuizzService.this.currentQuizz.getId())) {
                final QuizzResult quizzResult = new QuizzResult();
                quizzResult.setQuizz(QuizzService.this.currentQuizz);

                QuizzService.this.results.put(QuizzService.this.currentQuizz.getId(), quizzResult);
            }

            final JsonObject reply = new JsonObject();
            reply.putString("service", "slideshowfx.quizz.start");
            reply.putObject("data", new JsonObject().putString("encoded-quizz", Base64.getEncoder().encodeToString(this.currentQuizz.toJSON().encode().getBytes())));

            for(Object textHandlerId : this.vertx.sharedData().getSet(SlideshowFXServer.SHARED_DATA_WEBSOCKET_CLIENTS)) {
                this.vertx.eventBus().send((String) textHandlerId, reply.encode());
            }

            if(SlideShowScene.getSingleton() != null) SlideShowScene.getSingleton().publishQuizzResult(QuizzService.this.results.get(QuizzService.this.currentQuizz.getId()));
        };

        return handler;
    }

    private Handler<Message<JsonObject>> buildStopQuizzHandler() {
        final Handler<Message<JsonObject>> handler = message -> {
            message.reply();

            final JsonObject object = message.body();
            final Long quizzId = object.getNumber("id").longValue();

            // Ensure the ID is equal to the current quizz
            if(this.currentQuizz != null && this.currentQuizz.getId() == quizzId) {
                this.currentQuizz = null;

                final JsonObject reply = new JsonObject();
                reply.putString("service", "slideshowfx.quizz.stop");
                reply.putObject("data", new JsonObject().putString("message", "The quizz has been stopped"));

                for(Object textHandlerId : this.vertx.sharedData().getSet(SlideshowFXServer.SHARED_DATA_WEBSOCKET_CLIENTS)) {
                    this.vertx.eventBus().send((String) textHandlerId, reply.encode());
                }
            }
        };

        return handler;
    }

    private Handler<Message<JsonObject>> buildGetCurrentQuizzHandler() {
        final Handler<Message<JsonObject>> handler = message -> {
            if(this.currentQuizz != null) {
                message.reply(this.currentQuizz.toJSON());
            } else {
                message.reply();
            }
        };

        return handler;
    }
}
