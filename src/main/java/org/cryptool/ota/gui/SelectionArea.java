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

package org.cryptool.ota.gui;

import java.io.File;
import java.util.ArrayList;

import org.cryptool.ota.OTAApplication;
import org.cryptool.ota.util.Alignment;
import org.cryptool.ota.util.Icons;
import org.cryptool.ota.util.ImageUtils;
import org.cryptool.ota.util.Key;
import org.cryptool.ota.util.Selection;
import org.cryptool.ota.util.TranscribedImage;
import org.cryptool.ota.util.Utils;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.TilePane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.util.Duration;

public class SelectionArea extends HBox {

    private static final double ZOOM_WIDTH = 300;
    private static final double ZOOM_HEIGHT = 200;

    private static final int ICON_SIZE = 40;

    private static final CheckBox lockUnlock = new CheckBox();

    private final TextField transcriptionTextField = new TextField("");
    private final TextField decryptionTextField = new TextField("");

    private final TilePane rightTilePane = new TilePane();
    private final ImageView selectedImageView = new ImageView();
    private final ImageView iconImageView = new ImageView();
    private final ScrollPane rightScrollPane = new ScrollPane();
    private final Pane leftPane = new Pane();

    private final Button deleteIconButton;
    private final Button saveIconButton;

    public Color selectedColor = null;

    private long lastForcedTextUpdate;

    public SelectionArea() {
        super();
        setStyle("-fx-border-color: black; -fx-border-width: 3");

        lastForcedTextUpdate = System.currentTimeMillis();
        transcriptionTextField.setFont(new Font(Utils.adjust(36)));
        transcriptionTextField.setStyle("-fx-border-color: black; -fx-border-width: 1");
        transcriptionTextField.textProperty().addListener((obs, oldText, newText) -> {
            if (System.currentTimeMillis() - lastForcedTextUpdate < 100) {
                return;
            }

            if (!newText.equals(oldText) && OTAApplication.colors.available() && selectedColor != null) {

                OTAApplication.colors.put(selectedColor.toString(), newText);

                updateDecryptionFields(selectedColor, true);

                OTAApplication.colorParametersChanged(selectedColor, true, false, true /* to be on the safe side */);
            }

        });

        leftPane.setMinHeight(Utils.adjust(330));
        leftPane.setMaxHeight(Utils.adjust(330));
        leftPane.setMinWidth(Utils.adjust(580));
        leftPane.setMaxWidth(Utils.adjust(580));

        decryptionTextField.setFont(new Font(Utils.adjust(18)));
        decryptionTextField.setStyle("-fx-border-color: black; -fx-border-width: 1");

        decryptionTextField.textProperty().addListener((obs, oldText, newText) -> {
            if (System.currentTimeMillis() - lastForcedTextUpdate < 100) {
                return;
            }

            if (!newText.equals(oldText) && OTAApplication.colors.available() && selectedColor != null) {

                final String c = OTAApplication.colors.get(selectedColor.toString());
                if (newText.isEmpty()) {
                    OTAApplication.key.remove(c);
                } else {
                    OTAApplication.key.put(c, newText);
                }
                OTAApplication.key.markAsChanged();

                lockUnlock.setSelected(Key.lockedC(c) || OTAApplication.key.lockedP(c));

                OTAApplication.colorParametersChanged(selectedColor, false, false, true);
            }

        });

        iconImageView.setFitWidth(Utils.adjust(ZOOM_WIDTH * 2));
        iconImageView.setFitHeight(Utils.adjust(ZOOM_HEIGHT * 2));
        iconImageView.setPreserveRatio(true);
        iconImageView.setSmooth(true);

        leftPane.getChildren().add(transcriptionTextField);
        transcriptionTextField.setLayoutX(Utils.adjust(10));
        transcriptionTextField.setLayoutY(Utils.adjust(10));
        transcriptionTextField.setMinWidth(Utils.adjust(280));
        transcriptionTextField.setMaxWidth(Utils.adjust(280));

        leftPane.getChildren().add(decryptionTextField);
        decryptionTextField.setLayoutX(Utils.adjust(10));
        decryptionTextField.setLayoutY(Utils.adjust(100));
        decryptionTextField.setMaxWidth(Utils.adjust(200));

        leftPane.getChildren().add(lockUnlock);
        lockUnlock.setLayoutX(Utils.adjust(220));
        lockUnlock.setLayoutY(Utils.adjust(110));
        lockUnlock.setMinWidth(Utils.adjust(130));
        lockUnlock.setText("Locked");
        final Font smallButtonFont = new Font(Utils.adjust(12));
        lockUnlock.setFont(smallButtonFont);

        lockUnlock.setVisible(OTAApplication.key.isKeyAvailable());
        lockUnlock.setOnAction(e -> {
            boolean newState = lockUnlock.isSelected();
            updateLocked(newState);
        });

        leftPane.getChildren().add(iconImageView);
        iconImageView.setLayoutX(Utils.adjust(10));
        iconImageView.setLayoutY(Utils.adjust(190));

        Button importIconButton = new Button("Import Icon");
        importIconButton.setFont(smallButtonFont);
        importIconButton.setOnMousePressed(event -> {
            if (selectedColor != null) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.getExtensionFilters().addAll(
                        new FileChooser.ExtensionFilter("Png Files", "*.png"),
                        new FileChooser.ExtensionFilter("Jpeg Files", "*.jpg"));
                fileChooser.setInitialDirectory(new File(Icons.ICONS_DIR_NAME));
                File selectedFile = fileChooser.showOpenDialog(OTAApplication.myStage);
                if (selectedFile == null) {
                    return;
                }
                if (Icons.readIcon(selectedColor, selectedFile.getAbsolutePath(), false)) {
                    Icons.saveIcon(selectedColor, Icons.get(selectedColor.toString()));

                    OTAApplication.colorParametersChanged(selectedColor, false, true, false);
                }
            }
        });
        leftPane.getChildren().add(importIconButton);
        importIconButton.setLayoutX(Utils.adjust(10));
        importIconButton.setLayoutY(Utils.adjust(270));

        deleteIconButton = new Button("Delete Icon");
        deleteIconButton.setFont(smallButtonFont);
        deleteIconButton.setOnMousePressed(event -> {
            if (selectedColor != null) {
                if (Icons.deleteIcon(selectedColor)) {
                    OTAApplication.colorParametersChanged(selectedColor, false, true, false);
                }
            }
        });
        leftPane.getChildren().add(deleteIconButton);
        deleteIconButton.setLayoutX(Utils.adjust(90));
        deleteIconButton.setLayoutY(Utils.adjust(270));

        saveIconButton = new Button("Save as Icon");
        saveIconButton.setFont(smallButtonFont);
        saveIconButton.setOnMousePressed(event -> {

            if (selectedImageView.isVisible()) {
                String id = selectedImageView.getId();
                Rectangle r = TranscribedImage.idToRectangle(id);
                int idx = TranscribedImage.idToIndex(id);

                IconEditWindow.show(idx, r);
            }
        });
        leftPane.getChildren().add(saveIconButton);
        saveIconButton.setLayoutX(Utils.adjust(415));
        saveIconButton.setLayoutY(Utils.adjust(270));

        rightTilePane.setPadding(new Insets(5, 5, 5, 5));
        rightTilePane.setVgap(4);
        rightTilePane.setHgap(4);
        rightTilePane.setPrefColumns(50);
        rightTilePane.setMaxWidth(Region.USE_PREF_SIZE);
        rightTilePane.setTileAlignment(Pos.CENTER);
        rightTilePane.setMaxWidth(Utils.adjust(1450));

        rightScrollPane.setContent(new HBox(rightTilePane));
        rightScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        rightScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        // rightScrollPane.setMaxHeight(Utils.adjust(400));
        // rightScrollPane.setMinHeight(Utils.adjust(400));
        rightScrollPane.setMaxWidth(Utils.adjust(1467));
        rightScrollPane.setMinWidth(Utils.adjust(1467));

        selectedImageView.setOnDragDetected((MouseEvent event) -> {
            final String id = selectedImageView.getId();
            int index = TranscribedImage.idToIndex(id);
            if (index == -1) {
                event.consume();
                return;
            }
            Rectangle r = TranscribedImage.idToRectangle(id);
            if (r == null) {
                event.consume();
                return;
            }

            Dragboard db = selectedImageView.startDragAndDrop(TransferMode.COPY);
            ClipboardContent content = new ClipboardContent();

            ImageView imageView = new ImageView(TranscribedImage.image(index).image);

            Rectangle2D viewport = new Rectangle2D(r.getLayoutX(), r.getLayoutY(), r.getWidth(), r.getHeight());
            imageView.setViewport(viewport);
            imageView.setFitWidth(Utils.adjust(ICON_SIZE));
            imageView.setFitHeight(Utils.adjust(ICON_SIZE));
            imageView.setPreserveRatio(true);

            content.putImage(imageView.snapshot(new SnapshotParameters(), null));

            content.putString(id);
            db.setContent(content);

            event.consume();
        });
        selectedImageView.setFitWidth(Utils.adjust(ZOOM_WIDTH));
        selectedImageView.setFitHeight(Utils.adjust(ZOOM_HEIGHT));
        selectedImageView.setPreserveRatio(true);
        selectedImageView.setSmooth(true);

        leftPane.getChildren().add(selectedImageView);
        selectedImageView.setLayoutX(Utils.adjust(300));
        selectedImageView.setLayoutY(Utils.adjust(10));

        getChildren().add(leftPane);

        Region region = new Region();
        region.setMinWidth(Utils.adjust(30));
        HBox.setHgrow(region, Priority.ALWAYS);
        getChildren().add(region);

        getChildren().add(rightScrollPane);
        rightScrollPane.setMinHeight(Utils.adjust(310));
        rightScrollPane.setMaxHeight(Utils.adjust(310));

        OTAApplication.unselect();

        setMinHeight(Utils.adjust(320));
        setMaxHeight(Utils.adjust(320));
        setPrefHeight(Utils.adjust(320));

    }

    public void updateLocked(boolean newState) {
        boolean transcriptionChanged = false;
        String c = OTAApplication.colors.get(selectedColor.toString());
        String p = OTAApplication.key.fromTranscriptionOrDefault(c, "");
        if (p == null) {
            p = "";
        }

        if (newState) {
            if (!Key.lockedC(c) && !OTAApplication.key.lockedP(c)) {
                if (c.isEmpty()) {
                    OTAApplication.colors.put(selectedColor.toString(), "_");
                    transcriptionChanged = true;
                    OTAApplication.key.put("_", "_");
                } else {
                    if (p.length() >= 1 && p.charAt(0) >= 'a' && p.charAt(0) <= 'z') {
                        p = p.substring(0, 1).toUpperCase() + p.substring(1);
                    } else {
                        p = "_" + p;
                    }
                    OTAApplication.key.put(c, p);
                }
            }
        } else {
            if (Key.lockedC(c) && OTAApplication.key.lockedP(c)) {
                OTAApplication.key.remove(c);
                c = c.substring(1);
                if (c.isEmpty()) {
                    c = selectedColor.toString();
                }
                while ((p.length() >= 1 && p.charAt(0) >= 'A' && p.charAt(0) <= 'Z') || p.startsWith("_")) {
                    if (p.charAt(0) >= 'A' && p.charAt(0) <= 'Z') {
                        p = p.substring(0, 1).toLowerCase() + p.substring(1);
                    }
                    if (p.startsWith("_")) {
                        p = p.substring(1);
                    }
                }
                OTAApplication.key.put(c, p);
            } else if (Key.lockedC(c)) {
                OTAApplication.key.remove(c);
                c = c.substring(1);
                if (c.isEmpty()) {
                    c = selectedColor.toString();
                }
                OTAApplication.key.put(c, p);
            } else if (OTAApplication.key.lockedP(c)) {
                while ((p.length() >= 1 && p.charAt(0) >= 'A' && p.charAt(0) <= 'Z') || p.startsWith("_")) {
                    if (p.charAt(0) >= 'A' && p.charAt(0) <= 'Z') {
                        p = p.substring(0, 1).toLowerCase() + p.substring(1);
                    }
                    if (p.startsWith("_")) {
                        p = p.substring(1);
                    }
                }
                OTAApplication.key.put(c, p);
            }
        }
        transcriptionTextField.setText(c);
        updateDecryptionFields(selectedColor, false);
        OTAApplication.colorParametersChanged(selectedColor, transcriptionChanged, false, true /* to be on the safe side */);
    }

    public void toggleLocked() {
        String c = OTAApplication.colors.get(selectedColor.toString());

        if (Key.lockedC(c) || OTAApplication.key.lockedP(c)) {
            updateLocked(false);
        } else {
            updateLocked(true);
        }
    }

    public void colorIconChanged() {
        Image icon = Icons.get(selectedColor.toString());
        if (icon != null) {
            iconImageView.setImage(icon);
            iconImageView.setFitWidth(Utils.adjust(ICON_SIZE * 2));
            iconImageView.setFitHeight(Utils.adjust(ICON_SIZE * 2));
            iconImageView.setVisible(true);
        } else {
            iconImageView.setVisible(false);
        }
        updateIconButtonsVisibility();
    }

    public void unselectColor() {
        rightTilePane.getChildren().clear();

        final Background bg = new Background(new BackgroundFill(Color.GRAY, CornerRadii.EMPTY, Insets.EMPTY));
        setBackground(bg);
        lastForcedTextUpdate = System.currentTimeMillis();
        transcriptionTextField.setText("");
        decryptionTextField.setText("");
        selectedColor = null;

        leftPane.setVisible(false);
    }

    public void unselectRectangle() {
        hideZoomedImage();
        updateIconButtonsVisibility();
        if (selectedColor != null) {
            displayIcons(false);
        }
    }

    public void selectRectangle(int idx, Rectangle newSelected) {
        showZoomedImage(TranscribedImage.transcribedImages[idx].image, newSelected, true);
        updateIconButtonsVisibility();
    }

    public void hideZoomedImage() {
        selectedImageView.setVisible(false);
        selectedImageView.setId("");
    }

    public void showZoomedImage(Image image, Rectangle r, boolean setId) {
        selectedImageView.setVisible(true);

        // selectedImageView.setImage(image);
        double maxDim = Math.max(r.getWidth(), r.getHeight()) + 50;
        double addX = maxDim - r.getWidth();
        double addY = maxDim - r.getHeight();
        double w = r.getWidth() + addX;
        double h = r.getHeight() + addY;

        if (w / h > ZOOM_WIDTH / ZOOM_HEIGHT) {
            h = w * ZOOM_HEIGHT / ZOOM_WIDTH;
            addY = h - r.getHeight();
        } else if (w / h < ZOOM_WIDTH / ZOOM_HEIGHT) {
            w = h * ZOOM_WIDTH / ZOOM_HEIGHT;
            addX = w - r.getWidth();
        }

        final double x = r.getLayoutX() - addX / 2;
        final double y = r.getLayoutY() - addY / 2;

        Rectangle2D viewportRect = new Rectangle2D(x, y, w, h);
        selectedImageView.setViewport(viewportRect);

        selectedImageView.setImage(ImageUtils.negativeAround((int) x, (int) y, (int) (w), (int) (h), (int) addX / 2,
                (int) addY / 2, image));

        if (setId) {
            selectedImageView.setId(TranscribedImage.rectangleToId(TranscribedImage.rectangleToIndex(r), r));
        }
    }

    public void refresh() {
        if (selectedColor != null) {
            selectColor(selectedColor, true);
        }
    }

    public void selectColor(Color color, boolean force) {
        boolean colorChanged = !color.equals(selectedColor);
        if (colorChanged || force) {
            decryptionTextField.setVisible(OTAApplication.key.isKeyAvailable());
            lockUnlock.setVisible(OTAApplication.key.isKeyAvailable());

            leftPane.setVisible(true);

            Color newColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), 0.4);
            final Background background = new Background(new BackgroundFill(newColor, CornerRadii.EMPTY, Insets.EMPTY));

            setBackground(background);

            rightTilePane.setBackground(background);
            rightScrollPane.setBackground(background);
            Image icon = Icons.get(color.toString());
            if (icon != null) {
                iconImageView.setImage(icon);
                iconImageView.setFitHeight(Utils.adjust(ICON_SIZE * 2));
                iconImageView.setFitWidth(Utils.adjust(ICON_SIZE * 2));
                iconImageView.setVisible(true);
            } else {
                iconImageView.setVisible(false);
            }

            lastForcedTextUpdate = System.currentTimeMillis();

            transcriptionTextField.setText(OTAApplication.colors.get(color.toString()));
            updateDecryptionFields(color, true);
            updateIconButtonsVisibility();

            selectedColor = color;
        }
        displayIcons(colorChanged);
    }

    private void updateIconButtonsVisibility() {
        saveIconButton.setVisible(selectedImageView.isVisible());
        deleteIconButton.setVisible(iconImageView.isVisible());
    }

    private void updateDecryptionFields(Color color, boolean setSelected) {
        if (!OTAApplication.key.isKeyAvailable()) {
            decryptionTextField.setVisible(false);
            lockUnlock.setVisible(false);
            return;
        }
        decryptionTextField.setVisible(true);
        lockUnlock.setVisible(true);
        String plaintext = "";
        String c = OTAApplication.colors.get(color.toString());
        if (OTAApplication.key.fromTranscriptionAvailable(c)) {
            plaintext = OTAApplication.key.fromTranscription(c);
        }
        decryptionTextField.setText(plaintext);

        if (setSelected) {
            lockUnlock.setSelected(OTAApplication.key.lockedP(c) || Key.lockedC(c));
        }
    }

    void displayIcons(boolean updateScrollBar) {

        // System.out.println(updateScrollBar);

        rightTilePane.getChildren().clear();

        ImageView selectedImageView = null;

        double iconSize = Utils.adjust(ICON_SIZE);
        int count = 0;
        for (int index = 0; index < TranscribedImage.size(); index++) {
            final ArrayList<Rectangle> positions = Alignment.sortedPositions(index);

            for (Rectangle r : positions) {
                if (selectedColor.equals(r.getFill())) {
                    final String id = TranscribedImage.rectangleToId(index, r);

                    ImageView imageView = getImageView(iconSize, id);
                    if (Selection.contains(id)) {
                        selectedImageView = imageView;
                    }

                    if (imageView != null) {
                        rightTilePane.getChildren().add(imageView);

                        imageView.setOnDragDetected((MouseEvent event) -> {
                            if (Selection.contains(imageView.getId())) {
                                if (Selection.isEmpty()) {
                                    return;
                                }
                                Dragboard db = imageView.startDragAndDrop(TransferMode.COPY);
                                ClipboardContent content = new ClipboardContent();
                                final String firstId = Selection.getFirst();
                                HBox hBox = new HBox();

                                ImageView iv = getImageView(iconSize, firstId);
                                if (iv != null) {
                                    hBox.getChildren().add(iv);
                                }

                                if (Selection.size() == 1) {
                                    content.putString(firstId);
                                } else {
                                    content.putString("Selection");
                                    final Text text = new Text(" " + Selection.size() + " symbols");
                                    text.setFont(new Font(Utils.adjust(36)));
                                    hBox.getChildren().add(text);
                                }
                                content.putImage(hBox.snapshot(new SnapshotParameters(), null));
                                db.setContent(content);
                            }
                            event.consume();
                        });
                        imageView.setOnMouseClicked(mouseEvent -> {
                            // Utils.start();

                            final Node intersectedNode = mouseEvent.getPickResult().getIntersectedNode();
                            String clickedId = intersectedNode.getId();
                            int idx = TranscribedImage.idToIndex(clickedId);
                            // Utils.stop("idToIndex");

                            Rectangle nr = TranscribedImage.idToRectangle(clickedId);
                            // Utils.stop("idToRectangle");
                            if (idx == -1 && nr == null) {
                                System.out.printf("Undefined id: %s\n", clickedId);
                            }

                            ImageView iv2 = ((ImageView) intersectedNode);
                            // Inverse logic, because the Selection has not been changed.
                            if (!Selection.contains(clickedId)) {
                                iv2.setImage(TranscribedImage.image(idx).negative);
                            } else {
                                iv2.setImage(TranscribedImage.image(idx).image);
                            }
                            // Utils.stop("clickedId");

                            selectRectangle(idx, nr);
                            // Utils.stop("Select rectangle");

                            OTAApplication.symbolClickedFromSelectionArea(clickedId, idx, nr);
                            // Utils.stop("symbolClickedFromSelectionArea");

                        });
                    }
                }
                count++;
            }
        }
        if (updateScrollBar) {

            final int _count = count;
            final ImageView _selectedImageView = selectedImageView;
            Timeline tl = new Timeline(
                    new KeyFrame(Duration.millis(300),
                            event -> {
                                if (_selectedImageView != null) {
                                    Utils.adjustVerticalScrollBar(rightScrollPane, _selectedImageView, 1.0,
                                            10.0 / _count);
                                }
                            }));
            tl.setCycleCount(3);
            tl.play();

        }

    }

    public void refreshGrid() {
        for (Node n : rightTilePane.getChildren()) {
            if (n instanceof ImageView) {
                ImageView view = (ImageView) n;
                String rectangleId = view.getId();
                int rectangleIdx = TranscribedImage.idToIndex(rectangleId);
                Image current = view.getImage();

                final Image negative = TranscribedImage.image(rectangleIdx).negative;
                final Image image = TranscribedImage.image(rectangleIdx).image;
                if (!Selection.contains(rectangleId) && current == negative) {
                    view.setImage(image);
                } else if (Selection.contains(rectangleId) && current == image) {
                    view.setImage(negative);
                }
            }
        }
    }

    private ImageView getImageView(double iconSize, String id) {
        int index = TranscribedImage.idToIndex(id);
        if (index == -1) {
            return null;
        }
        Rectangle r = TranscribedImage.idToRectangle(id);
        if (r == null) {
            return null;
        }

        ImageView imageView = new ImageView();
        imageView.setFitWidth(iconSize);
        imageView.setFitHeight(iconSize + Utils.adjust(10));
        imageView.setPreserveRatio(true);
        imageView.setVisible(true);
        imageView.setId(id);

        updateImageView(id, index, r, imageView);

        return imageView;
    }

    private void updateImageView(String id, int index, Rectangle r, ImageView imageView) {
        if (Selection.contains(id)) {
            imageView.setImage(TranscribedImage.image(index).negative);
        } else {
            imageView.setImage(TranscribedImage.image(index).image);
        }

        Rectangle2D viewport = new Rectangle2D(r.getLayoutX(), r.getLayoutY(), r.getWidth(), r.getHeight());
        imageView.setViewport(viewport);
    }

}
