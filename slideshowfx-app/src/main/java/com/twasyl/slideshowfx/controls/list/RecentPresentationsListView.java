package com.twasyl.slideshowfx.controls.list;

import com.twasyl.slideshowfx.global.configuration.RecentPresentation;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.text.Text;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import javafx.util.Callback;

import java.util.function.Consumer;

import static javafx.scene.control.SelectionMode.SINGLE;
import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.ESCAPE;
import static javafx.scene.input.MouseButton.PRIMARY;
import static javafx.stage.WindowEvent.WINDOW_CLOSE_REQUEST;

/**
 * Implementation of a {@link ListView} containing {@link RecentPresentation}. The double-click on an item or pressing
 * the {@link javafx.scene.input.KeyCode#ENTER} key will trigger a <i>presentation opener</i>.
 *
 * @author Thierry Wasylczenko
 * @version 1.0-SNAPSHOT
 * @since SlideshowFX @@NEXT-VERSION@@
 */
public class RecentPresentationsListView extends ListView<RecentPresentation> {

    public RecentPresentationsListView(final Consumer<RecentPresentation> presentationOpener) {
        super();

        this.setPlaceholder(new Text("No recent presentations"));
        this.getSelectionModel().setSelectionMode(SINGLE);
        this.setCellFactory(cellFactory(presentationOpener));
        defineKeyEvent(presentationOpener);
    }

    private Callback<ListView<RecentPresentation>, ListCell<RecentPresentation>> cellFactory(Consumer<RecentPresentation> presentationOpener) {
        return listView -> {
            final ListCell<RecentPresentation> cell = new RecentPresentationCell();
            defineMouseEvent(cell, presentationOpener);
            return cell;
        };
    }

    private void defineMouseEvent(final ListCell<RecentPresentation> cell, final Consumer<RecentPresentation> presentationOpener) {
        cell.setOnMouseClicked(event -> {
            if (event.getButton() == PRIMARY && event.getClickCount() == 2 && !cell.isEmpty()) {
                final RecentPresentation presentation = this.getSelectionModel().getSelectedItem();
                presentationOpener.accept(presentation);
            }
        });
    }

    private void defineKeyEvent(final Consumer<RecentPresentation> presentationOpener) {
        this.setOnKeyPressed(event -> {
            if (event.getCode() == ENTER) {
                final RecentPresentation presentation = this.getSelectionModel().getSelectedItem();
                presentationOpener.accept(presentation);
            } else if (event.getCode() == ESCAPE) {
                final Window source = this.getScene().getWindow();
                source.fireEvent(new WindowEvent(source, WINDOW_CLOSE_REQUEST));
            }
        });
    }
}
