package com.twasyl.slideshowfx.theme;

import com.twasyl.slideshowfx.controls.tree.TemplateTreeView;
import com.twasyl.slideshowfx.engine.template.TemplateEngine;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TreeItem;

import java.io.File;
import java.net.URL;
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

            for (File child: children) {
                this.templateTreeView.appendContentToTreeView(child, root);
            }
        }

        this.templateTreeView.closeItem(root);
        root.setExpanded(true);
    }
}
