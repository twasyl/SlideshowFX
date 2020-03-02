package com.twasyl.slideshowfx.content.extension.shape.controllers;

import com.twasyl.slideshowfx.content.extension.AbstractContentExtensionController;
import com.twasyl.slideshowfx.content.extension.shape.beans.IShape;
import com.twasyl.slideshowfx.content.extension.shape.controls.*;
import com.twasyl.slideshowfx.ui.controls.ExtendedTextField;
import com.twasyl.slideshowfx.utils.DialogHelper;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.scene.control.TitledPane;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.*;

import static com.twasyl.slideshowfx.ui.controls.validators.Validators.isInteger;
import static javafx.scene.control.ButtonType.YES;
import static javafx.scene.input.MouseButton.PRIMARY;
import static javafx.scene.input.TransferMode.MOVE;

/**
 * This class is the controller used by the {@code ShapeContentExtension.fxml} file.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 2.0
 */
public class ShapeContentExtensionController extends AbstractContentExtensionController {
    private static DataFormat NODE_DATA_FORMAT = new DataFormat("sfx-node");
    final PseudoClass VALID_DRAG_OVER = PseudoClass.getPseudoClass("valid-drag-over");

    @FXML
    private ExtendedTextField drawingWidth;
    @FXML
    private ExtendedTextField drawingHeight;
    @FXML
    private ListView<IShapeItem> shapeType;
    @FXML
    private VBox shapesToDraw;
    private List<IShape> shapes = new ArrayList<>();

    public int getDrawingWidth() {
        int width = 0;
        final String widthAsString = this.drawingWidth.getText().trim();

        if (isInteger().isValid(widthAsString)) {
            width = Integer.parseInt(widthAsString);
        }

        return width;
    }

    public int getDrawingHeight() {
        int height = 0;
        final String heightAsString = this.drawingHeight.getText().trim();

        if (isInteger().isValid(heightAsString)) {
            height = Integer.parseInt(heightAsString);
        }

        return height;
    }

    public List<IShape> getShapes() {
        return this.shapes;
    }

    protected TitledPane buildShapeContainer(final IShapeItem item) {
        final IShape shape = item.getShape();
        this.shapes.add(shape);

        final TitledPane pane = new TitledPane(item.getLabel(), shape.getUI());
        pane.setId(item.getLabel() + System.currentTimeMillis());
        pane.setCollapsible(false);

        pane.setOnDragDetected(event -> {
            final Dragboard dragboard = pane.startDragAndDrop(MOVE);
            final Map<DataFormat, Object> contents = new HashMap<>();
            contents.put(NODE_DATA_FORMAT, 0);

            dragboard.setContent(contents);
            event.consume();
        });

        pane.setOnDragOver(event -> {
            final Object source = event.getGestureSource();

            if (source != null && source != pane && source instanceof Node) {
                final Dragboard dragboard = event.getDragboard();

                if (dragboard.hasContent(NODE_DATA_FORMAT)) {
                    event.acceptTransferModes(MOVE);
                    pane.pseudoClassStateChanged(VALID_DRAG_OVER, true);
                }
            }
            event.consume();
        });

        pane.setOnDragExited(event -> {
            pane.pseudoClassStateChanged(VALID_DRAG_OVER, false);
        });

        pane.setOnDragDropped(event -> {
            boolean dragIsASuccess = false;
            final Object source = event.getGestureSource();

            if (source != null && source != pane && source instanceof Node) {
                final Dragboard dragboard = event.getDragboard();

                if (dragboard.hasContent(NODE_DATA_FORMAT)) {

                    // Update the UI
                    final int sourceIndex = this.shapesToDraw.getChildren().indexOf(source);
                    this.shapesToDraw.getChildren().remove(sourceIndex);

                    int targetIndex = this.shapesToDraw.getChildren().indexOf(pane);
                    this.shapesToDraw.getChildren().add(targetIndex, (Node) source);

                    // Update the internal API
                    final IShape drawingToMove = this.shapes.get(sourceIndex);
                    this.shapes.remove(sourceIndex);
                    this.shapes.add(targetIndex, drawingToMove);

                    dragIsASuccess = true;
                }
            }

            event.setDropCompleted(dragIsASuccess);
            event.consume();
        });

        pane.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && event.getButton() == PRIMARY) {
                final ButtonType answer = DialogHelper.showConfirmationAlert("Delete this shape", "Are you sure you want to delete this shape?");

                if(answer == YES) {
                    // Determine the index in order to remove the pane and it's associated Shape
                    final int shapeIndex = this.shapesToDraw.getChildren().indexOf(pane);
                    this.shapesToDraw.getChildren().remove(shapeIndex);
                    this.shapes.remove(shapeIndex);
                }
            }

            event.consume();
        });

        return pane;
    }

    @Override
    public ReadOnlyBooleanProperty areInputsValid() {
        final ReadOnlyBooleanWrapper property = new ReadOnlyBooleanWrapper();
        property.bind(this.drawingWidth.validProperty().and(this.drawingHeight.validProperty()));

        return property;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.shapeType.setCellFactory(listview -> new ShapeCell());

        this.shapeType.getItems().addAll(
                new CircleItem(),
                new EllipseItem(),
                new OctagonItem(),
                new PentagonItem(),
                new RectangleItem(),
                new TriangleItem()
        );

        this.shapeType.setOnMouseClicked(event -> {
            if (event.getButton() == PRIMARY && event.getClickCount() == 2) {
                final IShapeItem item = this.shapeType.getSelectionModel().getSelectedItem();

                if (item != null) {
                    this.shapesToDraw.getChildren().add(this.buildShapeContainer(item));
                }
            }

            event.consume();
        });
    }
}
