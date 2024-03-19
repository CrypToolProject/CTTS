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
import org.cryptool.ctts.CTTSApplication;
import org.cryptool.ctts.util.FileUtils;
import org.cryptool.ctts.util.Icons;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;


public class KeySnapshot extends Popup {
    final static String RAW_PLAINTEXT_LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZÀÁÃÅΆĄÂªªÇČÐĎΛĚÊÈÉĘËĮÎÌÍÏŁŇŃÑØÒÓÔŐÕΘº°ǪΦÞŘŔŠ§ŤÚŰÙÛŮ×ÝŻŽŹ";

    final static String RAW_PLAINTEXT_LETTERS_LOWER = "ABCDEFGHIJKLMNOPQRSTUVWXYZÀÁÃÅΆĄÂªªÇČÐĎΛĚÊÈÉĘËĮÎÌÍÏŁŇŃÑØÒÓÔŐÕΘº°ǪΦÞŘŔŠ§ŤÚŰÙÛŮ×ÝŻŽŹ".toLowerCase();

    final static String RAW_VOWELS = "AEIOUÀÁÃÅΆĄÂĚÊÈÉĘËĮÎÌÍÏØÒÓÔŐÕΘÚŰÙÛŮÝV";
    final static String RAW_VOWELS_LOWER = "AEIOUÀÁÃÅΆĄÂĚÊÈÉĘËĮÎÌÍÏØÒÓÔŐÕΘÚŰÙÛŮÝV".toLowerCase();

    final static String RAW_CONSONANTS = "BCDFGHJKLMNPQRSTWXZÇČĎŇŃÑŘŔŠŤŻŽŹ";
    final static String RAW_CONSONANTS_LOWER = "BCDFGHJKLMNPQRSTWXZÇČĎŇŃÑŘŔŠŤŻŽŹ".toLowerCase();

    final static double ICON_SIZE = 35;

    private final Pane mainPane;

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

        final Background globalBackground = new Background(
                new BackgroundFill(Color.rgb(240, 240, 255), CornerRadii.EMPTY, Insets.EMPTY));
        mainPane.setBackground(globalBackground);
        canvas.setWidth(100);
        canvas.setHeight(100);

        Set<String> letters = new TreeSet<>();
        Set<String> doubles = new TreeSet<>();
        Set<String> cv = new TreeSet<>();
        Set<String> vc = new TreeSet<>();
        Set<String> nomenclature = new TreeSet<>();

        for (String colorString : CTTSApplication.colors.sortedColors()) {
            String c = CTTSApplication.colors.get(colorString);
            if (c == null || c.isEmpty()) {
                continue;
            }
            String p = CTTSApplication.key.get(c);
            if (p == null || p.isEmpty()) {
                continue;
            }
            if (isLetter(p)) {
                letters.add(p);
                continue;
            }

            if (isDouble(p)) {
                doubles.add(p);
                continue;
            }
            if (isSyllableVC(p)) {
                vc.add(p);
                continue;
            }
            if (isSyllableCV(p)) {
                cv.add(p);
                continue;
            }
            if (!p.startsWith("_") && !p.startsWith("?")) {
                nomenclature.add(p);
            }
        }

        if (vc.size() < 20) {
            nomenclature.addAll(vc);
            vc.clear();
        }
        if (cv.size() < 20) {
            nomenclature.addAll(cv);
            cv.clear();
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
        String desc = "Consonant-Vowel Syllables";
        TilePane cvTilePane = others(cv, letters.size());
        if (cvTilePane != null) {
            final Font verdana2 = Font.font("Verdana", FontWeight.BOLD, 12);
            Text text = new Text(desc);
            text.setFont(verdana2);
            text.setFill(Color.BLUE);
            VBox vBox = new VBox(text, cvTilePane);
            StackPane sp = new StackPane(vBox);
            sp.setBorder(new Border(borderStroke));
            all.getChildren().add(sp);
        }

        TilePane vcTilePane = others(vc, letters.size());
        if (vcTilePane != null) {
            final Font verdana2 = Font.font("Verdana", FontWeight.BOLD, 12);
            Text text = new Text("Vowel-Consonant Syllables");
            text.setFont(verdana2);
            text.setFill(Color.BLUE);
            VBox vBox = new VBox(text, vcTilePane);
            StackPane sp = new StackPane(vBox);
            sp.setBorder(new Border(borderStroke));
            StackPane.setAlignment(sp, Pos.CENTER);
            all.getChildren().add(sp);
        }

        TilePane othersTilePane = others(nomenclature, letters.size());
        if (othersTilePane != null) {
            final Font verdana2 = Font.font("Verdana", FontWeight.BOLD, 12);
            Text text = new Text("Nomenclature");
            text.setFont(verdana2);
            text.setFill(Color.BLUE);
            VBox vBox = new VBox(text, othersTilePane);
            StackPane sp = new StackPane(vBox);
            sp.setBorder(new Border(borderStroke));
            StackPane.setAlignment(sp, Pos.CENTER);
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

    private static boolean isLetter(String p) {
        if (p.length() > 1) {
            return false;
        }
        return RAW_PLAINTEXT_LETTERS.contains(p) || RAW_PLAINTEXT_LETTERS_LOWER.contains(p);
    }

    private static boolean isLetter(char p) {
        return isLetter("" + p);
    }

    private static boolean isDouble(String p) {
        if (p.length() != 2) {
            return false;
        }
        char c1 = p.charAt(0);
        char c2 = p.charAt(1);
        return isLetter(c1) && c1 == c2;
    }

    private static boolean isVowel(String p) {
        return RAW_VOWELS.contains(p) || RAW_VOWELS_LOWER.contains(p);
    }

    private static boolean isVowel(char p) {
        return isVowel("" + p);
    }

    private static boolean isConsonant(String p) {
        return RAW_CONSONANTS.contains(p) || RAW_CONSONANTS_LOWER.contains(p);
    }

    private static boolean isConsonant(char p) {
        return isConsonant("" + p);
    }

    private static boolean isSyllableCV(String p) {
        if (p.length() == 3) {
            char c1 = p.charAt(0);
            char c2 = p.charAt(1);
            char c3 = p.charAt(2);
            if (!isVowel(c3)) {
                return false;
            }
            if (isConsonant(c1) && isConsonant(c2)) {
                return true;
            }
            String start = p.substring(0, 2).toLowerCase();
            return start.equals("qu") || start.equals("qv");
        }
        if (p.length() != 2) {
            return false;
        }
        char c1 = p.charAt(0);
        char c2 = p.charAt(1);
        return isConsonant(c1) && isVowel(c2);
    }

    private static boolean isSyllableVC(String p) {
        if (p.length() != 2) {
            return false;
        }
        char c1 = p.charAt(0);
        char c2 = p.charAt(1);
        return isConsonant(c2) && isVowel(c1);
    }

    public static void keySnapshot() {

        if (!CTTSApplication.key.isKeyAvailable()) {
            return;
        }


        KeySnapshot k = new KeySnapshot();
        k.show(CTTSApplication.myStage);
        k.snapshot();
        k.hide();

    }

    private HBox alphabet(Set<String> letters) {
        HBox lettersHBox = new HBox();
        final Font verdana = Font.font("Verdana", FontWeight.BOLD, 28);
        final Background letterBg = new Background(
                new BackgroundFill(Color.LIGHTGRAY, CornerRadii.EMPTY, Insets.EMPTY));
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
            for (String colorString : CTTSApplication.colors.sortedColors()) {
                final String transcription = CTTSApplication.colors.get(colorString);
                if (transcription == null || transcription.isEmpty()) {
                    continue;
                }
                final String decryption = CTTSApplication.key.get(transcription);
                if (decryption == null || decryption.isEmpty() || !decryption.equals(letter)) {
                    continue;
                }
                final Image image = Icons.get(colorString);
                if (image == null || image.getHeight() == 0.0) {
                    continue;
                }
                add = true;
                final ImageView iv = new ImageView(image);
                iv.setFitHeight(ICON_SIZE);
                iv.setFitWidth(ICON_SIZE);
                iv.setPreserveRatio(true);

                StackPane spI = new StackPane(iv);
                spI.setMinSize(ICON_SIZE, ICON_SIZE);
                spI.setMaxSize(ICON_SIZE, ICON_SIZE);
                StackPane.setAlignment(iv, Pos.CENTER);
                vBox.getChildren().add(spI);
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

    private TilePane others(Collection<String> words, int alphabetSize) {
        if (words.isEmpty()) {
            return null;
        }
        TilePane tilePane = new TilePane();
        tilePane.setOrientation(Orientation.VERTICAL);

        int maxItemWidth = 0;
        for (String word : words) {
            int symbolCount = 0;
            for (String colorString : CTTSApplication.colors.sortedColors()) {
                final String transcription = CTTSApplication.colors.get(colorString);
                if (transcription == null || transcription.isEmpty()) {
                    continue;
                }
                final String decryption = CTTSApplication.key.get(transcription);
                if (decryption == null || !decryption.equals(word)) {
                    continue;
                }
                symbolCount++;

            }
            maxItemWidth = Math.max(10 + symbolCount * 42 + word.length() * 7, maxItemWidth);
        }

        if (maxItemWidth > 0) {
            int perLine = (1000 * alphabetSize / 26) / maxItemWidth;
            if (perLine != 0) {
                tilePane.setPrefRows((words.size() + perLine - 1) / perLine);
            }
        }

        final Font verdana = Font.font("Verdana", FontWeight.BOLD, 12);
        for (String word : words) {
            HBox hBox = new HBox();
            boolean add = false;

            for (String colorString : CTTSApplication.colors.sortedColors()) {
                final String transcription = CTTSApplication.colors.get(colorString);
                if (transcription == null || transcription.isEmpty()) {
                    continue;
                }
                final String decryption = CTTSApplication.key.get(transcription);
                if (decryption == null || !decryption.equals(word)) {
                    continue;
                }
                final Image image = Icons.get(colorString);
                if (decryption.equals(word) && image != null && image.getHeight() != 0.0) {
                    final ImageView iv = new ImageView(image);
                    iv.setFitHeight(ICON_SIZE);
                    iv.setFitWidth(ICON_SIZE);
                    iv.setPreserveRatio(true);

                    StackPane spI = new StackPane(iv);
                    spI.setMinSize(ICON_SIZE, ICON_SIZE);
                    spI.setMaxSize(ICON_SIZE, ICON_SIZE);
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

        for (String colorString : CTTSApplication.colors.sortedColors()) {
            final String transcription = CTTSApplication.colors.get(colorString);
            if (transcription == null || transcription.isEmpty()) {
                continue;
            }
            final String decryption = CTTSApplication.key.get(transcription);

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
                iv.setFitHeight(ICON_SIZE);
                iv.setFitWidth(ICON_SIZE);
                iv.setPreserveRatio(true);

                StackPane spI = new StackPane(iv);
                spI.setMinSize(ICON_SIZE, ICON_SIZE);
                spI.setMaxSize(ICON_SIZE, ICON_SIZE);
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

    public void snapshot() {

        FileUtils.snapshot("snapshots", "decryption key", mainPane);

    }
}