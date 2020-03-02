package com.twasyl.slideshowfx.controls.list;

import com.twasyl.slideshowfx.global.configuration.GlobalConfiguration;
import com.twasyl.slideshowfx.global.configuration.RecentPresentation;
import com.twasyl.slideshowfx.style.Styles;
import com.twasyl.slideshowfx.style.theme.Themes;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.function.Consumer;

/**
 * Control hosting a title and a {@link RecentPresentationsListView}. The control has the {@value STYLE_CLASS} style class.
 *
 * @author Thierry Wasylczenko
 * @version 1.0-SNAPSHOT
 * @since SlideshowFX @@NEXT-VERSION@@
 */
public class RecentPresentationsView extends VBox {
    public static final String STYLE_CLASS = "recent-presentations-view";

    private final Text title;
    private final RecentPresentationsListView list;

    public RecentPresentationsView(final Consumer<RecentPresentation> presentationOpener) {
        Themes.applyTheme(this, GlobalConfiguration.getThemeName());
        Styles.applyApplicationStyle(this);

        this.title = new Text("Recent presentations");
        this.title.getStyleClass().add("title");

        this.list = new RecentPresentationsListView(presentationOpener);
        this.list.setPrefHeight(200);
        this.list.setMinHeight(200);
        this.list.setMaxHeight(200);

        getStyleClass().add(STYLE_CLASS);

        this.setSpacing(10);
        this.getChildren().addAll(this.title, this.list);
    }

    public void addRecentPresentation(final RecentPresentation recentPresentation) {
        this.list.getItems().add(recentPresentation);
    }

    @Override
    public void requestFocus() {
        this.list.requestFocus();
    }
}
