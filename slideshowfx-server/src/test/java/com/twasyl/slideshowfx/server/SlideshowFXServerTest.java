package com.twasyl.slideshowfx.server;

import com.twasyl.slideshowfx.server.service.AttendeeChatService;
import com.twasyl.slideshowfx.server.service.PresenterChatService;
import com.twasyl.slideshowfx.server.service.QuizService;
import com.twasyl.slideshowfx.server.service.WebappService;
import com.twasyl.slideshowfx.utils.NetworkUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Thierry Wasylczenko
 */
@DisplayName("The SlideshowFX server")
public class SlideshowFXServerTest {

    static final int SERVER_PORT = 50080;
    static final String SERVER_HOST = NetworkUtils.getIP();
    static Document WEBPAGE;

    @BeforeAll
    static void before() throws IOException, InterruptedException, ExecutionException, TimeoutException {
        SlideshowFXServer.create(SERVER_HOST, SERVER_PORT, null)
                .start(WebappService.class)
                .exceptionally(error -> {
                    fail(error);
                    return null;
                }).get(10, SECONDS);


        WEBPAGE = Jsoup.parse(new URL("http://" + SERVER_HOST + ":" + SERVER_PORT + "/slideshowfx"), 5000);
    }

    @AfterAll
    static void after() {
        SlideshowFXServer.getSingleton().stop();
    }

    @DisplayName("has it's port set")
    @Test
    public void portIsSet() {
        assertEquals(SERVER_PORT, SlideshowFXServer.getSingleton().getPort());
    }

    @DisplayName("has it's name set")
    @Test
    public void hostIsSet() {
        assertEquals(SERVER_HOST, SlideshowFXServer.getSingleton().getHost());
    }

    @DisplayName("serves the web application")
    @Nested
    class WebApplication {
        @DisplayName("which has a title")
        @Test
        void webappTitle() {
            assertEquals("SlideshowFX", WEBPAGE.title());
        }

        @DisplayName("which has a page div")
        @Test
        void webappPageDiv() {
            assertNotNull(WEBPAGE.getElementById("page"));
        }

        @DisplayName("which has a header div")
        @Test
        void webappHeaderDiv() {
            assertNotNull(WEBPAGE.getElementById("header"));
        }

        @DisplayName("which has a content div")
        @Test
        void webappContentDiv() {
            assertNotNull(WEBPAGE.getElementById("content"));
        }

        @DisplayName("which has a connection form")
        @Test
        void webappConnectionForm() {
            assertNotNull(WEBPAGE.getElementById("connection-form"));
        }

        @DisplayName("which has a chat container")
        @Test
        void webappChatContainer() {
            assertNotNull(WEBPAGE.getElementById("chat-container"));
        }

        @DisplayName("which has a quiz container")
        @Test
        void webappQuizContainer() {
            assertNotNull(WEBPAGE.getElementById("quiz-container"));
        }
    }
}
