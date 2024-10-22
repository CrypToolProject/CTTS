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
import org.cryptool.ctts.util.*;

import java.util.ArrayList;
import java.util.List;


public class SimulatedImagePartialDecryption extends Popup {

    static int serial = 1;
    Pane mainPane;
    int index;

    SimulatedImagePartialDecryption(int index) {
        this.index = index;
        setX(50);
        setY(50);

        Canvas canvas = new Canvas();

        mainPane = new Pane(canvas);

        Scale scale = new Scale(1.0, 1.0);
        mainPane.getTransforms().addAll(scale);

        ScrollPane scrollPane = new ScrollPane(new Group(mainPane));

        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        scrollPane.setMaxSize(2000, 1000);

        final Background globalBackground = new Background(new BackgroundFill(Color.rgb(240, 240, 255), CornerRadii.EMPTY, Insets.EMPTY));
        mainPane.setBackground(globalBackground);

        canvas.setWidth(1000);
        canvas.setHeight(500);

        VBox lines = drawLines(index);

        mainPane.getChildren().add(lines);
        getContent().addAll(scrollPane);

    }



    public static VBox drawLines(int index) {
        VBox lines = new VBox();

        ArrayList<Rectangle> allSymbols = new ArrayList<>();
        ArrayList<String> allDecryption = new ArrayList<>();
        ArrayList<ArrayList<Rectangle>> linesOfSymbols = Alignment.linesOfSymbols(index);
        for (ArrayList<Rectangle> lineOfSymbols : linesOfSymbols) {
            ArrayList<String> decryptionSequence = DetailedTranscriptionPane.decryptionSequence(lineOfSymbols);
            allSymbols.addAll(lineOfSymbols);
            allDecryption.addAll(decryptionSequence);
        }

        int size = 50;
        for (int z = 0; z < (allSymbols.size() + size - 1) / size; z++) {
            HBox line = symbolDisplayLine(CTTSApplication.key, allSymbols.subList(z * size, Math.min(allSymbols.size(), (z + 1) * size)),
                    allDecryption.subList(z * size, Math.min(allSymbols.size(), (z + 1) * size)));
            lines.getChildren().add(line);


        }
        return lines;
    }

    private static HBox symbolDisplayLine(Key key, List<Rectangle> lineOfSymbols, List<String> decryptionSequence) {
        HBox line = new HBox();
        line.setSpacing(0);
        for (int i = 0; i < lineOfSymbols.size(); i++) {
            Rectangle r = lineOfSymbols.get(i);
            SymbolStackPane sp = new SymbolStackPane();
            sp.update(key, decryptionSequence, i, r);
            line.getChildren().add(sp);
        }
        return line;
    }

    public static void simulatedImageSnapshot(int i) {
        SimulatedImagePartialDecryption p = new SimulatedImagePartialDecryption(i);
        p.show(CTTSApplication.myStage);
        p.snapshot();
        p.hide();
    }

    public void snapshot() {
        String suffix = "_" + serial++;
        FileUtils.snapshot("simulation2", TranscribedImage.transcribedImages[index].filename.replaceAll("\\..*", suffix), mainPane);

    }


    static class SymbolStackPane extends StackPane {
        ImageView icon = new ImageView();
        Text pText = new Text();

        SymbolStackPane() {

            final Background globalBackground = new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY));

            icon.setFitHeight(Utils.adjust(DetailedTranscriptionPane.ICON_SIZE));
            icon.setPreserveRatio(true);

            StackPane psp = new StackPane(pText, icon);
            psp.setMinHeight(Utils.adjust(20));
            psp.setBackground(globalBackground);

            StackPane.setMargin(pText, new Insets(Utils.adjust(0), 0, 0, 0));
            StackPane.setAlignment(icon, Pos.CENTER);
            StackPane.setAlignment(pText, Pos.CENTER);

            Region region = new Region();
            region.setMinHeight(Utils.adjust(3));
            VBox.setVgrow(region, Priority.NEVER);

            VBox vBox = new VBox();

            vBox.getChildren().add(region);
            vBox.getChildren().add(psp);

            getChildren().add(vBox);
            HBox.setMargin(this, new Insets(Utils.adjust(0), Utils.adjust(5), Utils.adjust(0), Utils.adjust(5)));

        }

        void update(Key key, List<String> decryptionSequence, int i, Rectangle r) {
            final Font pFont = Font.font("Verdana", FontWeight.BOLD, Utils.adjust(24));
            final Font pFontSmall = Font.font("Verdana", FontWeight.BOLD, Utils.adjust(16));
            pText.setFill(Color.RED);

            final String decryption = (decryptionSequence != null && decryptionSequence.size() > i) ? decryptionSequence.get(i) : "";

            Color color = (Color) r.getFill();

            final String colorString = color.toString();

            final Image image = Icons.getOrDefault(colorString, null);
            if (image != null) {
                icon.setImage(image);
            }

            String p = "";
            if (key.isKeyAvailable()) {
                p = decryption;
            }

            if (p.isEmpty() || !p.matches("[A-Z- ]+")) {
                pText.setVisible(false);
                icon.setVisible(true);
            } else {
                pText.setText(p.equals("_") ? "" : p);
                if (p.length() > 1) {
                    pText.setFont(pFontSmall);
                } else {
                    pText.setFont(pFont);
                }
                pText.setVisible(true);
                icon.setVisible(false);
            }

        }

    }

}