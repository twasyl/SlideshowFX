package com.twasyl.slideshowfx.controls;

/*
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX @@NEXT-VERSION@@
 */

import com.twasyl.slideshowfx.controls.CollapsibleToolPane;
import com.twasyl.slideshowfx.icons.FontAwesome;
import com.twasyl.slideshowfx.icons.Icon;
import javafx.application.Application;
import javafx.geometry.HPos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class CollapsibleToolPaneTest extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        final CollapsibleToolPane left = new CollapsibleToolPane();
        left.setPosition(HPos.LEFT);
        final FontAwesome leftIcon = new FontAwesome(Icon.DESKTOP);
        leftIcon.setColor("black");
        left.addContent(leftIcon, new Button("Left"));

        final CollapsibleToolPane right = new CollapsibleToolPane();
        final FontAwesome rightIcon = new FontAwesome(Icon.COMMENTS_O);
        rightIcon.setColor("black");
        right.addContent(rightIcon, new Button("Right"));

        final StackPane root = new StackPane(left, right);

        final Scene scene = new Scene(root);

        primaryStage.setScene(scene);
//        primaryStage.setMaximized(true);
        primaryStage.setWidth(500);
        primaryStage.setHeight(500);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
