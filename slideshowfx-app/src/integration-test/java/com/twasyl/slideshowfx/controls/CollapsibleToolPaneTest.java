package com.twasyl.slideshowfx.controls;

/*
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX @@NEXT-VERSION@@
 */

import com.twasyl.slideshowfx.icons.FontAwesome;
import com.twasyl.slideshowfx.icons.Icon;
import javafx.application.Application;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.HPos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

public class CollapsibleToolPaneTest extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        final CollapsibleToolPane left = new CollapsibleToolPane();
        left.setPosition(HPos.LEFT);
        final FontAwesome leftIcon11 = new FontAwesome(Icon.DESKTOP);
        leftIcon11.setColor("black");

        left.addContent(leftIcon11, new Button("Left 1.1"));
        left.addContent("Left 1.2", new Button("Left 1.2        !"));

        final CollapsibleToolPane right = new CollapsibleToolPane();
        right.setPosition(HPos.RIGHT);

        final FontAwesome rightIcon = new FontAwesome(Icon.COMMENTS_O);
        rightIcon.setColor("black");
        right.addContent(rightIcon, new Button("Right 1.1"));
        right.addContent("Right 1.2", new Button("Right 1.2"));

        final SplitPane root = new SplitPane(left, right);

        // Make the right content fill the split pane
        final SplitPane.Divider div = root.getDividers().get(0);
        final DoubleProperty dividerWidth = new SimpleDoubleProperty(0);


        final Scene scene = new Scene(root);

        primaryStage.setScene(scene);
//        primaryStage.setMaximized(true);
        primaryStage.setWidth(500);
        primaryStage.setHeight(500);
        primaryStage.show();


        div.positionProperty().addListener((positionValue, oldPosition, newPosition) -> {
            final Region lookup = (Region) root.lookup("> .split-pane-divider");
            dividerWidth.set(lookup == null ? 0 : lookup.getWidth());
            double contentWidth = newPosition.doubleValue() * root.getWidth() - left.getToolbarWidth() - dividerWidth.get();
            left.setContentWidth(contentWidth);
        });

div.setPosition(div.getPosition());
//        dividerWidth.setValue(((Region) root.lookup(".split-pane-divider")).getWidth());
//        final DoubleBinding contentWidth = div.positionProperty().subtract(1).negate().multiply(root.widthProperty()).subtract(right.toolbarWidthProperty()).subtract(dividerWidth);
//        right.contentWidthProperty().bind(contentWidth);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
