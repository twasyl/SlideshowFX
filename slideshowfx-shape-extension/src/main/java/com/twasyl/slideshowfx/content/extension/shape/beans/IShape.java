package com.twasyl.slideshowfx.content.extension.shape.beans;

import javafx.scene.Node;

/**
 * A {@link IShape} object is used in order to get the configuration's UI allowing to define a
 * drawing that will be generated and to build the JavaScript instruction to build it.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 2.0
 */
public interface IShape {

    /**
     * Get the configuration's UI that will allow to define the drawing that will be created.
     *
     * @return The configuration's UI.
     */
    Node getUI();

    /**
     * Create the {@code SnapSVG} JavaScript line to create the drawing. For instance, to create
     * a circle the following instruction will be returned: {@code paper.circle(50, 50, 50);}.
     *
     * @param paper The variable's name of the SnapSVG object.
     * @return The JavaScript instruction creating the desired object.
     */
    String buildCreatingInstruction(final String paper);
}
