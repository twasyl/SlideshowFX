package com.twasyl.slideshowfx.content.extension.shape.beans;

import com.twasyl.slideshowfx.ui.controls.ExtendedTextField;
import javafx.scene.Node;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import static com.twasyl.slideshowfx.ui.controls.validators.Validators.isInteger;

/**
 * An implementation allowing to create triangles.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 2.0
 */
public class Triangle extends AbstractShape {
    private ExtendedTextField xField;
    private ExtendedTextField yField;
    private ExtendedTextField widthField;
    private ExtendedTextField heightField;

    public Triangle() {
        this.xField = new ExtendedTextField("X", true, 3);
        this.xField.setValidator(isInteger());

        this.yField = new ExtendedTextField("Y", true, 3);
        this.yField.setValidator(isInteger());

        this.widthField = new ExtendedTextField("Width", true, 3);
        this.widthField.setValidator(isInteger());

        this.heightField = new ExtendedTextField("Height", true, 3);
        this.heightField.setValidator(isInteger());
    }

    @Override
    public Node getUI() {
        final FlowPane container = new FlowPane(10, 10);
        container.getChildren().addAll(this.xField, this.yField, this.widthField, this.heightField);

        final FlowPane attributes = new FlowPane(10, 10);
        attributes.getChildren().addAll(this.getCommonAttributes());

        return new VBox(10, container, attributes);
    }

    @Override
    public String buildCreatingInstruction(String paper) {
        final double width = Double.parseDouble(this.widthField.getText());
        final double height = Double.parseDouble(this.heightField.getText());
        final double x = Double.parseDouble(this.xField.getText());
        final double y = Double.parseDouble(this.yField.getText());

        double x1 = x + (width / 2);
        double y1 = y;
        double x2 = x + width;
        double y2 = y + height;
        double x3 = x;
        double y3 = y + height;

        final StringBuilder builder = new StringBuilder(paper)
                .append(".polyline([")
                .append(x1).append(", ").append(y1).append(", ")
                .append(x2).append(", ").append(y2).append(", ")
                .append(x3).append(", ").append(y3).append(", ")
                .append(x1).append(", ").append(y1).append("])")
                .append(this.buildAttributesInstruction())
                .append(this.buildDragInstruction())
                .append(";");

        return builder.toString();
    }
}
