package com.twasyl.slideshowfx.icons;

import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Class used to host multiple {@link FontAwesome} icons.
 *
 * @author Thierry Wasylczenko
 * @version 1.1
 * @since SlideshowFX 2.0
 */
public class IconStack extends StackPane {

    private static final String DEFAULT_ICON_COLOR_STYLE_CLASS = "default-icon-color-";
    private static final String ICON_INDEX_STYLE_CLASS = "icon-index-";

    public IconStack() {
        getStyleClass().add("icon-stack");
        setListenerOnChildren();
    }

    private void setListenerOnChildren() {
        this.getChildren().addListener((ListChangeListener<Node>) change -> {

            while (change.next()) {
                if (change.wasAdded()) {
                    for (int index = change.getFrom(); index < change.getTo(); index++) {
                        this.addIconStyleClassForNode(change.getList().get(index), index);
                    }

                    if (change.getTo() < change.getList().size()) {
                        for (int index = change.getTo(); index < change.getList().size(); index++) {
                            final Node node = change.getList().get(index);
                            this.cleanAllIconStyleClassFromNode(node);
                            this.addIconStyleClassForNode(node, index);
                        }
                    }
                } else if (change.wasRemoved()) {
                    // Clean classes for removed elements
                    change.getRemoved().stream().forEach(this::cleanAllIconStyleClassFromNode);

                    // Change remaining icons after the deletion
                    for (int index = change.getFrom(); index < change.getList().size(); index++) {
                        final Node node = change.getList().get(index);
                        this.cleanAllIconStyleClassFromNode(node);
                        this.addIconStyleClassForNode(node, index);
                    }
                }
            }

            change.reset();
        });
    }

    private void cleanAllIconStyleClassFromNode(final Node node) {
        final List<String> styleClasses = node.getStyleClass().stream()
                .filter(styleClass -> styleClass.startsWith(DEFAULT_ICON_COLOR_STYLE_CLASS) || styleClass.startsWith(ICON_INDEX_STYLE_CLASS))
                .collect(Collectors.toList());
        node.getStyleClass().removeAll(styleClasses);
    }

    private void addIconStyleClassForNode(final Node node, final int index) {
        node.getStyleClass().addAll(DEFAULT_ICON_COLOR_STYLE_CLASS + index, ICON_INDEX_STYLE_CLASS + index);
    }
}
