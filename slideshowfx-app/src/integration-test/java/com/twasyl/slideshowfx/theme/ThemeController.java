package com.twasyl.slideshowfx.theme;

import com.twasyl.slideshowfx.controls.tree.TemplateTreeView;
import com.twasyl.slideshowfx.engine.template.TemplateEngine;
import com.twasyl.slideshowfx.icons.FontAwesome;
import com.twasyl.slideshowfx.icons.Icon;
import com.twasyl.slideshowfx.icons.IconStack;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.TreeItem;

import java.io.File;
import java.net.URL;
import java.util.Random;
import java.util.ResourceBundle;

/*
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX @@NEXT-VERSION@@
 */

public class ThemeController implements Initializable {
    @FXML
    private TemplateTreeView templateTreeView;
    @FXML
    public IconStack modifiableIconStack;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        final TemplateEngine engine = new TemplateEngine();
        try {
            engine.loadArchive(new File("examples/templates/shower-template.sfxt"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        final TreeItem root = new TreeItem(engine.getWorkingDirectory());
        root.setExpanded(true);

        this.templateTreeView.setEngine(engine);
        this.templateTreeView.setRoot(root);

        final File[] children = engine.getWorkingDirectory().listFiles();
        if (children != null) {

            for (File child : children) {
                this.templateTreeView.appendContentToTreeView(child, root);
            }
        }

        this.templateTreeView.closeItem(root);
        root.setExpanded(true);
    }

    public void addIconToStack(ActionEvent event) {
        final Random random = new Random();
        final Icon icon = Icon.values()[random.nextInt(Icon.values().length)];
        final double iconSize = random.nextInt(51) * 1d;
        final Pos position = Pos.values()[random.nextInt(Pos.values().length)];

        final FontAwesome fontAwesome = new FontAwesome(icon, iconSize);
        this.modifiableIconStack.getChildren().add(fontAwesome);

        IconStack.setAlignment(fontAwesome, position);
    }

    public void removeIconFromStack(ActionEvent event) {
        if (this.modifiableIconStack.getChildren().size() > 1) {
            this.modifiableIconStack.getChildren().remove(1);
        }
    }
}
