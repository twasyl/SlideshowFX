package com.twasyl.slideshowfx.controls.list;

import com.twasyl.slideshowfx.global.configuration.RecentPresentation;
import javafx.scene.control.ListCell;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

/**
 * A {@link javafx.scene.control.Cell} implementation used to display {@link RecentPresentation}. The cell has the
 * {@value #STYLE_CLASS} style class.
 *
 * @author Thierry Wasylczenko
 * @version 1.0-SNAPSHOT
 * @since SlideshowFX @@NEXT-VERSION@@
 */
public class RecentPresentationCell extends ListCell<RecentPresentation> {
    public static final String STYLE_CLASS = "recent-presentation-list-cell";

    public RecentPresentationCell() {
        super();
        this.getStyleClass().add(STYLE_CLASS);
    }

    @Override
    protected void updateItem(RecentPresentation presentation, boolean empty) {
        super.updateItem(presentation, empty);

        if (presentation == null || empty) {
            setGraphic(null);
        } else {
            final Text presentationName = new Text(presentation.getName());
            presentationName.getStyleClass().add("recent-presentation-name");

            final Text presentationPath = new Text(presentation.getParent());
            presentationPath.getStyleClass().add("recent-presentation-path");

            final TextFlow flow = new TextFlow(presentationName, new Text(" "), presentationPath);
            setGraphic(flow);
        }
    }
}
