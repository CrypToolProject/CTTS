/*
    Copyright 2023 George Lasry & CrypTool 2 Team

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

package org.cryptool.ctts.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.cryptool.ctts.CTTSApplication;
import org.cryptool.ctts.CTTSApplication.Mode;
import org.cryptool.ctts.util.Icons;
import org.cryptool.ctts.util.Selection;
import org.cryptool.ctts.util.TranscribedImage;
import org.cryptool.ctts.util.Utils;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

/*
    List view of the key
 */
public class ClusterListView extends ListView<String> {

    public ClusterListView() {

        this.setItems(items);
        this.setCellFactory(l -> new ColorRectCell());
        this.setMinWidth(Utils.adjust(350));

        VBox.setVgrow(this, Priority.ALWAYS);
        this.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> select(newValue));

        Timeline tl = new Timeline(
                new KeyFrame(Duration.millis(300),
                        event -> {
                            if (updateListView) {
                                refresh();
                                updateListView = false;
                            }
                        }));
        tl.setCycleCount(Timeline.INDEFINITE);
        tl.play();
    }

    public void updateListView(boolean now) {
        if (now) {
            refresh();
            updateListView = false;
        } else {
            updateListView = true;
        }
    }

    public void show(boolean clusterView) {
        refresh();
        setVisible(true);
        resize(clusterView);
    }

    public void hide() {
        setMaxHeight(0);
        setMinHeight(0);
        setMaxWidth(0);
        setMinWidth(0);
        setVisible(false);
    }

    public void reset() {
        ArrayList<String> usedColors = CTTSApplication.colors.sortedColors();

        items.clear();
        items.addAll(usedColors);

    }

    // This is also used for the grid view of the key
    public static HBox line(String item, boolean gridView, boolean noCache) {
        final int maxTextLength = 36;
        final int iconSize = 35;
        Color color = CTTSApplication.colors.valueOf(item);
        if (color == null) {
            return null;
        }

        Color newColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), 0.4);
        final Background bg = new Background(new BackgroundFill(newColor, CornerRadii.EMPTY, Insets.EMPTY));

        HBox hBox = new HBox();

        ImageView iconImageView = new ImageView();
        iconImageView.setPreserveRatio(true);
        iconImageView.setFitWidth(Utils.adjust(iconSize));
        iconImageView.setFitHeight(Utils.adjust(iconSize));

        StackPane iconSymbolPane = new StackPane(iconImageView);
        iconSymbolPane.setMinWidth(Utils.adjust(iconSize));
        iconSymbolPane.setMaxWidth(Utils.adjust(iconSize));
        iconSymbolPane.setBackground(bg);
        iconSymbolPane.setMinSize(Utils.adjust(iconSize), Utils.adjust(iconSize));
        StackPane.setAlignment(iconImageView, Pos.CENTER);

        Image icon = Icons.get(color.toString());
        if (icon != null) {
            iconImageView.setImage(icon);
        } else if (gridView) {
            Rectangle r = TranscribedImage.first(color);
            if (r != null) {
                int idx = TranscribedImage.rectangleToIndex(r);
                iconImageView.setImage(TranscribedImage.image(idx).image);
                Rectangle2D viewport = new Rectangle2D(r.getLayoutX(), r.getLayoutY(), r.getWidth(), r.getHeight());
                iconImageView.setViewport(viewport);
                iconImageView.setId(color.toString());
            }
        }
        iconImageView.setId(color.toString());
        iconSymbolPane.setId(color.toString());

        hBox.getChildren().add(iconSymbolPane);
        Region region = new Region();
        region.setMinWidth(Utils.adjust(5));
        hBox.getChildren().add(region);

        String transcriptionValue = CTTSApplication.colors.get(item);
        String cacheKey = gridView + "|" + color + "|" + transcriptionValue;

        StackPane ciphertextSymbolPane;

        if (!noCache && cache.containsKey(cacheKey)) {
            ciphertextSymbolPane = cache.get(cacheKey);
        } else {
            ciphertextSymbolPane = t(transcriptionValue, maxTextLength, iconSize, newColor, color);
            cache.put(cacheKey, ciphertextSymbolPane);
        }
        hBox.getChildren().add(ciphertextSymbolPane);

        if (CTTSApplication.key.isKeyAvailable()) {

            String plaintext = "";
            if (CTTSApplication.key.fromColorStringAvailable(item)) {
                plaintext = CTTSApplication.key.fromColorString(item);
            }

            String cachePKey = gridView + "||" + color + "||" + plaintext;
            StackPane plaintextSymbolPane;
            if (!noCache && cache.containsKey(cachePKey)) {
                plaintextSymbolPane = cache.get(cachePKey);
            } else {
                plaintextSymbolPane = d(plaintext, maxTextLength, iconSize, color);
                cache.put(cachePKey, plaintextSymbolPane);
            }
            hBox.getChildren().add(plaintextSymbolPane);
        }

        int maxSymbols = gridView ? (CTTSApplication.key.isKeyAvailable() ? 0 : 3) : (CTTSApplication.mode == Mode.CLUSTER ? 47 : 5);

        if (maxSymbols != 0) {
            int maxWidth = maxSymbols * Utils.adjust(iconSize);
            double displayedWidth = 0.0;
            ArrayList<Rectangle> symbols = TranscribedImage.symbolsOfType(color);
            int total = symbols.size();
            int displayed = 0;
            for (Rectangle r : symbols) {
                if (displayedWidth < maxWidth) {
                    int index = TranscribedImage.idToIndex(r.getId());
                    ImageView imageView = new ImageView(TranscribedImage.image(index).image);
                    Rectangle2D viewport = new Rectangle2D(r.getLayoutX(), r.getLayoutY(), r.getWidth(), r.getHeight());
                    imageView.setViewport(viewport);
                    imageView.setPreserveRatio(true);
                    if (gridView) {
                        imageView.setFitWidth(Utils.adjust(30));
                    }
                    imageView.setFitHeight(Utils.adjust(iconSize));

                    displayedWidth += Utils.adjust(r.getWidth() * Utils.adjust(iconSize) / r.getHeight());
                    hBox.getChildren().add(imageView);

                    imageView.setId(TranscribedImage.rectangleToId(index, r));

                    imageView.setOnDragDetected((MouseEvent event) -> {
                        if (CTTSApplication.mode == Mode.CLUSTER && !CTTSApplication.detailed) {
                            CTTSApplication.unselectRectangle();
                            Dragboard db = imageView.startDragAndDrop(TransferMode.COPY);
                            ClipboardContent content = new ClipboardContent();
                            SnapshotParameters snapshotParameters = new SnapshotParameters();
                            WritableImage image = imageView.snapshot(snapshotParameters, null);
                            content.putImage(image);
                            content.putString(imageView.getId());
                            db.setContent(content);
                        }
                        event.consume();
                    });
                    displayed++;
                } else {
                    break;
                }
            }

            if (total > 0 && !gridView) {
                final Text totalText = new Text();
                String text = "";
                if (displayed < total) {
                    text = " ... + " + (total - displayed) + " more - ";
                }
                text += " Total: " + total;
                totalText.setText(text);
                totalText.setFont(new Font(Utils.adjust(12)));

                hBox.getChildren().add(new StackPane(totalText));
            }
        }

        hBox.setOnDragDropped((DragEvent event) -> {
            Color droppedColor = CTTSApplication.colors.valueOf(hBox.getId());
            if (droppedColor == null) {
                return;
            }
            Dragboard db = event.getDragboard();
            // Get item id here, which was stored when the drag started.
            boolean success = false;
            // If this is a meaningful drop...

            if (db.hasString()) {
                final String id = db.getString();
                boolean fromSelected = id.equals("Selection");
                Rectangle draggedR = TranscribedImage.idToRectangle(id);
                int draggedIdx = TranscribedImage.idToIndex(id);
                Color draggedColor = null;
                if (draggedR != null && draggedIdx != -1) {
                    draggedColor = (Color) draggedR.getFill();
                } else if (fromSelected) {
                    for (String selectedId : Selection.selectedIds()) {
                        Rectangle r = TranscribedImage.idToRectangle(selectedId);
                        if (r != null) {
                            draggedColor = (Color) r.getFill();
                        }
                    }
                } else {
                    try {
                        draggedColor = Color.valueOf(id.substring(id.indexOf(":") + 1));
                    } catch (Exception ignored) {

                    }
                }
                if (draggedColor == null) {
                    return;
                }

                if (fromSelected && !Selection.isEmpty()) {
                    for (String selectedId : Selection.selectedIds()) {

                        Rectangle r = TranscribedImage.idToRectangle(selectedId);

                        if (r != null) {
                            TranscribedImage.changeColor(selectedId, r, droppedColor);
                        } else {
                            System.out.printf("ClusterView Drop Undefined id: %s\n", selectedId);
                        }
                    }
                    CTTSApplication.symbolChangedColor(draggedR, draggedColor);
                } else if (draggedR != null) {
                    TranscribedImage.changeColor(id, draggedR, droppedColor);
                    CTTSApplication.symbolChangedColor(draggedR, draggedColor);
                } else {
                    if (id.startsWith("swap")) {
                        if (CTTSApplication.colors.swap(draggedColor, droppedColor)) {
                            CTTSApplication.fullKeyWindow.refresh();
                            CTTSApplication.unselect();
                        }
                    } else if (id.startsWith("insert")) {
                        if (CTTSApplication.colors.insert(draggedColor, droppedColor)) {
                            CTTSApplication.fullKeyWindow.refresh();
                            CTTSApplication.unselect();
                        }
                    }
                }
            }

            event.setDropCompleted(success);
            event.consume();
        });

        hBox.setOnDragOver((DragEvent event) -> {
            if (event.getGestureSource() != hBox && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
            event.consume();
        });

        hBox.setOnMouseClicked(mouseEvent -> {
            final Node intersectedNode = mouseEvent.getPickResult().getIntersectedNode();
            String id = intersectedNode.getId();

            if (id == null) {
                return;
            }

            try {
                CTTSApplication.colorSelected(id);
                return;
            } catch (IllegalArgumentException e) {
            }

            Rectangle r = TranscribedImage.idToRectangle(id);
            if (r != null) {
                CTTSApplication.colorSelected(r.getFill().toString());
            }

        });

        hBox.setId(color.toString());

        hBox.setBackground(FullKeyWindow.whiteBg);

        hBox.setOnDragDetected((MouseEvent event) -> {
            if (CTTSApplication.mode == Mode.CLUSTER && CTTSApplication.detailed) {
                CTTSApplication.unselectRectangle();
                Dragboard db = hBox.startDragAndDrop(
                        (event.getButton() == MouseButton.SECONDARY) ? TransferMode.MOVE : TransferMode.COPY);
                ClipboardContent content = new ClipboardContent();
                SnapshotParameters snapshotParameters = new SnapshotParameters();
                HBox h = line(hBox.getId(), true, true);
                Text text = new Text();
                text.setTextAlignment(TextAlignment.CENTER);
                text.setFont(Font.font("Verdana", FontWeight.BOLD, Utils.adjust(24)));
                text.setFill(Color.RED);
                if (event.getButton() == MouseButton.SECONDARY) {
                    content.putString("swap:" + hBox.getId());
                    text.setText("Swap ");
                } else {
                    content.putString("insert:" + hBox.getId());
                    text.setText("Insert ");
                }
                h.getChildren().add(0, text);

                WritableImage image = h.snapshot(snapshotParameters, null);
                content.putImage(image);

                db.setContent(content);
            }
            event.consume();
        });

        return hBox;
    }

    private boolean updateListView = false;
    private ObservableList<String> items = FXCollections.observableArrayList();
    private static Map<String, StackPane> cache = new HashMap<>();

    private static StackPane d(String text, int maxTextLength, int iconSize, Color color) {
        if (text.length() > maxTextLength) {
            text = text.substring(0, maxTextLength) + " ...";
        }
        Text textField = new Text(text);
        final FontWeight weight = Utils.isSingleLetter(text) ? FontWeight.BOLD : FontWeight.NORMAL;
        textField.setFont(Font.font("Verdana", weight, Utils.adjust(16)));
        final int maxWidth = Utils.adjust(80);
        if (text.length() > 6) {
            boolean found = false;
            if (!text.contains(" ")) {
                for (int size = 16; size >= 11; size -= 1) {
                    final Font font2 = Font.font("Verdana", weight, Utils.adjust(size));
                    final double[] dim = Utils.setAndGetSize(textField, font2, 0);
                    if (dim[0] <= maxWidth) {
                        found = true;
                        break;
                    }
                }
                textField.setWrappingWidth(0);
            }

            if (!found) {
                for (int size = 16; size > 0; size -= 1) {
                    final Font font2 = Font.font("Verdana", weight, Utils.adjust(size));
                    final double[] dim = Utils.setAndGetSize(textField, font2, maxWidth);
                    if (dim[0] <= maxWidth && dim[1] <= Utils.adjust(iconSize)) {
                        break;
                    }
                }
                textField.setWrappingWidth(maxWidth);
            }
        }
        textField.setTextAlignment(TextAlignment.CENTER);

        StackPane stackPane = new StackPane(textField);

        stackPane.setMinSize(maxWidth, Utils.adjust(iconSize));
        stackPane.setMaxSize(maxWidth, Utils.adjust(iconSize));

        stackPane.setId(color.toString());
        textField.setId(color.toString());
        return stackPane;
    }

    private static StackPane t(String text, int maxTextLength, int iconSize, Color newColor, Color color) {
        if (text.length() > maxTextLength) {
            text = text.substring(0, maxTextLength) + " ...";
        }
        Text textField = new Text(text);
        final FontWeight weight = FontWeight.BOLD;
        textField.setFont(Font.font("Verdana", weight, Utils.adjust(16)));

        final int maxWidth = Utils.adjust(80);
        if (text.length() > 6) {
            boolean found = false;
            if (!text.contains(" ")) {
                for (int size = 16; size >= 8; size -= 1) {
                    final Font font2 = Font.font("Verdana", weight, Utils.adjust(size));
                    final double[] dim = Utils.setAndGetSize(textField, font2, 0);
                    if (dim[0] <= maxWidth) {
                        found = true;
                        break;
                    }
                }
                textField.setWrappingWidth(0);
            }
            if (!found) {
                for (int size = 16; size > 0; size -= 1) {
                    final Font font2 = Font.font("Verdana", weight, Utils.adjust(size));
                    final double[] dim = Utils.setAndGetSize(textField, font2, maxWidth);
                    if (dim[0] <= maxWidth && dim[1] <= Utils.adjust(iconSize)) {
                        break;
                    }
                }
                textField.setWrappingWidth(maxWidth);
            }
        }

        StackPane stackPane = new StackPane(textField);
        final Background bg = new Background(new BackgroundFill(newColor, CornerRadii.EMPTY, Insets.EMPTY));
        stackPane.setBackground(bg);
        textField.setId(color.toString());
        textField.setTextAlignment(TextAlignment.CENTER);
        StackPane.setAlignment(textField, Pos.CENTER);
        stackPane.setMinSize(maxWidth, Utils.adjust(iconSize));
        stackPane.setMaxSize(maxWidth, Utils.adjust(iconSize));
        stackPane.setId(color.toString());
        textField.setId(color.toString());

        return stackPane;

    }

    private void resize(boolean full) {
        if (full) {
            setMinWidth(getParent().getBoundsInParent().getWidth());
            setMinHeight(getParent().getBoundsInParent().getHeight() - Utils.adjust(4.));
        } else {
            setMinWidth(Utils.adjust(350));
            setMaxWidth(Utils.adjust(350));
        }
        reset();
    }

    private static class ColorRectCell extends ListCell<String> {
        @Override
        public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (item != null) {
                HBox hBox2 = line(item, false, false);
                setGraphic(hBox2);

            }

        }
    }

    private void select(String colorString) {

        if (colorString == null) {
            return;
        }
        CTTSApplication.colorSelected(colorString);
    }    

}
