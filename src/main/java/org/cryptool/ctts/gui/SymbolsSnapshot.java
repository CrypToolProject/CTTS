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

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.transform.Scale;
import javafx.stage.Popup;
import org.cryptool.ctts.CTTSApplication;
import org.cryptool.ctts.util.FileUtils;
import org.cryptool.ctts.util.Icons;
import org.cryptool.ctts.util.TranscribedImage;

import java.util.ArrayList;

public class SymbolsSnapshot extends Popup {

    Pane mainPane;
    boolean snapshot;

    SymbolsSnapshot(boolean snapshot, boolean details) {
        this.snapshot = snapshot;
        setX(50);
        setY(50);
        Canvas canvas = new Canvas();

        mainPane = new Pane(canvas);

        Scale scale = new Scale(1.0, 1.0);
        mainPane.getTransforms().addAll(scale);

        ScrollPane scrollPane = new ScrollPane(new Group(mainPane));
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        scrollPane.setMaxSize(2100, 1200);

        final Background globalBackground = new Background(
                new BackgroundFill(Color.rgb(240, 240, 255), CornerRadii.EMPTY, Insets.EMPTY));
        mainPane.setBackground(globalBackground);
        canvas.setWidth(1000);
        canvas.setHeight(600);
        ArrayList<String> usedColors = CTTSApplication.colors.sortedColors();

        HBox columns = new HBox();

        VBox lines = new VBox();

        for (String color : usedColors) {

            final HBox line = line(color, Math.min(10, 1300 / (CTTSApplication.key.keySize() + 1)), details);
            if (line != null) {
                lines.getChildren().add(line);
                if (lines.getChildren().size() == (details ? 45 : 20)) {
                    columns.getChildren().add(lines);
                    lines = new VBox();
                }
            }
        }
        columns.getChildren().add(lines);

        mainPane.getChildren().add(columns);
        getContent().addAll(scrollPane);

    }

    private static HBox line(String item, int maxSymbols, boolean details) {

        Color color = Color.valueOf(item);
        Color newColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), 0.4);

        String formatted = CTTSApplication.colors.get(item);
        if (formatted.isEmpty()) {
            return null;
        }
        while (formatted.length() < 10) {
            formatted = " " + formatted + " ";
        }
        formatted = formatted.substring(0, 10);
        Text ciphertextText = new Text(formatted);
        ciphertextText.setFont(Font.font("Verdana", FontWeight.BOLD, 16));
        //ciphertextText.setFont(new Font(16));
        StackPane ciphertextSymbolPane = new StackPane(ciphertextText);
        final Background bg = new Background(new BackgroundFill(newColor, CornerRadii.EMPTY, Insets.EMPTY));
        ciphertextSymbolPane.setBackground(bg);
        HBox hBox = new HBox();

        ImageView iconImageView = new ImageView();
        iconImageView.setPreserveRatio(true);
        iconImageView.setFitWidth(25);
        iconImageView.setFitHeight(25);

        StackPane iconSymbolPane = new StackPane(iconImageView);
        iconSymbolPane.setMinWidth(30);
        iconSymbolPane.setMaxWidth(30);
        iconSymbolPane.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY)));
        iconSymbolPane.setMinSize(40, 25);
        StackPane.setAlignment(iconImageView, Pos.CENTER);

        Image icon = Icons.get(color.toString());
        if (icon != null) {
            iconImageView.setImage(icon);
        } else if (!details) {
            return null;
        }

        hBox.getChildren().add(iconSymbolPane);
        if (details) {
            hBox.getChildren().add(ciphertextSymbolPane);
        }
        ciphertextSymbolPane.setMinSize(100, 25);

        if (CTTSApplication.key.isKeyAvailable()) {

            String plaintext = "";
            if (CTTSApplication.key.fromColorStringAvailable(item)) {
                plaintext = CTTSApplication.key.fromColorString(item);
            }

            if (plaintext.length() > 17) {
                plaintext = plaintext.substring(0, 17) + "...";
            }
            Text plaintextText = new Text(plaintext);
            if (plaintext.length() <= 5) {
                plaintextText.setFont(new Font(16));
            } else if (plaintext.length() <= 10) {
                plaintextText.setFont(new Font(12));
            } else {
                plaintextText.setFont(new Font(8));
            }
            StackPane plaintextSymbolPane = new StackPane(plaintextText);

            plaintextSymbolPane.setMinSize(100, 25);
            //plaintextSymbolPane.setBackground(new Background(new BackgroundFill(newColor, CornerRadii.EMPTY, Insets.EMPTY)));

            if (details) {
                hBox.getChildren().add(plaintextSymbolPane);
            }

        }

        int count = 0;
        for (Rectangle r : TranscribedImage.symbolsOfType(color)) {
            int index = TranscribedImage.idToIndex(r.getId());
            if (r.getWidth() > 3 * r.getHeight() || r.getHeight() > 3 * r.getWidth()) {
                continue;
            }
            ImageView imageView = new ImageView(TranscribedImage.image(index).image);
            Rectangle2D viewport = new Rectangle2D(r.getLayoutX(), r.getLayoutY(), r.getWidth(), r.getHeight());
            imageView.setViewport(viewport);
            //imageView.setFitWidth(25);
            imageView.setPreserveRatio(true);
            imageView.setFitHeight(25);

            if (details) {
                hBox.getChildren().add(imageView);
            }

            imageView.setId(TranscribedImage.rectangleToId(index, r));

            if (++count == maxSymbols) {
                return hBox;
            }
        }

        return hBox;
    }

    public static void keySnapshot() {

        SymbolsSnapshot k = new SymbolsSnapshot(true, true);
        k.show(CTTSApplication.myStage);
        k.snapshot(true);
        k.hide();

        k = new SymbolsSnapshot(true, false);
        k.show(CTTSApplication.myStage);
        k.snapshot(false);
        k.hide();

    }

    public void snapshot(boolean details) {

        FileUtils.snapshot("snapshots", details ? "key" : "symbols", mainPane);

    }
}