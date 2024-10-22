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

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.transform.Scale;
import javafx.util.Duration;
import org.cryptool.ctts.CTTSApplication;
import org.cryptool.ctts.util.*;

import java.util.*;

public class DetailedTranscriptionPane {

    public final static int ICON_SIZE = 40;
    static boolean showDecryptionContinuousText = false;
    static boolean showDecryptionContinuousTextSimplified = false;
    static boolean showImageSegment = false;
    static boolean showTranscriptionValue = false;
    static long lastDecryptionTextUpdate;
    static Map<String, SymbolStackPane> idToSp = new HashMap<>();
    static Map<Integer, HBox> lineToHbox = new TreeMap<>();
    static Map<Integer, TextField> lineToDecryptionTextField = new TreeMap<>();
    static VBox lines = null;

    public static void updateBorders(int idx) {

        for (Rectangle r : TranscribedImage.image(idx).positions()) {
            String id = TranscribedImage.rectangleToId(idx, r);
            updateSpBorder(r, id);
        }
    }

    private static void updateSpBorder(Rectangle r, String id) {
        if (CTTSApplication.selectionArea.selectedColor != null && CTTSApplication.selectionArea.selectedColor.equals(r.getFill())) {
            BorderStroke borderStroke = new BorderStroke(Color.RED, BorderStrokeStyle.SOLID, null,
                    new BorderWidths(4));
            idToSp.get(id).setBorder(new Border(borderStroke));
        } else {
            BorderStroke borderStroke = new BorderStroke(Color.rgb(240, 240, 255), BorderStrokeStyle.SOLID, null,
                    new BorderWidths(4));

            if (!showTranscriptionValue) {
                borderStroke = new BorderStroke(Color.WHITE, BorderStrokeStyle.SOLID, null,
                        new BorderWidths(4));
            }
            idToSp.get(id).setBorder(new Border(borderStroke));
        }
    }

    public static void setDetailed(boolean simple) {
        showDecryptionContinuousText = true;
        if (simple) {
            showImageSegment = false;
            showTranscriptionValue = false;
        } else {
            showImageSegment = true;
            showTranscriptionValue = true;
        }
        showDecryptionContinuousTextSimplified = true;
    }

    public static void show() {

        MainImagePane.mainPane.getChildren().clear();
        Background globalBackground = new Background(
                new BackgroundFill(Color.rgb(240, 240, 255), CornerRadii.EMPTY, Insets.EMPTY));
        if (!showTranscriptionValue) {
            globalBackground = new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY));
        }
        MainImagePane.mainPane.setBackground(globalBackground);

        lines = drawLines(TranscribedImage.currentImageIndex);
        MainImagePane.mainPane.getChildren().add(lines);

        Timeline tl = new Timeline(
                new KeyFrame(Duration.millis(300),
                        event -> {
                            restoreZoomAndScroll();
                            if (!Selection.isEmpty()) {
                                String id = Selection.getFirst();
                                Rectangle r = TranscribedImage.idToRectangle(id);
                                if (r != null) {
                                    int idx = TranscribedImage.idToIndex(id);
                                    if (idx == TranscribedImage.currentImageIndex) {
                                        MainImagePane.scrollTo(MainImagePane.scrollPane, r);
                                    }
                                }
                            }
                        }));
        tl.setCycleCount(1);
        tl.play();

    }

    public static void restoreZoomAndScroll() {
        MainImagePane.zoom(TranscribedImage.current().detailedScaleValue);
        MainImagePane.scrollPane.setVvalue(TranscribedImage.current().detailedvValue);
        MainImagePane.scrollPane.setHvalue(TranscribedImage.current().detailedhValue);
    }

    public static VBox drawLines(int index) {
        final Font italicFont = Font.font("Verdana", FontWeight.BOLD, FontPosture.ITALIC, Utils.adjust(32));
        final Font normalFont = Font.font("Verdana", FontWeight.BOLD, Utils.adjust(32));
        idToSp.clear();
        lineToHbox.clear();
        lineToDecryptionTextField.clear();

        VBox lines = new VBox();

        ArrayList<ArrayList<Rectangle>> linesOfSymbols = Alignment.linesOfSymbols(index);
        final Image transcribedImage = TranscribedImage.image(index).image;

        for (int lineNumber = 0; lineNumber < linesOfSymbols.size(); lineNumber++) {
            ArrayList<Rectangle> lineOfSymbols = linesOfSymbols.get(lineNumber);
            ArrayList<String> decryptionSequence = decryptionSequence(lineOfSymbols);

            HBox imageSegment = imageSegment(transcribedImage, lineNumber + 1, lineOfSymbols);
            HBox line = symbolDisplayLine(lineOfSymbols, decryptionSequence);
            lineToHbox.put(lineNumber, line);

            if (imageSegment != null && showImageSegment) {
                lines.getChildren().add(imageSegment);
            }
            lines.getChildren().add(line);
            if (showDecryptionContinuousText && CTTSApplication.key.isKeyAvailable()) {
                TextField decryptionContinuousText = new TextField();
                String editedRecord = EditedRecord.get(TranscribedImage.image(index).filename, lineNumber);
                if (editedRecord == null) {
                    decryptionContinuousText.setText(decryptionLineString(decryptionSequence));
                    decryptionContinuousText.setFont(normalFont);
                } else {
                    decryptionContinuousText.setText(editedRecord);
                    decryptionContinuousText.setFont(italicFont);
                }
                lastDecryptionTextUpdate = System.currentTimeMillis();
                decryptionContinuousText.setId(TranscribedImage.image(index).filename + "|" + lineNumber);
                decryptionContinuousText.textProperty().addListener((obs, oldText, newText) -> {
                    final long elapsed = System.currentTimeMillis() - lastDecryptionTextUpdate;
                    if (elapsed > 100) {
                        if (!newText.equals(oldText)) {
                            final String id = decryptionContinuousText.getId();
                            String filename = id.split("\\|")[0];
                            int ln = Integer.parseInt(id.split("\\|")[1]);
                            EditedRecord.add(filename, ln, newText);
                            decryptionContinuousText.setFont(italicFont);
                        }
                    }
                });
                decryptionContinuousText.setMinWidth(Utils.adjust(1600));
                decryptionContinuousText.setStyle("-fx-text-inner-color: blue;");
                lines.getChildren().add(new BorderPane(decryptionContinuousText));
                lineToDecryptionTextField.put(lineNumber, decryptionContinuousText);
            }
        }
        return lines;
    }

    public static void printAllDecryptedLines() {
        showDecryptionContinuousTextSimplified = true;
        for (int index = 0; index < TranscribedImage.size(); index++) {
            System.out.println(TranscribedImage.transcribedImages[index].filename);
            ArrayList<ArrayList<Rectangle>> linesOfSymbols = Alignment.linesOfSymbols(index);
            for (ArrayList<Rectangle> lineOfSymbols : linesOfSymbols) {
                ArrayList<String> decryptionSequence = decryptionSequence(lineOfSymbols);
                String decryptionLineString = decryptionLineString(decryptionSequence);
                System.out.println(decryptionLineString);
            }
        }
    }

    public static void updateSymbol(int index, String id) {
        if (index != TranscribedImage.currentImageIndex) {
            return;
        }
        ArrayList<ArrayList<Rectangle>> linesOfSymbols = Alignment.linesOfSymbols(index);
        for (int line = 0; line < linesOfSymbols.size(); line++) {
            ArrayList<Rectangle> lineOfSymbols = linesOfSymbols.get(line);
            Rectangle selected = null;
            int positionInLine = -1;
            for (int i = 0; i < lineOfSymbols.size(); i++) {
                Rectangle r = lineOfSymbols.get(i);
                if (id.equals(TranscribedImage.rectangleToId(index, r))) {
                    selected = r;
                    positionInLine = i;
                    break;
                }
            }
            if (selected == null) {
                continue;
            }
            ArrayList<String> decryptionSequence = decryptionSequence(lineOfSymbols);
            SymbolStackPane sp = idToSp.get(id);
            if (sp == null) {
                continue;
            }
            sp.update(decryptionSequence, positionInLine, selected, id);
            if (lineToDecryptionTextField.containsKey(line)) {
                if (EditedRecord.get(TranscribedImage.image(index).filename, line) == null) {
                    String lineP = decryptionLineString(decryptionSequence);
                    if (!lineP.equals(lineToDecryptionTextField.get(line).getText())) {
                        lastDecryptionTextUpdate = System.currentTimeMillis();
                        lineToDecryptionTextField.get(line).setText(lineP);
                    }
                }
            }
        }

    }

    public static void refreshColor(Color color, boolean decryptionValueChanged) {
        int index = TranscribedImage.currentImageIndex;
        ArrayList<ArrayList<Rectangle>> linesOfSymbols = Alignment.linesOfSymbols(index);
        for (int lineNumber = 0; lineNumber < linesOfSymbols.size(); lineNumber++) {
            ArrayList<Rectangle> lineOfSymbols = linesOfSymbols.get(lineNumber);
            ArrayList<String> decryptionSequence = decryptionSequence(lineOfSymbols);
            for (int i = 0; i < lineOfSymbols.size(); i++) {
                Rectangle r = lineOfSymbols.get(i);
                if (r.getFill().equals(color)) {
                    String id = TranscribedImage.rectangleToId(index, r);
                    SymbolStackPane sp = idToSp.get(id);
                    if (sp == null) {
                        continue;
                    }
                    sp.update(decryptionSequence, i, r, id);
                }
            }
            if (decryptionValueChanged) {
                if (CTTSApplication.key.isKeyAvailable()) {
                    if (lineToDecryptionTextField.containsKey(lineNumber)) {
                        if (EditedRecord.get(TranscribedImage.image(index).filename, lineNumber) == null) {
                            String lineP = decryptionLineString(decryptionSequence);
                            if (!lineP.equals(lineToDecryptionTextField.get(lineNumber).getText())) {
                                lastDecryptionTextUpdate = System.currentTimeMillis();
                                lineToDecryptionTextField.get(lineNumber).setText(lineP);
                            }
                        }
                    }
                }
            }
        }
    }

    private static String decryptionLineString(ArrayList<String> decryptionSequence) {
        StringBuilder lineP = new StringBuilder();
        if (showDecryptionContinuousTextSimplified) {
            for (String s : decryptionSequence) {
                if (s.equals("-")) {
                    continue;
                }
                lineP.append(s);
            }
            return lineP.toString().toLowerCase(Locale.ROOT);
        } else {
            for (String s : decryptionSequence) {
                if (s.equals("-")) {
                    continue;
                }
                if ((lineP.length() > 0) && !lineP.toString().endsWith(".")) {
                    lineP.append(".");
                }
                lineP.append(s);
            }
            return lineP.toString();
        }
    }

    private static HBox symbolDisplayLine(ArrayList<Rectangle> lineOfSymbols, ArrayList<String> decryptionSequence) {
        HBox line = new HBox();
        line.setSpacing(0);
        for (int i = 0; i < lineOfSymbols.size(); i++) {
            Rectangle r = lineOfSymbols.get(i);
            final String id = TranscribedImage.rectangleToId(TranscribedImage.currentImageIndex, r);
            SymbolStackPane sp = new SymbolStackPane();
            idToSp.put(id, sp);
            sp.update(decryptionSequence, i, r, id);
            line.getChildren().add(sp);
        }
        return line;
    }

    public static ArrayList<String> decryptionSequence(ArrayList<Rectangle> lineOfSymbols) {
        if (!CTTSApplication.key.isKeyAvailable()) {
            return new ArrayList<>();
        }
        return decryptionSequence(CTTSApplication.key, lineOfSymbols);
    }

    public static ArrayList<String> decryptionSequence(Key key, ArrayList<Rectangle> lineOfSymbols) {
        ArrayList<String> rawP = new ArrayList<>();
        ArrayList<String> rawC = new ArrayList<>();
        for (Rectangle r : lineOfSymbols) {
            Color color = (Color) r.getFill();
            final String colorString = color.toString();
            String c = CTTSApplication.colors.get(colorString);
            rawC.add(c);
            String p = key.fromTranscriptionOrDefault(c, c.startsWith("_") ? c : "<" + c + ">");
            if (c.equals("_[x2]")) {
                p = "[x2]";
            }
            if (c.equals("_[-]")) {
                p = "[-]";
            }
            rawP.add(p);
        }
        ArrayList<String> processedP = new ArrayList<>();
        String lastC = "";
        String lastP = "";
        for (int i = 0; i < rawP.size(); i++) {
            final String p = rawP.get(i);
            final String c = rawC.get(i);
            if (i < rawP.size() - 1 && rawP.get(i + 1).equals("[-]")) {
                processedP.add("-");
                processedP.add("-");
                i++;
                continue;
            }
            if (p.equals("[-]")) {
                processedP.add("-");
                lastC = lastP = "";
                continue;
            }
            if (p.equals("[x2]")) {
                if (!lastP.isEmpty()) {
                    processedP.add(lastP);
                } else {
                    processedP.add("-");
                }
                lastC = lastP = "";
                continue;
            }
            if (p.equals("[*]")) {
                if (!lastP.isEmpty()) {
                    String nC = lastC + ":" + c;
                    String nP = "{" + key.fromTranscriptionOrDefault("[" + nC + "]", nC) + "}";
                    int index = processedP.lastIndexOf(lastP);
                    processedP.remove(index);
                    processedP.add("-");
                    processedP.add(nP);
                } else {
                    processedP.add("-");
                }
                lastC = lastP = "";
                continue;
            }
            processedP.add(p);
            lastC = c;
            lastP = p;
        }
        return processedP;
    }

    public static void saveZoomAndScrollState() {
        TranscribedImage.current().detailedvValue = MainImagePane.scrollPane.getVvalue();
        TranscribedImage.current().detailedhValue = MainImagePane.scrollPane.getHvalue();
        TranscribedImage.current().detailedScaleValue = ((Scale) MainImagePane.mainPane.getTransforms().get(0)).getX();
    }

    private static HBox imageSegment(Image transcribedImage, int lineNumber, ArrayList<Rectangle> lineOfSymbols) {
        int symbols = lineOfSymbols.size();
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = 0;
        double maxY = 0;
        for (Rectangle r : lineOfSymbols) {
            minX = Math.min(minX, r.getLayoutX());
            minY = Math.min(minY, r.getLayoutY());
            maxX = Math.max(maxX, r.getLayoutX() + r.getWidth());
            maxY = Math.max(maxY, r.getLayoutY() + r.getHeight());
        }
        if ((minX < maxX) && (minY < maxY)) {
            minX = Math.max(0, minX - 30);
            minY = Math.max(0, minY - 5);
            maxX = Math.min(transcribedImage.getWidth(), maxX + 30);
            maxY = Math.min(transcribedImage.getHeight(), maxY + 5);
            if (maxX <= minX || maxY <= minY) {
                return new HBox();
            }
            ImageView imageView = new ImageView(transcribedImage);
            Rectangle2D viewport = new Rectangle2D(minX, minY, maxX - minX, maxY - minY);
            imageView.setViewport(viewport);
            imageView.setFitWidth(Utils.adjust(symbols * 1.25 * ICON_SIZE));
            imageView.setFitHeight(Utils.adjust(200));
            imageView.setPreserveRatio(true);
            final Font hugeFont = Font.font("Verdana", FontWeight.NORMAL, Utils.adjust(36));
            final Text t1 = new Text("" + lineNumber);
            t1.setFont(hugeFont);
            final Text t2 = new Text("" + lineNumber);
            t2.setFont(hugeFont);
            final HBox hBox = new HBox(t1, imageView, t2);
            hBox.setStyle("-fx-border-color: black;");
            return hBox;
        }
        return null;
    }

    static class SymbolStackPane extends StackPane {
        final Text cText = new Text();
        ImageView icon = new ImageView();
        Text pText = new Text();
        ImageView symbol = new ImageView();

        SymbolStackPane() {
            Background globalBackground = new Background(
                    new BackgroundFill(Color.rgb(240, 240, 255), CornerRadii.EMPTY, Insets.EMPTY));
            if (!showTranscriptionValue) {
                globalBackground = new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY));
            }
            icon.setFitHeight(Utils.adjust(ICON_SIZE));
            icon.setPreserveRatio(true);
            StackPane.setAlignment(cText, Pos.BOTTOM_CENTER);
            StackPane psp = new StackPane(pText);
            if (!showTranscriptionValue) {
                pText.setFill(Color.RED);
            }
            psp.setMinHeight(Utils.adjust(20));
            psp.setBackground(globalBackground);
            StackPane.setMargin(pText, new Insets(Utils.adjust(0), 0, 0, 0));
            StackPane.setAlignment(pText, Pos.BOTTOM_CENTER);
            symbol.setOnDragDetected((MouseEvent event) -> {
                Dragboard db = ((ImageView) event.getTarget()).startDragAndDrop(TransferMode.COPY);
                ClipboardContent content = new ClipboardContent();
                WritableImage image = ((ImageView) event.getTarget()).snapshot(new SnapshotParameters(), null);
                content.putImage(image);
                content.putString(((ImageView) event.getTarget()).getId());
                db.setContent(content);
                event.consume();
            });
            symbol.setFitHeight(Utils.adjust(ICON_SIZE));
            symbol.setPreserveRatio(true);
            Region region = new Region();
            region.setMinHeight(Utils.adjust(5));
            VBox.setVgrow(region, Priority.NEVER);
            Region region2 = new Region();
            region2.setMinHeight(Utils.adjust(5));
            VBox.setVgrow(region2, Priority.ALWAYS);
            region2.setBackground(globalBackground);
            StackPane sPane = new StackPane(symbol);
            sPane.setMinHeight(Utils.adjust(ICON_SIZE));
            sPane.setMaxHeight(Utils.adjust(ICON_SIZE));
            StackPane.setAlignment(symbol, Pos.CENTER);
            Pane iPane = new Pane(icon);
            sPane.setMinHeight(Utils.adjust(ICON_SIZE));
            sPane.setMaxHeight(Utils.adjust(ICON_SIZE));
            VBox vBox = new VBox();
            vBox.getChildren().add(sPane);
            if (showTranscriptionValue) {
                vBox.getChildren().add(new StackPane(cText));
            }
            vBox.getChildren().add(iPane);
            if (CTTSApplication.key.isKeyAvailable()) {
                if (showTranscriptionValue) {
                    vBox.getChildren().add(region);
                }
                vBox.getChildren().add(psp);
            }
            vBox.getChildren().add(region2);
            StackPane.setAlignment(cText, Pos.CENTER);
            StackPane.setAlignment(symbol, Pos.CENTER);
            StackPane.setAlignment(icon, Pos.CENTER);
            StackPane.setAlignment(pText, Pos.CENTER);
            getChildren().add(vBox);
            HBox.setMargin(this, new Insets(Utils.adjust(0), Utils.adjust(3), Utils.adjust(0), Utils.adjust(3)));
        }

        void update(ArrayList<String> decryptionSequence, int i, Rectangle r, String id) {
            final Font cFont = Font.font("Verdana", FontWeight.NORMAL, Utils.adjust(16));
            final Font pFont = Font.font("Verdana", FontWeight.BOLD, Utils.adjust(24));
            final Font pFontSmall = Font.font("Verdana", FontWeight.BOLD, Utils.adjust(16));

            final String decryption = (decryptionSequence != null && decryptionSequence.size() > i)
                    ? decryptionSequence.get(i)
                    : "";

            Color color = (Color) r.getFill();
            Color symbolBackgroundColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), 0.4);
            if (!showTranscriptionValue) {
                symbolBackgroundColor = Color.WHITE;
            }
            final String colorString = color.toString();
            String c = CTTSApplication.colors.get(colorString);
            if (Selection.contains(id)) {
                symbolBackgroundColor = Color.RED;
            }
            icon.setImage(Icons.getOrDefault(colorString, null));
            cText.setText(c);
            String p = "";
            if (CTTSApplication.key.isKeyAvailable()) {
                cText.setFont(cFont);
                p = decryption;
            } else {
                cText.setFont(pFont);
            }
            if (Selection.contains(id)) {
                symbol.setImage(TranscribedImage.current().negative);
            } else {
                symbol.setImage(TranscribedImage.current().image);
            }
            Rectangle2D viewport = new Rectangle2D(r.getLayoutX(), r.getLayoutY(), r.getWidth(), r.getHeight());
            symbol.setViewport(viewport);
            pText.setText(p);
            if (p.length() > 1) {
                pText.setFont(pFontSmall);
            } else {
                pText.setFont(pFont);
            }
            setBackground(new Background(new BackgroundFill(symbolBackgroundColor, null, null)));
            Utils.recursiveSetId(this, id);
            updateSpBorder(r, id);
        }
    }
}