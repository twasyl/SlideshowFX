package com.twasyl.slideshowfx.content.extension.shape.beans;

import com.twasyl.slideshowfx.ui.controls.ExtendedTextField;
import javafx.scene.Node;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import static com.twasyl.slideshowfx.ui.controls.validators.Validators.isInteger;

/**
 * An implementation allowing to create rectangles.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 2.0
 */
public class Rectangle extends AbstractShape {
    private ExtendedTextField x;
    private ExtendedTextField y;
    private ExtendedTextField width;
    private ExtendedTextField height;

    public Rectangle() {
        this.x = new ExtendedTextField("X", true, 3);
        this.x.setValidator(isInteger());

        this.y = new ExtendedTextField("Y", true, 3);
        this.y.setValidator(isInteger());

        this.width = new ExtendedTextField("Width", true, 3);
        this.width.setValidator(isInteger());

        this.height = new ExtendedTextField("Height", true, 3);
        this.height.setValidator(isInteger());
    }

    @Override
    public Node getUI() {
        final FlowPane container = new FlowPane(10, 10);
        container.getChildren().addAll(this.x, this.y, this.width, this.height);

        final FlowPane attributes = new FlowPane(10, 10);
        attributes.getChildren().addAll(this.getCommonAttributes());

        return new VBox(10, container, attributes);
    }

    @Override
    public String buildCreatingInstruction(String paper) {
        final StringBuilder builder = new StringBuilder(paper)
                .append(".rect(")
                .append(this.x.getText()).append(", ")
                .append(this.y.getText()).append(", ")
                .append(this.width.getText()).append(", ")
                .append(this.height.getText())
                .append(")")
                .append(this.buildAttributesInstruction())
                .append(this.buildDragInstruction())
                .append(";");

        return builder.toString();
    }
}
