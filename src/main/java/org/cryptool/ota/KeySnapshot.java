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

package org.cryptool.ota;

import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.transform.Scale;
import javafx.stage.Popup;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;


public class KeySnapshot extends Popup {
    private final static String RAW_PLAINTEXT_LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZÀÁÃÅΆĄÂªªÇČÐĎΛĚÊÈÉĘËĮÎÌÍÏŁŇŃÑØÒÓÔŐÕΘº°ǪΦÞŘŔŠ§ŤÚŰÙÛŮ×ÝŻŽŹ";

    private final static String RAW_PLAINTEXT_LETTERS_LOWER = "ABCDEFGHIJKLMNOPQRSTUVWXYZÀÁÃÅΆĄÂªªÇČÐĎΛĚÊÈÉĘËĮÎÌÍÏŁŇŃÑØÒÓÔŐÕΘº°ǪΦÞŘŔŠ§ŤÚŰÙÛŮ×ÝŻŽŹ".toLowerCase();

    private Pane mainPane;

    private KeySnapshot() {
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

        final Background globalBackground = new Background(new BackgroundFill(Color.rgb(240, 240, 255), CornerRadii.EMPTY, Insets.EMPTY));
        mainPane.setBackground(globalBackground);
        canvas.setWidth(100);
        canvas.setHeight(100);

        Set<String> letters = new TreeSet<>();
        for (String v : Main.key.values()) {
            if (v.length() == 1 && (RAW_PLAINTEXT_LETTERS.contains(v) || RAW_PLAINTEXT_LETTERS_LOWER.contains(v))) {
                letters.add(v);
            }
        }
        Set<String> doubles = new TreeSet<>();
        for (String v : Main.key.values()) {
            if (v.length() == 2 && (RAW_PLAINTEXT_LETTERS.contains(v.substring(0, 1)) || RAW_PLAINTEXT_LETTERS_LOWER.contains(v.substring(0, 1))) && v.charAt(0) == v.charAt(1)) {
                doubles.add(v);
            }
        }
        ArrayList<String> others = new ArrayList<>();
        for (String colorString : Main.colors.sortedColors()) {
            String c = Main.colors.get(colorString);
            if (c == null || c.isEmpty()) {
                continue;
            }
            String v = Main.key.get(c);
            if (v != null && !v.isEmpty() && !letters.contains(v) && !doubles.contains(v)) {
                if (!others.contains(v)) {
                    others.add(v);
                }
            }
        }

        VBox all = new VBox();
        BorderStroke borderStroke = new BorderStroke(Color.BLUE, BorderStrokeStyle.SOLID, null, new BorderWidths(1));

        VBox title = new VBox();
        Text collection = new Text(FileUtils.currentDirectoryString());
        final Font verdana = Font.font("Verdana", FontWeight.BOLD, 24);
        collection.setFont(verdana);
        title.getChildren().add(collection);
        final String fs = FileUtils.currentDirectoryFullpathString();
        if (fs.contains("F:\\Cryptology") && !fs.contains("2988")) {
            DateFormat outputFormatter = new SimpleDateFormat("dd/MM/yyyy");
            String output = outputFormatter.format(System.currentTimeMillis());
            Text george = new Text("George Lasry " + output);
            title.getChildren().add(george);
        }
        StackPane sp0 = new StackPane(title);
        sp0.setBorder(new Border(borderStroke));
        StackPane.setAlignment(sp0, Pos.CENTER);
        all.getChildren().add(sp0);

        HBox lettersHBox = alphabet(letters);
        HBox doublesHBox = alphabet(doubles);

        if (lettersHBox != null || doublesHBox != null) {

            final VBox vBox = new VBox();
            if (lettersHBox != null) {
                vBox.getChildren().add(lettersHBox);
            }
            if (doublesHBox != null) {
                vBox.getChildren().add(doublesHBox);
            }
            StackPane sp = new StackPane(vBox);
            sp.setBorder(new Border(borderStroke));
            all.getChildren().add(sp);
        }

        TilePane othersTilePane = others(others);
        if (othersTilePane != null) {
            StackPane sp = new StackPane(othersTilePane);
            sp.setBorder(new Border(borderStroke));
            all.getChildren().add(sp);
        }

        VBox nulls = specific("_", "Space or null");
        if (nulls != null) {
            StackPane sp = new StackPane(nulls);
            sp.setBorder(new Border(borderStroke));
            StackPane.setAlignment(sp, Pos.CENTER);
            all.getChildren().add(sp);
        }

        VBox unknown = specific("?", "Unknown");
        if (unknown != null) {
            StackPane sp = new StackPane(unknown);
            sp.setBorder(new Border(borderStroke));
            StackPane.setAlignment(sp, Pos.CENTER);
            all.getChildren().add(sp);
        }



        mainPane.getChildren().add(all);
        getContent().addAll(scrollPane);

    }

    private HBox alphabet(Set<String> letters) {
        HBox lettersHBox = new HBox();
        final Font verdana = Font.font("Verdana", FontWeight.BOLD, 28);
        final Background letterBg = new Background(new BackgroundFill(Color.LIGHTGRAY, CornerRadii.EMPTY, Insets.EMPTY));
        for (String letter : letters) {
            boolean add = false;
            if (!lettersHBox.getChildren().isEmpty()) {
                Region region = new Region();
                region.setMinWidth(10);

                lettersHBox.getChildren().add(region);
            }

            VBox vBox = new VBox();
            final Text text = new Text(letter);
            text.setFont(verdana);
            text.setFill(Color.BLUE);
            StackPane sp = new StackPane(text);
            StackPane.setAlignment(text, Pos.CENTER);
            sp.setBackground(letterBg);
            vBox.getChildren().add(sp);
            for (String colorString : Main.colors.keySet()) {
                final String transcription = Main.colors.get(colorString);
                if (transcription == null || transcription.isEmpty()) {
                    continue;
                }
                final String decryption = Main.key.get(transcription);
                if (decryption == null || decryption.isEmpty()) {
                    continue;
                }
                final Image image = Icons.get(colorString);
                if (decryption.equals(letter) && image != null && image.getHeight() != 0.0) {
                    add = true;
                    final ImageView iv = new ImageView(image);
                    iv.setFitHeight(35);
                    iv.setFitWidth(35);
                    iv.setPreserveRatio(true);

                    StackPane spI = new StackPane(iv);
                    spI.setMinSize(35, 35);
                    spI.setMaxSize(35, 35);
                    StackPane.setAlignment(iv, Pos.CENTER);
                    vBox.getChildren().add(spI);
                }
            }
            if (add) {
                lettersHBox.getChildren().add(vBox);
            }
        }
        if (lettersHBox.getChildren().isEmpty()) {
            return null;
        }
        return lettersHBox;
    }

    private TilePane others(Collection<String> words) {
        TilePane tilePane = new TilePane();
        tilePane.setOrientation(Orientation.VERTICAL);

        int count = 0;
        for (String word : words) {

            for (String colorString : Main.colors.keySet()) {
                final String transcription = Main.colors.get(colorString);
                if (transcription == null || transcription.isEmpty()) {
                    continue;
                }
                final String decryption = Main.key.get(transcription);
                if (decryption == null || decryption.isEmpty() || decryption.equals("?") || decryption.equals("_")) {
                    continue;
                }
                final Image image = Icons.get(colorString);
                if (decryption.equals(word) && image != null && image.getHeight() != 0.0) {
                    count++;
                }
            }
        }


        tilePane.setPrefRows((count + 3)/ 4);

        final Font verdana = Font.font("Verdana", FontWeight.BOLD, 12);
        for (String word : words) {
            HBox hBox = new HBox();
            boolean add = false;

            for (String colorString : Main.colors.keySet()) {
                final String transcription = Main.colors.get(colorString);
                if (transcription == null || transcription.isEmpty()) {
                    continue;
                }
                final String decryption = Main.key.get(transcription);
                if (decryption == null || decryption.isEmpty() || decryption.equals("?") || decryption.equals("_")) {
                    continue;
                }
                final Image image = Icons.get(colorString);
                if (decryption.equals(word) && image != null && image.getHeight() != 0.0) {
                    final ImageView iv = new ImageView(image);
                    iv.setFitHeight(35);
                    iv.setFitWidth(35);
                    iv.setPreserveRatio(true);

                    StackPane spI = new StackPane(iv);
                    spI.setMinSize(35, 35);
                    spI.setMaxSize(35, 35);
                    StackPane.setAlignment(iv, Pos.CENTER);

                    hBox.getChildren().add(spI);

                    Region r = new Region();
                    r.setMinWidth(20);
                    hBox.getChildren().add(r);

                    add = true;
                }
            }
            if (add) {
                final Text text = new Text(word);
                text.setFont(verdana);
                text.setFill(Color.BLUE);
                StackPane sp = new StackPane(text);
                StackPane.setAlignment(text, Pos.CENTER);
                hBox.getChildren().add(sp);
                tilePane.getChildren().add(hBox);
            }

        }
        if (tilePane.getChildren().isEmpty()) {
            return null;
        }
        return tilePane;
    }

    private VBox specific(String value, String desc) {


        ArrayList<HBox> hBoxes = new ArrayList<>();
        HBox hBox = new HBox();

        for (String colorString : Main.colors.sortedColors()) {
            final String transcription = Main.colors.get(colorString);
            if (transcription == null || transcription.isEmpty()) {
                continue;
            }
            final String decryption = Main.key.get(transcription);

            if (value.equals("?")) {
                if (decryption != null && !decryption.isEmpty() && !decryption.equals(value)) {
                    continue;
                }
            } else {
                if (decryption == null || !decryption.equals(value)) {
                    continue;
                }
            }
            final Image image = Icons.get(colorString);
            if (image != null && image.getHeight() != 0.0) {
                final ImageView iv = new ImageView(image);
                iv.setFitHeight(35);
                iv.setFitWidth(35);
                iv.setPreserveRatio(true);

                StackPane spI = new StackPane(iv);
                spI.setMinSize(35, 35);
                spI.setMaxSize(35, 35);
                StackPane.setAlignment(iv, Pos.CENTER);
                if (!hBox.getChildren().isEmpty()) {
                    Region r = new Region();
                    r.setMinWidth(20);
                    hBox.getChildren().add(r);
                }
                hBox.getChildren().add(spI);
                if (hBox.getChildren().size() >= 30) {
                    hBoxes.add(hBox);
                    hBox = new HBox();
                }
            }
        }


        if (!hBox.getChildren().isEmpty()) {
            hBoxes.add(hBox);
        }
        if (hBoxes.isEmpty()) {
            return null;
        }


        final Font verdana = Font.font("Verdana", FontWeight.BOLD, 12);
        Text text = new Text(desc);
        text.setFont(verdana);
        text.setFill(Color.BLUE);
        VBox vBox = new VBox(text);
        vBox.getChildren().addAll(hBoxes);
        return vBox;
    }

    public static void keySnapshot() {

        if (!Main.key.isKeyAvailable()) {
            return;
        }


        KeySnapshot k = new KeySnapshot();
        k.show(Main.myStage);
        k.snapshot();
        k.hide();

    }

    public void snapshot() {

        FileUtils.snapshot("snapshots", "decryption key", mainPane);

    }
}