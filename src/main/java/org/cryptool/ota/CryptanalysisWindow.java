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

import static org.cryptool.ota.DetailedTranscriptionPane.ICON_SIZE;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.transform.Scale;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

public class CryptanalysisWindow {

    static boolean showKeyOnTopOfDecryption = false; // show the key on the right (=false) or on top of decryption (yes)

    static Stage myDialog;

    static boolean callback;

    final static CryptanalysisParameters params = new CryptanalysisParameters(Language.FRENCH);

    static ChoiceBox<String> languageChoiceBox = new ChoiceBox<>();
    static ChoiceBox<String> ngramChoiceBox = new ChoiceBox<>();
    static CheckBox uToV = new CheckBox("U => V");
    static CheckBox wToV = new CheckBox("W => V");
    static CheckBox jToI = new CheckBox("J => I");
    static CheckBox yToI = new CheckBox("Y => I");
    static CheckBox zToS = new CheckBox("Z => S");
    static CheckBox kToC = new CheckBox("K => C");
    static CheckBox removeSpaces = new CheckBox("Spaces");
    static CheckBox removeDoubledLetters = new CheckBox("Doubled Letters");
    static CheckBox removeX = new CheckBox("X");
    static CheckBox removeH = new CheckBox("H");
    static ChoiceBox<String> maxHomophonesChoiceBox = new ChoiceBox<>();
    static ChoiceBox<String> minCountChoiceBox = new ChoiceBox<>();
    static ChoiceBox<String> minMatchingLengthForLockingChoiceBox = new ChoiceBox<>();
    static CheckBox ignoreCurrentKey = new CheckBox("Ignore current key");

    static VBox decryptionVBox = new VBox();
    static VBox keyVBox = new VBox();

    static Text plaintextSymbolTypes = new Text();
    static Text assignableCiphertextSymbolTypes = new Text();
    static Text ciphertextSymbols = new Text();
    static Text ciphertextSymbolsFiltered = new Text();
    static Text ciphertextSymbolTypes = new Text();
    static Text ciphertextSymbolTypesFiltered = new Text();

    static Text comments = new Text();

    static Circle blink = new Circle(Utils.adjust(20));
    static Button startStop = new Button("Start Cryptanalysis");
    static Button save = new Button("Save Key");
    static Button close = new Button("Close");
    final static String initialResultsText = "Set the parameters above and press Start Cryptanalysis.";
    static FadeTransition fadeTransition;

    static ProgressBar progressBar = new ProgressBar();

    static Timeline tl = null;
    static AtomicBoolean slowUpdate = new AtomicBoolean(false);

    static int iteration = 0;

    public static void show(final boolean slowUpdate) {
        callback = false;
        CryptanalysisWindow.slowUpdate.set(slowUpdate);
        final Background globalBackground = new Background(
                new BackgroundFill(Color.rgb(200, 200, 255), CornerRadii.EMPTY, Insets.EMPTY));
        myDialog = new Stage();
        myDialog.initModality(Modality.APPLICATION_MODAL);

        fadeTransition = new FadeTransition(Duration.seconds(1.0), blink);
        fadeTransition.setFromValue(0.8);
        fadeTransition.setToValue(0.4);
        fadeTransition.setCycleCount(Animation.INDEFINITE);
        fadeTransition.stop();

        VBox vBox = new VBox();
        vBox.setSpacing(Utils.adjust(10));
        vBox.setPadding(new Insets(Utils.adjust(10)));
        vBox.setBackground(globalBackground);

        VBox languageParametersVBox = new VBox();

        final Text languageParametersTitle = new Text("Language Model");
        languageParametersTitle.setFont(new Font(Utils.adjust(18)));

        languageParametersVBox.setStyle("-fx-padding: 10;" +
                "-fx-border-style: solid inside;" +
                "-fx-border-width: 2;" +
                "-fx-border-insets: 5;" +
                "-fx-border-radius: 5;" +
                "-fx-border-color: blue;");

        languageParametersVBox.getChildren().addAll(languageParametersTitle, vRegion(1));
        languageParametersVBox.getChildren()
                .addAll(new HBox(new Text("Plaintext language:"), hRegion(1), languageChoiceBox), vRegion(1));
        languageParametersVBox.getChildren()
                .addAll(new HBox(new Text("Combine:   "), hRegion(1), uToV, wToV, jToI, yToI, zToS, kToC), vRegion(1));
        languageParametersVBox.getChildren().addAll(
                new HBox(new Text("Remove:    "), hRegion(1), removeDoubledLetters, removeSpaces, removeX, removeH),
                vRegion(1));
        languageParametersVBox.getChildren()
                .addAll(new HBox(new Text("Ngrams for scoring: "), hRegion(1), ngramChoiceBox, hRegion(2),
                        new Text("Minimum length of plausible decrypted sequence for homophone auto-locking: "),
                        hRegion(1),
                        minMatchingLengthForLockingChoiceBox), vRegion(1));
        languageParametersVBox.getChildren()
                .addAll(new HBox(new Text("Maximum homophones per letter: "), hRegion(1), maxHomophonesChoiceBox,
                        hRegion(1), ignoreCurrentKey, hRegion(1), new Text("Distinct letter types:      "), hRegion(1),
                        plaintextSymbolTypes, hRegion(1),
                        new Text("Maximum number of symbol types that can be assigned: "), hRegion(1),
                        assignableCiphertextSymbolTypes));

        VBox inputParametersVBox = new VBox();

        final Text inputParametersTitle = new Text("Ciphertext Symbols");
        inputParametersTitle.setFont(new Font(Utils.adjust(18)));

        inputParametersVBox.setStyle("-fx-padding: 10;" +
                "-fx-border-style: solid inside;" +
                "-fx-border-width: 2;" +
                "-fx-border-insets: 5;" +
                "-fx-border-radius: 5;" +
                "-fx-border-color: blue;");

        inputParametersVBox.getChildren().addAll(inputParametersTitle, vRegion(1));
        inputParametersVBox.getChildren().addAll(
                new HBox(new Text("Minimum ciphertext symbol type count: "), hRegion(1), minCountChoiceBox),
                vRegion(1));
        inputParametersVBox.getChildren()
                .addAll(new HBox(new Text("Total ciphertext symbols:  "), hRegion(1), ciphertextSymbols), vRegion(1));
        inputParametersVBox.getChildren().addAll(
                new HBox(new Text("Input symbols included: "), hRegion(1), ciphertextSymbolsFiltered), vRegion(1));
        inputParametersVBox.getChildren().addAll(
                new HBox(new Text("Total distinct ciphertext symbol types: "), hRegion(1), ciphertextSymbolTypes),
                vRegion(1));
        inputParametersVBox.getChildren().addAll(new HBox(new Text("Symbol types to be assigned: "), hRegion(1),
                ciphertextSymbolTypesFiltered, hRegion(1)));

        languageChoiceBox.getItems().clear();
        for (Language l : Language.values()) {
            languageChoiceBox.getItems().add(l.toString());
        }
        languageChoiceBox.setOnAction(e -> {
            if (callback)
                updateParametersNewLanguage();
        });

        maxHomophonesChoiceBox.getItems().clear();
        for (String v : new String[] { "1", "2", "3", "4", "5", "6", "7" }) {
            maxHomophonesChoiceBox.getItems().add(v);
        }
        maxHomophonesChoiceBox.setOnAction(e -> {
            if (callback)
                readParameters();
        });

        minMatchingLengthForLockingChoiceBox.getItems().clear();
        for (String v : new String[] { "8", "9", "10", "12", "15", "20", "25", "Disabled" }) {
            minMatchingLengthForLockingChoiceBox.getItems().add(v);
        }
        minMatchingLengthForLockingChoiceBox.setOnAction(e -> {
            if (callback)
                readParameters();
        });

        minCountChoiceBox.getItems().clear();
        for (String v : new String[] { "1", "2", "3", "4", "5", "10", "15", "20", "25", "50", "75", "100", "150", "250",
                "500", "1000" }) {
            minCountChoiceBox.getItems().add(v);
        }
        minCountChoiceBox.setOnAction(e -> {
            if (callback)
                readParameters();
        });

        ngramChoiceBox.getItems().clear();
        for (String v : new String[] { "4", "5", "6" }) {
            ngramChoiceBox.getItems().add(v);
        }
        ngramChoiceBox.setOnAction(e -> {
            if (callback)
                readParameters();
        });

        vBox.getChildren().add(new HBox(languageParametersVBox, inputParametersVBox));

        for (CheckBox cb : new CheckBox[] { uToV, wToV, jToI, yToI, zToS, kToC, removeDoubledLetters, removeSpaces,
                removeX, removeH, ignoreCurrentKey }) {
            cb.setMinWidth(Utils.adjust(125));
            cb.setOnAction(e -> {
                if (callback)
                    readParameters();
            });
        }
        comments.setText(initialResultsText);
        comments.setFont(Font.font(java.awt.Font.MONOSPACED, Utils.adjust(12)));

        Font bigButtons = new Font(Utils.adjust(18));
        for (Button b : new Button[] { startStop, close, save }) {
            b.setFont(bigButtons);
        }
        close.setOnAction(arg0 -> {
            Cryptanalysis.stop();
            myDialog.close();
        });

        startStop.setOnAction(arg0 -> {
            synchronized (Cryptanalysis.keySb) {

                if (Cryptanalysis.started.get()) {
                    Cryptanalysis.stop();
                    progressBar.setProgress(0.0);
                } else {

                    int toAssign = Integer.parseInt(ciphertextSymbolTypesFiltered.getText().split(" ")[0]);
                    int assignable = Integer.parseInt(assignableCiphertextSymbolTypes.getText());
                    if (toAssign > assignable) {
                        comments.setText(String.format(
                                "Too many ciphertext symbol types - %d - but only %d can be assigned as homophones. " +
                                        "Increase 'Maximum homophones per letter' or increase 'Minimum ciphertext symbol count'.",
                                toAssign, assignable));
                        return;
                    }

                    comments.setText("Starting ...");

                    ArrayList<Token> tokens = tokens(params);

                    params.readTokens();
                    params.referenceSequences();

                    params.lockedHomophones.clear();
                    if (Main.key.isKeyAvailable() && !params.ignoreCurrentKey) {
                        Set<String> distinctPlaintext = new TreeSet<>();
                        for (Token t : params.referenceTokens) {
                            if (t.type == Token.Type.HOMOPHONE) {
                                distinctPlaintext.add(t.p);
                            }
                        }
                        for (String c : Main.key.keySet()) {
                            final String p = Main.key.get(c);
                            String pLowerCase = p.toLowerCase(Locale.ROOT);
                            if (params.jToI && pLowerCase.equals("j")) {
                                pLowerCase = "i";
                            }
                            if (params.yToI && pLowerCase.equals("y")) {
                                pLowerCase = "i";
                            }
                            if (params.uToV && pLowerCase.equals("u")) {
                                pLowerCase = "v";
                            }
                            if (params.wToV && pLowerCase.equals("w")) {
                                pLowerCase = "v";
                            }
                            if (params.zToS && pLowerCase.equals("z")) {
                                pLowerCase = "s";
                            }
                            if (params.kToC && pLowerCase.equals("k")) {
                                pLowerCase = "c";
                            }

                            if (Main.key.lockedHomophoneP(c)) {
                                if (distinctPlaintext.contains(pLowerCase)) {
                                    params.lockedHomophones.put(c, pLowerCase);
                                }
                            }

                        }
                    }
                    TreeMap<String, Integer> tokenCounts = tokenCiphertextCounts(tokens);

                    for (Token t : tokens) {
                        if (t.type == Token.Type.HOMOPHONE && tokenCounts.get(t.c) < params.minCount) {
                            t.type = Token.Type.OTHER;
                        }
                    }

                    Cryptanalysis.solve(tokens, params, CryptanalysisWindow.slowUpdate.get() ? 1 : 1000);
                }
            }

        });

        save.setOnAction(arg0 -> {
            synchronized (Cryptanalysis.keySb) {
                if (Cryptanalysis.keySb.length() == 0) {
                    return;
                }
            }
            Cryptanalysis.stop();
            boolean save = false;

            if (Main.key.isKeyAvailable()) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);

                alert.setTitle("Save key from cryptanalysis");
                alert.setContentText("Are you sure? The current key will be lost");

                alert.initOwner(myDialog.getOwner());
                Optional<ButtonType> res = alert.showAndWait();

                if (res.isPresent()) {
                    if (res.get().equals(ButtonType.CANCEL)) {

                    } else if (res.get().equals(ButtonType.OK)) {
                        save = true;
                    } else {
                        throw new RuntimeException("Unrecognized response: " + res);
                    }
                }
            } else {
                save = true;
            }
            if (save) {
                myDialog.close();

                replaceKey(params, Cryptanalysis.keySb);

                Main.fullKeyChanged();
            }
        });

        if (decryptionVBox.getTransforms().isEmpty()) {
            if (showKeyOnTopOfDecryption) {
                decryptionVBox.getTransforms().add(new Scale(0.70, 0.70));
            } else {
                decryptionVBox.getTransforms().add(new Scale(0.5, 0.5));
            }
        }
        decryptionVBox.getChildren().clear();

        ScrollPane decryptionScrollPane = new ScrollPane(decryptionVBox);
        if (!showKeyOnTopOfDecryption) {
            decryptionScrollPane.setMaxWidth(Utils.adjust(953));
            decryptionScrollPane.setMinWidth(Utils.adjust(953));
        } else {
            decryptionScrollPane.setMaxWidth(Utils.adjust(1260));
            decryptionScrollPane.setMinWidth(Utils.adjust(1260));
        }
        decryptionScrollPane.setMaxHeight(Utils.adjust(625));
        decryptionScrollPane.setMinHeight(Utils.adjust(625));
        decryptionScrollPane.setStyle("-fx-padding: 10;" +
                "-fx-border-style: solid inside;" +
                "-fx-border-width: 2;" +
                "-fx-border-insets: 5;" +
                "-fx-border-radius: 5;" +
                "-fx-border-color: green;");

        keyVBox.getChildren().clear();
        ScrollPane keyScrollPane = new ScrollPane(keyVBox);
        if (showKeyOnTopOfDecryption) {
            keyScrollPane.setVisible(false);
        }
        keyScrollPane.setMaxWidth(Utils.adjust(308));
        keyScrollPane.setMinWidth(Utils.adjust(308));
        keyScrollPane.setMaxHeight(Utils.adjust(625));
        keyScrollPane.setMinHeight(Utils.adjust(625));
        keyScrollPane.setStyle("-fx-padding: 10;" +
                "-fx-border-style: solid inside;" +
                "-fx-border-width: 2;" +
                "-fx-border-insets: 5;" +
                "-fx-border-radius: 5;" +
                "-fx-border-color: green;");
        vBox.getChildren().add(new HBox(decryptionScrollPane, hRegion(0.3), keyScrollPane));

        vBox.getChildren().addAll(comments);
        vBox.getChildren().addAll(new HBox(hRegion(0.3), blink, hRegion(1), startStop, hRegion(2), save, hRegion(2),
                close, hRegion(2), progressBar));

        progressBar.setMinWidth(Utils.adjust(730));
        progressBar.setMinHeight(Utils.adjust(40));
        progressBar.setOpacity(0.4);
        progressBar.setBackground(new Background(new BackgroundFill(Color.GREEN, null, null)));
        progressBar.setStyle("-fx-accent: green;");
        progressBar.setProgress(0.0);

        progressBar.setVisible(slowUpdate);

        Scene myDialogScene = new Scene(vBox);
        myDialog.setScene(myDialogScene);
        myDialog.setMinWidth(Utils.adjust(1300));
        myDialog.setMaxWidth(Utils.adjust(1300));
        myDialog.setMinHeight(Utils.adjust(1050));
        myDialog.setMaxHeight(Utils.adjust(1050));
        myDialog.setTitle("Cryptanalysis - Recover Homophones");
        displayParameters();
        callback = true;
        blink.setFill(Color.LIGHTGRAY);

        if (tl == null) {
            int maxSymbols = 5_000;
            tl = new Timeline(
                    new KeyFrame(Duration.millis(0.1 * Math.min(maxSymbols, TranscribedImage.totalSymbols())),
                            event -> {
                                if (Cryptanalysis.started.get()) {
                                    if (startStop.getText().contains("Start")) {
                                        startStop.setText("Stop Cryptanalysis");
                                        blink.setFill(Color.GREEN);
                                        fadeTransition.play();
                                    }

                                    StringBuilder keySb = null;
                                    StringBuilder results = null;

                                    if (CryptanalysisWindow.slowUpdate.get()) {
                                        String res = null;
                                        int step = 1;
                                        final int size = Cryptanalysis.sequence.size();
                                        if (size <= 20) {
                                            step = 1;
                                        } else if (size <= 40) {
                                            step = 2;
                                        } else if (size <= 80) {
                                            step = 3;
                                        } else if (size <= 160) {
                                            step = 4;
                                        } else {
                                            step = 5;
                                        }
                                        for (int i = 0; i < step; i++) {
                                            res = (String) Cryptanalysis.sequence.poll();
                                            iteration++;
                                        }
                                        if (res != null && !res.isEmpty()) {
                                            keySb = new StringBuilder(res);
                                            String status = res.split("\n")[0];
                                            String updateString = status.substring(status.indexOf("Update: ") + 8);
                                            updateString = updateString.substring(0, updateString.length() - 1);
                                            double progress = Double.parseDouble(updateString)
                                                    / Cryptanalysis.updates.get();
                                            status = status.replaceAll("\\]",
                                                    String.format("/%,d]", Cryptanalysis.updates.get()));
                                            status = status.replaceAll(" \\[",
                                                    String.format("/%,d [", Cryptanalysis.bestOverall.get()));
                                            results = new StringBuilder(status);
                                            progressBar.setProgress(progress);
                                        }
                                    } else {
                                        synchronized (Cryptanalysis.keySb) {
                                            if (!Cryptanalysis.readUpdate.get()) {
                                                if (Cryptanalysis.keySb.length() != 0) {
                                                    keySb = new StringBuilder(Cryptanalysis.keySb);
                                                    results = new StringBuilder(
                                                            Cryptanalysis.keySb.toString().split("\n")[0]);
                                                }
                                                Cryptanalysis.readUpdate.set(true);
                                            }
                                        }
                                    }
                                    if (keySb != null) {
                                        Key key = newKeyWithLocks(params, keySb);

                                        keyVBox.getChildren().clear();
                                        ArrayList<String> letters = new ArrayList<>(new TreeSet<>(key.values()));
                                        letters.sort((o1, o2) -> {
                                            if ((o1.length() == 1) != (o2.length() == 1)) {
                                                return o1.length() - o2.length();
                                            }
                                            boolean o1Upper = o1.matches("[A-Z].*");
                                            boolean o2Upper = o2.matches("[A-Z].*");
                                            if (o1Upper == o2Upper) {
                                                return o1.compareTo(o2);
                                            }
                                            return o1.compareToIgnoreCase(o2);

                                        });
                                        final ArrayList<String> colors = Main.colors.sortedColors();
                                        double factor = 0.65;
                                        final double cellSizeAdjusted = factor * Utils.adjust(ICON_SIZE);
                                        final Font pFont = Font.font("Verdana", FontWeight.BOLD,
                                                factor * Utils.adjust(24));

                                        HBox h = new HBox();
                                        String lastP = "123---2222";

                                        if (showKeyOnTopOfDecryption) {
                                            letters.clear();
                                            for (char l : "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray()) {
                                                letters.add("" + l);
                                                letters.add(("" + l).toLowerCase());
                                            }
                                            if (params.jToI) {
                                                letters.remove("J");
                                                letters.remove("j");
                                            }
                                            if (params.yToI) {
                                                letters.remove("Y");
                                                letters.remove("y");
                                            }
                                            if (params.uToV) {
                                                letters.remove("U");
                                                letters.remove("u");
                                            }
                                            if (params.wToV) {
                                                letters.remove("W");
                                                letters.remove("w");
                                            }
                                            if (params.zToS) {
                                                letters.remove("Z");
                                                letters.remove("z");
                                            }
                                            if (params.kToC) {
                                                letters.remove("K");
                                                letters.remove("k");
                                            }
                                            if (params.removeX) {
                                                letters.remove("X");
                                                letters.remove("x");
                                            }
                                            if (params.removeH) {
                                                letters.remove("H");
                                                letters.remove("h");
                                            }
                                        }

                                        Map<String, ArrayList<Image>> decKey = new TreeMap<>();

                                        for (String p : letters) {
                                            if (!p.equalsIgnoreCase(lastP)) {
                                                if (!h.getChildren().isEmpty()) {
                                                    keyVBox.getChildren().add(h);
                                                    h = new HBox();
                                                }
                                            }
                                            if (!showKeyOnTopOfDecryption || !p.equalsIgnoreCase(lastP)) {
                                                Text t = new Text(p);
                                                t.setFont(pFont);
                                                t.setFill(Color.RED);

                                                StackPane textStackPane = new StackPane(t);
                                                textStackPane.setMinWidth(cellSizeAdjusted);
                                                // textStackPane.setMaxWidth(cellSizeAdjusted);
                                                textStackPane.setMinHeight(cellSizeAdjusted);
                                                textStackPane.setMaxHeight(cellSizeAdjusted);
                                                // StackPane.setAlignment(t, Pos.CENTER_LEFT);
                                                h.getChildren().add(textStackPane);
                                            }
                                            lastP = p;
                                            for (String c : key.keySet()) {
                                                String d = key.fromTranscription(c);

                                                if (d != null && d.equals(p)) {
                                                    for (String colorString : colors) {
                                                        if (Main.colors.get(colorString).equals(c)) {
                                                            Image iconImage = Icons.get(colorString);

                                                            if (iconImage != null) {
                                                                ArrayList<Image> list = decKey.getOrDefault(
                                                                        p.toUpperCase(), new ArrayList<>());
                                                                list.add(iconImage);
                                                                decKey.put(p.toUpperCase(), list);

                                                                ImageView icon = new ImageView(iconImage);
                                                                icon.setFitHeight(cellSizeAdjusted);
                                                                icon.setFitWidth(cellSizeAdjusted);
                                                                icon.setPreserveRatio(true);

                                                                StackPane iconStackPane = new StackPane(icon);
                                                                iconStackPane.setMinWidth(cellSizeAdjusted);
                                                                iconStackPane.setMaxWidth(cellSizeAdjusted);
                                                                iconStackPane.setMinHeight(cellSizeAdjusted);
                                                                iconStackPane.setMaxHeight(cellSizeAdjusted);
                                                                h.getChildren().add(iconStackPane);
                                                            }
                                                        }
                                                    }
                                                }
                                            }

                                        }
                                        if (!h.getChildren().isEmpty()) {
                                            keyVBox.getChildren().add(h);
                                        }

                                        decryptionVBox.getChildren().clear();

                                        if (showKeyOnTopOfDecryption) {
                                            HBox hl = new HBox();
                                            final double cellSizeAdjusted2 = Utils.adjust(52);

                                            for (String p : letters) {
                                                if (!p.toUpperCase().equals(p)) {
                                                    continue;
                                                }

                                                Text t = new Text(p);
                                                final Font font = Font.font("Verdana", FontWeight.BOLD,
                                                        Utils.adjust(42));

                                                t.setFont(font);
                                                t.setFill(Color.BLUE);

                                                StackPane textStackPane = new StackPane(t);
                                                textStackPane.setMinWidth(cellSizeAdjusted2);
                                                textStackPane.setMaxWidth(cellSizeAdjusted2);
                                                textStackPane.setMinHeight(cellSizeAdjusted2);
                                                textStackPane.setMaxHeight(cellSizeAdjusted2);
                                                StackPane.setAlignment(t, Pos.CENTER);

                                                hl.getChildren().add(textStackPane);
                                            }
                                            decryptionVBox.getChildren().add(hl);

                                            for (int i = 0; i < params.maxHomophones; i++) {
                                                HBox ih = new HBox();
                                                for (String p : letters) {
                                                    if (!p.toUpperCase().equals(p)) {
                                                        continue;
                                                    }
                                                    ArrayList<Image> list = decKey.get(p);
                                                    ImageView icon = new ImageView();
                                                    icon.setFitHeight(Utils.adjust(ICON_SIZE));
                                                    icon.setFitWidth(Utils.adjust(ICON_SIZE));
                                                    icon.setPreserveRatio(true);

                                                    StackPane iconStackPane = new StackPane(icon);
                                                    iconStackPane.setMinWidth(cellSizeAdjusted2);
                                                    iconStackPane.setMaxWidth(cellSizeAdjusted2);
                                                    iconStackPane.setMinHeight(cellSizeAdjusted2);
                                                    iconStackPane.setMaxHeight(cellSizeAdjusted2);
                                                    StackPane.setAlignment(icon, Pos.CENTER);

                                                    if (list != null && list.size() > i) {
                                                        icon.setImage(list.get(i));
                                                    }
                                                    ih.getChildren().add(iconStackPane);
                                                }
                                                decryptionVBox.getChildren().add(ih);
                                            }
                                            Rectangle r = new Rectangle(decryptionVBox.getWidth(), Utils.adjust(3));
                                            r.setFill(Color.BLUE);
                                            decryptionVBox.getChildren().add(r);
                                            Region reg = new Region();
                                            reg.setMinHeight(10);
                                            decryptionVBox.getChildren().add(reg);
                                        }

                                        int total = 0;
                                        for (int idx = 0; idx < TranscribedImage.size(); idx++) {

                                            final int symbolsInDocument = TranscribedImage.image(idx).positions()
                                                    .size();
                                            if (symbolsInDocument == 0) {
                                                continue;
                                            }

                                            if (!showKeyOnTopOfDecryption) {
                                                final Text documentName = new Text(FileUtils.currentDirectoryString()
                                                        + " - " + TranscribedImage.image(idx).filename);
                                                final Font font = Font.font("Verdana", FontWeight.BOLD,
                                                        Utils.adjust(36));
                                                documentName.setFont(font);
                                                decryptionVBox.getChildren().add(documentName);
                                                decryptionVBox.getChildren().add(vRegion(1));
                                            }
                                            ArrayList<ArrayList<Rectangle>> lines = Alignment.linesOfSymbols(idx);
                                            if (showKeyOnTopOfDecryption) {
                                                lines = new ArrayList<>();
                                                ArrayList<Rectangle> line = new ArrayList<>();
                                                for (ArrayList<Rectangle> c : Alignment.linesOfSymbols(idx)) {
                                                    for (Rectangle r : c) {
                                                        line.add(r);
                                                        if (line.size() >= 35) {
                                                            lines.add(line);
                                                            line = new ArrayList<>();
                                                            if (lines.size() == 8) {
                                                                break;
                                                            }
                                                        }
                                                    }
                                                    if (lines.size() == 8) {
                                                        line = new ArrayList<>();
                                                        break;
                                                    }
                                                }
                                                if (!line.isEmpty()) {
                                                    lines.add(line);
                                                }
                                            }
                                            for (ArrayList<Rectangle> c : lines) {
                                                ArrayList<String> d = DetailedTranscriptionPane.decryptionSequence(key,
                                                        c);
                                                decryptionVBox.getChildren().add(symbolDisplayLine(key, c, d));
                                            }
                                            decryptionVBox.getChildren().add(vRegion(2));
                                            total += symbolsInDocument;
                                            if (total > maxSymbols || showKeyOnTopOfDecryption) {
                                                break;
                                            }
                                        }

                                        comments.setText(results.toString());

                                        if (showKeyOnTopOfDecryption) {
                                            // FileUtils.snapshot("hillclimbing", "d" + iteration, decryptionVBox);
                                        }

                                    }
                                } else {
                                    if (startStop.getText().contains("Stop")) {
                                        startStop.setText("Start Cryptanalysis");
                                        blink.setFill(Color.LIGHTGRAY);
                                        fadeTransition.stop();
                                    }
                                }
                            }

                    ));
            tl.setCycleCount(Timeline.INDEFINITE);
            tl.play();
        }

        myDialog.getScene().getWindow().addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, we -> {
            synchronized (Cryptanalysis.keySb) {
                Cryptanalysis.stop();
            }
        });

        myDialog.show();
    }

    private static HBox symbolDisplayLine(Key key, List<Rectangle> lineOfSymbols, List<String> decryptionSequence) {
        HBox line = new HBox();
        line.setSpacing(0);
        for (int i = 0; i < lineOfSymbols.size(); i++) {
            Rectangle r = lineOfSymbols.get(i);

            SimulatedImage.SymbolStackPane sp = new SimulatedImage.SymbolStackPane(true, key);
            sp.update(key, decryptionSequence, i, r, false);
            line.getChildren().add(sp);
        }
        return line;
    }

    public static void replaceKey(CryptanalysisParameters parameters, StringBuilder keySb) {
        Key newKey = newKeyWithLocks(parameters, keySb);
        Main.key.replace(newKey, "#ORIGIN: Cryptanalysis by OTA");

    }

    public static Key newKeyWithLocks(CryptanalysisParameters parameters, StringBuilder keyString) {
        Key newKey = new Key();

        newKey.parse("From cryptanalysis", keyString);
        if (!parameters.ignoreCurrentKey) {
            for (String c : Main.key.keySet()) {

                if (Key.lockedC(c)) {
                    continue;
                }

                if (Main.key.lockedP(c)) {
                    newKey.put(c, Main.key.fromTranscription(c));
                }
            }
        }

        Set<String> toLock = lockedBasedOnReferenceSequences(params);

        for (String c : toLock) {
            newKey.put(c, Main.key.get(c).toUpperCase());
        }
        return newKey;
    }

    public static Set<String> lockedBasedOnReferenceSequences(CryptanalysisParameters params) {

        ArrayList<Token> ciphertext = tokens(params);
        Set<String> toLock = new TreeSet<>();
        for (int i = 0; i < ciphertext.size() - params.referenceSequenceLengthForLocking; i++) {
            String s = "";
            for (int z = 0; z < params.referenceSequenceLengthForLocking; z++) {
                String c = ciphertext.get(i + z).c;
                final String pp = Main.key.get(c);
                if (pp != null && !pp.isEmpty()) {
                    s += pp;
                } else {
                    s += "!";
                }
                if (params.referenceSequences.contains(s)) {
                    for (int z2 = 0; z2 < params.referenceSequenceLengthForLocking; z2++) {
                        String c2 = ciphertext.get(i + z2).c;
                        toLock.add(c2);
                    }
                }
            }
        }
        return toLock;
    }

    static TreeMap<String, Integer> tokenCiphertextCounts(ArrayList<Token> tokens) {
        TreeMap<String, Integer> counts = new TreeMap<>();
        for (Token token : tokens) {
            if (token.type == Token.Type.HOMOPHONE) {
                counts.put(token.c, counts.getOrDefault(token.c, 0) + 1);
            }
        }

        return counts;
    }

    private static void changesWillTakeEffect() {
        if (Cryptanalysis.started.get()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);

            alert.setTitle("Parameters changed");
            alert.setContentText(
                    "Cryptanalysis currently in process. The new parameters\nwill take effect next time you start cryptanalysis");

            alert.initOwner(myDialog.getOwner());
            Optional<ButtonType> res = alert.showAndWait();

            if (res.isPresent()) {

            }
        }
    }

    private static void displayParameters() {

        languageChoiceBox.setValue(params.language.toString());

        uToV.setSelected(params.uToV);
        wToV.setSelected(params.wToV);
        jToI.setSelected(params.jToI);
        yToI.setSelected(params.yToI);
        kToC.setSelected(params.kToC);
        zToS.setSelected(params.zToS);
        ignoreCurrentKey.setSelected(params.ignoreCurrentKey);
        removeX.setSelected(params.removeX);
        removeH.setSelected(params.removeH);
        removeDoubledLetters.setSelected(params.removeDoubles);
        removeSpaces.setSelected(params.removeSpaces);

        minCountChoiceBox.setValue("" + params.minCount);
        maxHomophonesChoiceBox.setValue("" + params.maxHomophones);
        if (params.referenceSequenceLengthForLocking >= 1000) {
            minMatchingLengthForLockingChoiceBox.setValue("Disabled");
        } else {
            minMatchingLengthForLockingChoiceBox.setValue("" + params.referenceSequenceLengthForLocking);
        }

        ngramChoiceBox.setValue("" + params.ngrams);
        updateCounts();

    }

    private static void readParameters() {
        params.uToV = uToV.isSelected();
        params.wToV = wToV.isSelected();
        params.jToI = jToI.isSelected();
        params.yToI = yToI.isSelected();
        params.kToC = kToC.isSelected();
        params.zToS = zToS.isSelected();
        params.ignoreCurrentKey = ignoreCurrentKey.isSelected();
        params.removeX = removeX.isSelected();
        params.removeH = removeH.isSelected();
        params.removeDoubles = removeDoubledLetters.isSelected();
        params.removeSpaces = removeSpaces.isSelected();
        try {
            params.minCount = Integer.parseInt(minCountChoiceBox.getValue());
            params.maxHomophones = Integer.parseInt(maxHomophonesChoiceBox.getValue());
            params.ngrams = Integer.parseInt(ngramChoiceBox.getValue());
            if (minMatchingLengthForLockingChoiceBox.getValue() != null) {
                if (minMatchingLengthForLockingChoiceBox.getValue().equals("Disabled")) {
                    params.referenceSequenceLengthForLocking = 1000;
                } else {
                    params.referenceSequenceLengthForLocking = Integer
                            .parseInt(minMatchingLengthForLockingChoiceBox.getValue());
                }
            }
        } catch (NumberFormatException ignored) {
        }
        updateCounts();
    }

    private static void updateParametersNewLanguage() {
        final String value = languageChoiceBox.getValue();
        if (value == null) {
            return;
        }
        Language l = Language.valueOf(value);

        if (!l.equals(params.language)) {
            params.updateLanguage(l);
        }

        displayParameters();

    }

    private static void updateCounts() {

        params.readTokens();

        Set<String> distinctPlaintext = new TreeSet<>();
        for (Token t : params.referenceTokens) {
            if (t.type == Token.Type.HOMOPHONE) {
                distinctPlaintext.add(t.p);
            }
        }
        final int plaintextDistinct = distinctPlaintext.size();

        final int assignable = Cryptanalysis.assignable(params, plaintextDistinct);

        ArrayList<Token> tokens = tokens(params);

        Map<String, Integer> counts = tokenCiphertextCounts(tokens);

        int filtered = 0;
        int totalTokens = 0;
        for (Token t : tokens) {
            if (t.type == Token.Type.HOMOPHONE) {
                totalTokens++;
                if (counts.get(t.c) >= params.minCount) {
                    filtered++;
                }
            }
        }

        int filteredDistinctTypes = 0;
        for (Integer count : counts.values()) {
            if (count >= params.minCount) {
                filteredDistinctTypes++;
            }
        }

        int locked = 0;
        if (Main.key.isKeyAvailable() && !params.ignoreCurrentKey) {
            params.lockedHomophones.clear();
            for (String c : Main.key.keySet()) {
                String p = Main.key.get(c);
                String pLowerCase = p.toLowerCase(Locale.ROOT);

                if (params.jToI && pLowerCase.equals("j")) {
                    pLowerCase = "i";
                }
                if (params.yToI && pLowerCase.equals("y")) {
                    pLowerCase = "i";
                }
                if (params.uToV && pLowerCase.equals("u")) {
                    pLowerCase = "v";
                }
                if (params.wToV && pLowerCase.equals("w")) {
                    pLowerCase = "v";
                }
                if (params.zToS && pLowerCase.equals("z")) {
                    pLowerCase = "s";
                }
                if (params.kToC && pLowerCase.equals("k")) {
                    pLowerCase = "c";
                }

                if (Main.key.lockedHomophoneP(c)) {
                    if (distinctPlaintext.contains(pLowerCase)) {
                        params.lockedHomophones.put(c, pLowerCase);
                    }
                    locked++;
                }

            }
        }

        ciphertextSymbolTypes.setText("" + counts.size());
        ciphertextSymbolTypesFiltered.setText("" + filteredDistinctTypes + " (" + locked + " Locked)");

        plaintextSymbolTypes.setText("" + plaintextDistinct);
        assignableCiphertextSymbolTypes.setText("" + assignable);

        ciphertextSymbols.setText("" + totalTokens);
        ciphertextSymbolsFiltered.setText(String.format("%4.1f%%", 100.0 * filtered / totalTokens));

        if (filteredDistinctTypes > assignable) {
            ciphertextSymbolTypesFiltered.setFill(Color.RED);
            assignableCiphertextSymbolTypes.setFill(Color.RED);
        } else {
            ciphertextSymbolTypesFiltered.setFill(Color.GREEN);
            assignableCiphertextSymbolTypes.setFill(Color.GREEN);
        }
        changesWillTakeEffect();
    }

    static ArrayList<Token> tokens(CryptanalysisParameters params) {
        ArrayList<Token> tokens = new ArrayList<>();
        for (int index = 0; index < TranscribedImage.transcribedImages.length; index++) {
            ArrayList<ArrayList<javafx.scene.shape.Rectangle>> linesOfSymbols = Alignment.linesOfSymbols(index);
            for (ArrayList<Rectangle> lineOfSymbols : linesOfSymbols) {
                for (Rectangle symbol : lineOfSymbols) {
                    final String colorString = symbol.getFill().toString();
                    if (Main.colors.contains(colorString)) {
                        final String c = Main.colors.get(colorString);

                        if (Key.lockedC(c)) {
                            tokens.add(new Token(Token.Type.OTHER, c));
                        } else if (Main.key.lockedOtherP(c) && !params.ignoreCurrentKey) {
                            tokens.add(new Token(Token.Type.OTHER, c, Main.key.fromTranscription(c)));
                        } else if (Main.key.lockedHomophoneP(c) && !params.ignoreCurrentKey) {
                            tokens.add(new Token(Token.Type.HOMOPHONE, c, Main.key.fromTranscription(c)));
                        } else {
                            tokens.add(new Token(Token.Type.HOMOPHONE, c));
                        }

                    } else {
                        tokens.add(new Token(Token.Type.OTHER));
                    }
                }
                tokens.add(new Token(Token.Type.NEW_LINE));
            }
            if (tokens.size() > 20_000) {
                break;
            }
        }
        return tokens;
    }

    static Region vRegion(double multiplier) {
        Region region = new Region();
        region.setMinWidth(1);
        region.setMinHeight(Utils.adjust(15 * multiplier));
        return region;
    }

    static Region hRegion(double multiplier) {
        Region region = new Region();
        region.setMinHeight(1);
        region.setMinWidth(Utils.adjust(20 * multiplier));
        return region;
    }

}