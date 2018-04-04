package com.twasyl.slideshowfx.content.extension.shape.beans;

import com.twasyl.slideshowfx.ui.controls.ExtendedTextField;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import static com.twasyl.slideshowfx.ui.controls.validators.Validators.*;

/**
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 2.0
 */
public abstract class AbstractShape implements IShape {
    protected ExtendedTextField fill;
    protected ExtendedTextField opacity;
    protected ExtendedTextField stroke;
    protected ExtendedTextField strokeWidth;
    protected CheckBox drag;

    protected AbstractShape() {
        this.fill = new ExtendedTextField("Background color", false, 5);
        this.fill.setValidator(isNotEmpty());

        this.opacity = new ExtendedTextField("Opacity", false, 5);
        this.opacity.setValidator(isDouble());

        this.stroke = new ExtendedTextField("Border color", false, 5);
        this.stroke.setValidator(isNotEmpty());

        this.strokeWidth = new ExtendedTextField("Border width", false, 2);
        this.strokeWidth.setValidator(isInteger());

        this.drag = new CheckBox("Can be dragged");
    }

    protected List<Node> getCommonAttributes() {
        final List<Node> attributes = new ArrayList<>();

        attributes.add(this.fill);
        attributes.add(this.opacity);
        attributes.add(this.stroke);
        attributes.add(this.strokeWidth);
        attributes.add(this.drag);

        return attributes;
    }

    protected String buildAttributesInstruction() {
        if (hasAtLeastOneValidAttribute()) {
            final StringJoiner attributes = new StringJoiner(", ");
            if (this.fill.isValid()) {
                attributes.add(String.format("fill: \"%1$s\"", this.fill.getText()));
            }
            if (this.opacity.isValid()) {
                attributes.add(String.format("\"fill-opacity\": %1$s", this.opacity.getText()));
            }
            if (this.stroke.isValid()) {
                attributes.add(String.format("stroke: \"%1$s\"", this.stroke.getText()));
            }
            if (this.strokeWidth.isValid()) {
                attributes.add(String.format("strokeWidth: %1$s", this.strokeWidth.getText()));
            }

            final StringBuilder builder = new StringBuilder(".attr({").append(attributes.toString()).append("})");
            return builder.toString();
        }

        return "";
    }

    protected String buildDragInstruction() {
        if (this.drag.isSelected()) {
            return ".drag()";
        }
        return "";
    }

    protected boolean hasAtLeastOneValidAttribute() {
        return this.fill.isValid() || this.opacity.isValid() || this.stroke.isValid() || this.strokeWidth.isValid();
    }
}
