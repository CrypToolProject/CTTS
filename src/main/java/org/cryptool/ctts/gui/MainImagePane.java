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

import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Scale;
import org.cryptool.ctts.CTTSApplication;
import org.cryptool.ctts.util.*;

import java.util.ArrayList;
import java.util.Random;

public class MainImagePane {
    public static final double OPACITY = 0.5;
    public static Pane mainPane;
    public static ScrollPane scrollPane;
    public static SubMode subMode = SubMode.SYMBOLS;
    public static Rectangle baseRectangle;
    public static double decryptionFontSizeFactor = 1.0;
    public static double decryptionYOffset = 1.0;
    static Canvas imageCanvas;

    public static SubMode nextSubMode() {
        switch (subMode) {
            case SYMBOLS:
                return SubMode.LINES;
            case LINES:
                if (CTTSApplication.key.isKeyAvailable()) {
                    return SubMode.DECRYPTION;
                } else {
                    return SubMode.SYMBOLS;
                }
            case DECRYPTION:
                return SubMode.SYMBOLS;
        }
        return null;
    }

    public static void symbolsInImageSnapshot() {
        saveZoomAndScrollState();
        zoom(1.0);

        FileUtils.snapshot("snapshots", ImageUtils.replaceImageFormat(TranscribedImage.current().filename, "_symbols"), mainPane);

        zoom(TranscribedImage.current().scaleValue);
        scrollPane.setVvalue(TranscribedImage.current().vValue);
        scrollPane.setHvalue(TranscribedImage.current().hValue);

    }

    public static void saveZoomAndScrollState() {
        TranscribedImage.current().vValue = scrollPane.getVvalue();
        TranscribedImage.current().hValue = scrollPane.getHvalue();
        TranscribedImage.current().scaleValue = ((Scale) mainPane.getTransforms().get(0)).getX();
    }

    public static void zoom(double scaleValue) {
        ((Scale) mainPane.getTransforms().get(0)).setX(scaleValue);
        ((Scale) mainPane.getTransforms().get(0)).setY(scaleValue);
    }

    public static void scrollTo(ScrollPane scrollPane, Node selected) {
        if (CTTSApplication.mode != CTTSApplication.Mode.IMAGE) {
            return;
        }
        ArrayList<ArrayList<Rectangle>> lines = Alignment.linesOfSymbols(TranscribedImage.currentImageIndex);
        int lineNumber = -1;

        for (int l = 0; l < lines.size(); l++) {
            ArrayList<Rectangle> line = lines.get(l);
            for (Rectangle r : line) {
                if (selected == r) {
                    lineNumber = l;
                    break;
                }
            }
        }

        Node reference;
        if (CTTSApplication.detailed) {
            reference = DetailedTranscriptionPane.lineToHbox.get(lineNumber);
        } else {
            reference = selected;
        }
        if (reference == null) {
            return;
        }
        double scaleValue = ((Scale) mainPane.getTransforms().get(0)).getX();
        final double increment = 1.0 / Math.min(10 * lines.size(), 100);
        Utils.adjustVerticalScrollBar(scrollPane, reference, scaleValue, increment);
        if (!CTTSApplication.detailed) {
            Utils.adjustHorizontalScrollBar(scrollPane, reference, scaleValue, increment);
        }
    }

    public static void showImage() {
        subMode = SubMode.SYMBOLS;
        BackgroundImage myBI = new BackgroundImage(TranscribedImage.current().image,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.DEFAULT,
                BackgroundSize.DEFAULT);
        mainPane.setBackground(new Background(myBI));
        mainPane.setMinWidth(TranscribedImage.current().image.getWidth());
        mainPane.setMaxWidth(TranscribedImage.current().image.getWidth());
        mainPane.setMinHeight(TranscribedImage.current().image.getHeight());
        mainPane.setMaxHeight(TranscribedImage.current().image.getHeight());

        imageCanvas.setHeight(TranscribedImage.current().image.getHeight());
        imageCanvas.setWidth(TranscribedImage.current().image.getWidth());

        mainPane.getChildren().clear();
        mainPane.getChildren().add(baseRectangle);

        SelectionBox.add(mainPane);

        for (Rectangle r : TranscribedImage.current().positions()) {
            r.opacityProperty().set(OPACITY);
            DragResizeMod.makeResizable(r, null);
            r.setVisible(true);
            mainPane.getChildren().add(r);
        }
        zoom(TranscribedImage.current().scaleValue);
        scrollPane.setVvalue(TranscribedImage.current().vValue);
        scrollPane.setHvalue(TranscribedImage.current().hValue);

    }

    public static void initMainImagePane() {
        imageCanvas = new Canvas(5000, 5000);
        mainPane = new Pane(imageCanvas);

        Scale scale = new Scale(1.0, 1.0);
        mainPane.getTransforms().addAll(scale);
        mainPane.setFocusTraversable(true);

        scrollPane = new ScrollPane(new Group(mainPane));
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        scrollPane.setOnKeyPressed(CTTSApplication::keyPressed);
        mainPane.setOnKeyPressed(CTTSApplication::keyPressed);
        mainPane.addEventFilter(MouseEvent.ANY, MainImagePane::handleMouseEvents);
        scrollPane.addEventFilter(ScrollEvent.ANY, new ZoomHandler());

        baseRectangle = new SymbolRectangle(50, 50, 0, 0, CTTSApplication.colors.get(0), MainImagePane.OPACITY, false);

    }

    public static void zoomOut() {
        if (CTTSApplication.mode != CTTSApplication.Mode.CLUSTER) {
            if (CTTSApplication.detailed) {
                TranscribedImage.current().detailedScaleValue /= 1.05;
                zoom(TranscribedImage.current().detailedScaleValue);
            } else {
                TranscribedImage.current().scaleValue /= 1.05;
                zoom(TranscribedImage.current().scaleValue);
            }
        }
    }

    public static void zoomIn() {
        if (CTTSApplication.mode != CTTSApplication.Mode.CLUSTER) {
            if (CTTSApplication.detailed) {
                TranscribedImage.current().detailedScaleValue *= 1.05;
                zoom(TranscribedImage.current().detailedScaleValue);
            } else {
                TranscribedImage.current().scaleValue *= 1.05;
                zoom(TranscribedImage.current().scaleValue);
            }
        }
    }

    public static void showLines() {

        CTTSApplication.unselect();

        subMode = SubMode.LINES;

        mainPane.getChildren().clear();
        ArrayList<ArrayList<Rectangle>> linesOfSymbols = Alignment.linesOfSymbols(TranscribedImage.currentImageIndex);
        for (ArrayList<Rectangle> lineOfSymbols : linesOfSymbols) {
            Color color = CTTSApplication.colors.get(new Random().nextInt(Colors.all().size()));
            double sum = 0.0;
            for (Rectangle or : lineOfSymbols) {
                Rectangle r = new Rectangle(or.getLayoutX(), or.getLayoutY(), or.getWidth(), or.getHeight());
                r.setFill(color);
                mainPane.getChildren().add(r);
                sum += or.getLayoutY() + or.getHeight() / 2.0;
            }
            int y = (int) (sum / lineOfSymbols.size());
            Rectangle line = new Rectangle(0, y, imageCanvas.getWidth(), 1);

            line.setFill(color);
            mainPane.getChildren().add(line);
        }


    }

    public static void showDecryption() {

        CTTSApplication.unselect();

        subMode = SubMode.DECRYPTION;

        mainPane.getChildren().clear();
        ArrayList<ArrayList<Rectangle>> linesOfSymbols = Alignment.linesOfSymbols(TranscribedImage.currentImageIndex);

        double sumWidth = 0;
        int count = 0;
        for (ArrayList<Rectangle> lineOfSymbols : linesOfSymbols) {
            for (int i = 0; i < lineOfSymbols.size() - 1; i++) {
                sumWidth += lineOfSymbols.get(i + 1).getLayoutX() - lineOfSymbols.get(i).getLayoutX();
                count++;
            }
        }
        double avgWidth = sumWidth / count;
        final int window = 5;
        final Color fontColor = new Color(1.0, 0, 0, 0.7);

        for (ArrayList<Rectangle> lineOfSymbols : linesOfSymbols) {

            for (int i = 0; i < lineOfSymbols.size(); i++) {
                Rectangle r = lineOfSymbols.get(i);
                Color color = (Color) r.getFill();
                String c = CTTSApplication.colors.get(color.toString());
                String p = CTTSApplication.key.get(c);
                if (p != null && !p.isEmpty()) {

                    double maxHeight = avgWidth * 0.7;

                    double maxWidth;
                    if (i == lineOfSymbols.size() - 1) {
                        maxWidth = r.getWidth();
                    } else {
                        maxWidth = lineOfSymbols.get(i + 1).getLayoutX() - r.getLayoutX();
                    }
                    //maxWidth = Math.min(avgWidth * p.length() * 1.3, maxWidth);

                    double sumY = 0;
                    int countY = 0;
                    for (int z = Math.max(0, i - window); z <= Math.min(i + window, lineOfSymbols.size() - 1); z++) {
                        sumY += lineOfSymbols.get(z).getLayoutY();
                        countY++;
                    }

                    StackPane d = d(p, 50, maxWidth * decryptionFontSizeFactor, maxHeight * decryptionFontSizeFactor, fontColor);
                    d.setLayoutX(r.getLayoutX());
                    d.setLayoutY(sumY / countY - maxHeight + decryptionYOffset);
                    mainPane.getChildren().add(d);
                }

            }
        }

    }

    static StackPane d(String text, int maxTextLength, double maxWidth, double maxHeight, Color color) {
        boolean polyphonic = text.contains("|");
        if (polyphonic) {
            text = text.replaceAll("\\|", "\n");
        }
        if (text.length() > maxTextLength) {
            text = text.substring(0, maxTextLength) + " ...";
        }
        Text textField = new Text(text);
        textField.setFill(color);
        final FontWeight weight = FontWeight.BOLD;

        boolean found = false;
        if (!text.contains(" ")) {
            for (int size = (int) maxHeight; size >= 11; size -= 1) {
                final Font font = Font.font("Verdana", weight, size);
                final double[] dim = Utils.setAndGetSize(textField, font, 0);

                if (dim[0] <= maxWidth) {
                    found = true;
                    if (polyphonic) {
                        final Font font2 = Font.font("Verdana", weight, size / 2);
                        textField.setFill(new Color(1.0, 0.0, 0.3, 0.7));
                        textField.setFont(font2);
                    }
                    break;
                }
            }
            textField.setWrappingWidth(0);
        }

        if (!found) {
            for (int size = (int) maxHeight; size > 0; size -= 1) {
                final Font font = Font.font("Verdana", weight, size);
                final double[] dim = Utils.setAndGetSize(textField, font, maxWidth);
                if (dim[0] <= maxWidth && dim[1] <= maxHeight) {
                    break;
                }
            }
            textField.setWrappingWidth(maxWidth);
        }

        textField.setTextAlignment(TextAlignment.LEFT);

        StackPane stackPane = new StackPane(textField);

        stackPane.setMinSize(maxWidth, maxHeight);
        stackPane.setMaxSize(maxWidth, maxHeight);

        return stackPane;
    }

    public static void handleMouseEvents(MouseEvent mouseEvent) {
        if (CTTSApplication.mode == CTTSApplication.Mode.IMAGE && CTTSApplication.detailed) {
            if (mouseEvent.getEventType() == MouseEvent.MOUSE_CLICKED) {
                final String id = mouseEvent.getPickResult().getIntersectedNode().getId();
                if (id != null) {
                    int idx = TranscribedImage.idToIndex(id);
                    Rectangle nr = TranscribedImage.idToRectangle(id);
                    if (idx != -1 && idx == TranscribedImage.currentImageIndex && nr != null) {
                        CTTSApplication.symbolSelectedFromImagePane(nr);
                        if (mouseEvent.getClickCount() == 2) {
                            CTTSApplication.selectionArea.toggleLocked();
                        }
                    }
                }
            }
        }
        if (CTTSApplication.mode == CTTSApplication.Mode.IMAGE && !CTTSApplication.detailed && MainImagePane.subMode == SubMode.SYMBOLS) {
            if (mouseEvent.getX() < imageCanvas.getWidth() && mouseEvent.getY() < imageCanvas.getHeight() && DragResizeMod.acceptMainPaneMouseEvents) {
                if (mouseEvent.getEventType() == MouseEvent.MOUSE_PRESSED) {
                    baseRectangle.setVisible(false);
                    DragResizeMod.pressed[0] = mouseEvent.getX();
                    DragResizeMod.pressed[1] = mouseEvent.getY();
                    //System.out.printf("PRESSED: %f %f %f %f\n", baseRectangle.getLayoutX(), baseRectangle.getLayoutY(), baseRectangle.getWidth(), baseRectangle.getHeight());
                }
                if (mouseEvent.getEventType() == MouseEvent.MOUSE_DRAGGED) {
                    baseRectangle.setLayoutX(Math.min(mouseEvent.getX(), DragResizeMod.pressed[0]));
                    baseRectangle.setLayoutY(Math.min(mouseEvent.getY(), DragResizeMod.pressed[1]));
                    baseRectangle.setWidth(Math.abs(mouseEvent.getX() - DragResizeMod.pressed[0]));
                    baseRectangle.setHeight(Math.abs(mouseEvent.getY() - DragResizeMod.pressed[1]));
                    //System.out.printf("DRAGGED: %f %f %f %f\n", baseRectangle.getLayoutX(), baseRectangle.getLayoutY(), baseRectangle.getWidth(), baseRectangle.getHeight());
                    baseRectangle.setVisible(true);

                    //Main.selectionArea.showZoomedImage(TranscribedImage.current().image, baseRectangle, false);
                }
                if (mouseEvent.getEventType() == MouseEvent.MOUSE_RELEASED) {
                    baseRectangle.setLayoutX(Math.min(mouseEvent.getX(), DragResizeMod.pressed[0]));
                    baseRectangle.setLayoutY(Math.min(mouseEvent.getY(), DragResizeMod.pressed[1]));
                    baseRectangle.setWidth(Math.abs(mouseEvent.getX() - DragResizeMod.pressed[0]));
                    baseRectangle.setHeight(Math.abs(mouseEvent.getY() - DragResizeMod.pressed[1]));
                    //System.out.printf("RELEASED: %f %f %f %f\n", baseRectangle.getLayoutX(), baseRectangle.getLayoutY(), baseRectangle.getWidth(), baseRectangle.getHeight());
                    baseRectangle.setVisible(false);

                    if (baseRectangle.getWidth() > 3 && baseRectangle.getHeight() > 3) {

                        Rectangle nr = new SymbolRectangle(
                                baseRectangle.getLayoutX(), baseRectangle.getLayoutY(), baseRectangle.getWidth(),
                                baseRectangle.getHeight(), (Color) baseRectangle.getFill(), OPACITY, true);
                        mainPane.getChildren().add(nr);
                        TranscribedImage.current().add(nr);

                        CTTSApplication.symbolSelectedFromImagePane(nr);

                    }
                }
            }
        }
    }

    public enum SubMode {SYMBOLS, LINES, DECRYPTION}

    public static class ZoomHandler implements EventHandler<ScrollEvent> {

        @Override
        public void handle(ScrollEvent scrollEvent) {
            if (scrollEvent.isControlDown()) {
                if (scrollEvent.getDeltaY() > 0) {
                    zoomIn();
                } else {
                    zoomOut();
                }
            }
        }
    }
}
