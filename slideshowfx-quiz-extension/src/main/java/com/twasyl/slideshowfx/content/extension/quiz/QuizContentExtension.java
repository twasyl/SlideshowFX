package com.twasyl.slideshowfx.content.extension.quiz;

import com.twasyl.slideshowfx.content.extension.AbstractContentExtension;
import com.twasyl.slideshowfx.content.extension.quiz.controllers.QuizContentExtensionController;
import com.twasyl.slideshowfx.markup.IMarkup;
import com.twasyl.slideshowfx.plugin.Plugin;
import com.twasyl.slideshowfx.server.beans.quiz.Quiz;
import javafx.beans.property.ReadOnlyBooleanProperty;

import java.util.Base64;

import static com.twasyl.slideshowfx.content.extension.ResourceType.JAVASCRIPT_FILE;
import static com.twasyl.slideshowfx.global.configuration.GlobalConfiguration.getDefaultCharset;
import static com.twasyl.slideshowfx.icons.FontAwesome.*;
import static com.twasyl.slideshowfx.icons.Icon.QUESTION;

/**
 * The {@link QuizContentExtension} extends the {@link AbstractContentExtension}. It allows to build a content containing
 * quiz to insert inside a SlideshowFX presentation.
 * This extension uses FontAwesome
 * This extension supports HTML markup language.
 *
 * @author Thierry Wasylczenko
 * @version 1.2-SNAPSHOT
 * @since SlideshowFX 1.0
 */
@Plugin
public class QuizContentExtension extends AbstractContentExtension<QuizContentExtensionController> {

    public QuizContentExtension() {
        super("QUIZ",
                "/com/twasyl/slideshowfx/content/extension/quiz/fxml/QuizContentExtension.fxml",
                null,
                QUESTION,
                "Insert a quiz", "Insert a quiz");

        // Add URL
        this.putResource(JAVASCRIPT_FILE, String.format("quiz/font-awesome/%s/js/%s", getFontAwesomeVersion(), getFontAwesomeJSFilename()), getFontAwesomeJSFile());
    }

    @Override
    public String buildContentString(IMarkup markup) {
        return this.buildDefaultContentString();
    }

    @Override
    public String buildDefaultContentString() {
        final Quiz quiz = this.getController().getQuiz();

        final String encodedQuiz = Base64.getEncoder().encodeToString(quiz.toJSONString().getBytes(getDefaultCharset()));

        final StringBuilder builder = new StringBuilder("<div id=\"quiz-").append(quiz.getId()).append("\" class=\"slideshowfx-quiz\" style=\"width: 100%\">\n");
        builder.append("\t<span id=\"").append(System.currentTimeMillis()).append("\" style=\"display: block; width: 100%; background-color: #ECECEC; border-radius: 10px 10px 0 0\">\n")
                .append("\t\t<span id=\"start-quiz-action-button-").append(quiz.getId()).append("\" class=\"start-quiz\" onclick=\"javascript:quiz(this, '")
                .append(encodedQuiz).append("');\">")
                .append("<i class=\"fa fa-play fa-fw\" title=\"Start the quiz\"></i></span>")
                .append("<span id=\"stop-quiz-action-button-").append(quiz.getId()).append("\" class=\"stop-quiz\" style=\"display:none\" onclick=\"javascript:quiz(this, '")
                .append(encodedQuiz).append("');\">")
                .append("<i class=\"fa fa-stop fa-fw\" title=\"Stop the quiz\"></i></span>\n\t\t&nbsp;")
                .append(quiz.getQuestion().getText()).append("\n\t</span>\n");
        builder.append("\t<ul>");

        quiz.getAnswers().forEach(answer -> builder.append("\n\t\t<li>").append(answer.getText()).append("</li>"));

        builder.append("\n\t</ul>\n</div>");

        return builder.toString();
    }

    @Override
    public ReadOnlyBooleanProperty areInputsValid() {
        return this.getController().areInputsValid();
    }
}
