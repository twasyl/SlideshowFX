package com.twasyl.slideshowfx.controls.builder.elements;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;

/**
 * The StringTemplateElement allows to enter a String as value in a text field.
 * It extends {@link com.twasyl.slideshowfx.controls.builder.elements.AbstractTemplateElement}
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0.0
 */
public class ListTemplateElement extends AbstractTemplateElement<ObservableList<ITemplateElement>> {

    protected final Label opening = new Label("{");
    protected final Label closing = new Label("}");

    public ListTemplateElement(String name) {
        super();

        this.name.set(name);

        /*
         * Create the pane that will host the value and accept drag'n'drop events.
         */
        final VBox content = new VBox(5);
        content.getChildren().addAll(opening, closing);
        content.setOnDragOver(event -> event.acceptTransferModes(TransferMode.COPY));

        final EventHandler<DragEvent> handler = event -> {
            final Dragboard board = event.getDragboard();
            boolean dragSuccess = false;

            if(board.hasString()) {
                final String stringContent = board.getString();
                ITemplateElement element = TemplateElementFactory.buildTemplateElement(stringContent);
                if(this.getWorkingPath() != null) element.setWorkingPath(this.getWorkingPath());

                ListTemplateElement.this.getValue().add(element);
                dragSuccess = true;
            }

            event.setDropCompleted(dragSuccess);
            event.consume();
        };

        content.setOnDragDropped(handler);
        this.text.setOnDragDropped(handler);

        this.appendContent(content);

        this.setValue(FXCollections.observableArrayList());
        this.getValue().addListener((ListChangeListener) change -> {
            while(change.next()) {
                if (change.wasRemoved()) {
                    change.getRemoved()
                            .stream()
                            .filter(node -> content.getChildren().contains(node))
                            .forEach(node -> content.getChildren().remove(node));
                    content.getChildren().removeAll(change.getRemoved());
                } else if(change.wasAdded()) {
                    content.getChildren().addAll(content.getChildren().size() - 1, change.getAddedSubList());
                }
            }

            change.reset();
        });

        content.getChildren().addListener((ListChangeListener) change -> {
            while(change.next()) {
                if (change.wasRemoved()) {
                    change.getRemoved()
                            .stream()
                            .filter(node -> this.getValue().contains(node))
                            .forEach(node -> this.getValue().remove(node));
                }
            }
        });
    }

    /**
     * Add a template element to the value list.
     * @param element The element to add.
     */
    public void add(ITemplateElement element) {
        this.getValue().add(element);
    }

    @Override
    public String getAsString() {
        final StringBuilder builder = new StringBuilder();

        if(getName() != null) builder.append(String.format("\"%1$s\": ", getName()));

        builder.append(this.opening.getText());

        if(getValue() != null) {
            for(int index = 0; index < getValue().size(); index++) {
                builder.append(getValue().get(index).getAsString());

                if(index < getValue().size() - 1) {
                    builder.append(", ");
                }
            }
        }

        builder.append(this.closing.getText());

        return builder.toString();
    }
}
