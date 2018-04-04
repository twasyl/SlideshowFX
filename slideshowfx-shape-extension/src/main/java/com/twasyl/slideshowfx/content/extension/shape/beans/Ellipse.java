package com.twasyl.slideshowfx.content.extension.shape.beans;

import com.twasyl.slideshowfx.ui.controls.ExtendedTextField;
import javafx.scene.Node;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import static com.twasyl.slideshowfx.ui.controls.validators.Validators.isInteger;

/**
 * An implementation allowing to create ellipses.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 2.0
 */
public class Ellipse extends AbstractShape {
    private ExtendedTextField x;
    private ExtendedTextField y;
    private ExtendedTextField horizontalRadius;
    private ExtendedTextField verticalRadius;

    public Ellipse() {
        this.x = new ExtendedTextField("X", true, 3);
        this.x.setValidator(isInteger());

        this.y = new ExtendedTextField("Y", true, 3);
        this.y.setValidator(isInteger());

        this.horizontalRadius = new ExtendedTextField("Horizontal radius", true, 3);
        this.horizontalRadius.setValidator(isInteger());

        this.verticalRadius = new ExtendedTextField("Vertical radius", true, 3);
        this.verticalRadius.setValidator(isInteger());
    }

    @Override
    public Node getUI() {
        final HBox container = new HBox(10);
        container.getChildren().addAll(this.x, this.y, this.horizontalRadius, this.verticalRadius);

        final FlowPane attributes = new FlowPane(10, 10);
        attributes.getChildren().addAll(this.getCommonAttributes());

        return new VBox(10, container, attributes);
    }

    @Override
    public String buildCreatingInstruction(String paper) {
        final StringBuilder builder = new StringBuilder(paper)
                .append(".ellipse(")
                .append(this.x.getText()).append(", ")
                .append(this.y.getText()).append(", ")
                .append(this.horizontalRadius.getText()).append(", ")
                .append(this.verticalRadius.getText())
                .append(")")
                .append(this.buildAttributesInstruction())
                .append(this.buildDragInstruction())
                .append(";");

        return builder.toString();
    }


}
