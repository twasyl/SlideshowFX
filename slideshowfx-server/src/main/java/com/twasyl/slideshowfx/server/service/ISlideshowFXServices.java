package com.twasyl.slideshowfx.server.service;

/**
 * This interface lists the base services provided by SlideshowFX for the embedded server.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0
 */
public interface ISlideshowFXServices {
    String SERVICE_CHAT_ATTENDEE_MESSAGE_ADD = "slideshowfx.chat.attendee.message.add";
    String SERVICE_CHAT_ATTENDEE_MESSAGE_UPDATE = "slideshowfx.chat.attendee.message.update";
    String SERVICE_CHAT_ATTENDEE_HISTORY = "slideshowfx.chat.attendee.history";

    String SERVICE_CHAT_PRESENTER_MESSAGE_ADD = "slideshowfx.chat.presenter.message.add";

    String SERVICE_QUIZ_START = "slideshowfx.quiz.start";
    String SERVICE_QUIZ_STOP = "slideshowfx.quiz.stop";
    String SERVICE_QUIZ_CURRENT = "slideshowfx.quiz.current";
}
