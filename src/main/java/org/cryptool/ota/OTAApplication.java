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

import java.io.File;
import java.util.ArrayList;
import java.util.Optional;

import org.cryptool.ota.cryptanalysis.CryptanalysisWindow;
import org.cryptool.ota.cryptanalysis.Key;
import org.cryptool.ota.gui.ClusterListView;
import org.cryptool.ota.gui.DetailedTranscriptionPane;
import org.cryptool.ota.gui.DetailedTranscriptionSnapshot;
import org.cryptool.ota.gui.FullKeyWindow;
import org.cryptool.ota.gui.Headers;
import org.cryptool.ota.gui.KeySnapshot;
import org.cryptool.ota.gui.MainImagePane;
import org.cryptool.ota.gui.SelectionArea;
import org.cryptool.ota.gui.SimulatedImage;
import org.cryptool.ota.gui.SimulatedImagePartialDecryption;
import org.cryptool.ota.gui.SymbolsSnapshot;
import org.cryptool.ota.util.Colors;
import org.cryptool.ota.util.EditedRecord;
import org.cryptool.ota.util.FileUtils;
import org.cryptool.ota.util.ImageUtils;
import org.cryptool.ota.util.Selection;
import org.cryptool.ota.util.SelectionBox;
import org.cryptool.ota.util.TranscribedImage;
import org.cryptool.ota.util.Utils;

import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.DirectoryChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class OTAApplication extends Application {

    public enum Mode {
        IMAGE, 
        CLUSTER
    }

    public static final String COLORS_FILE = "colors";
    public static Colors colors;
    public static SelectionArea selectionArea;

    static ClusterListView listView;
    public static FullKeyWindow fullKeyWindow;
    public static Stage myStage;

    public static Mode mode = Mode.IMAGE;
    public static boolean detailed = false;

    static String[] args;

    public static Key key = new Key();

    public static void main(String[] args) {

        OTAApplication.args = args;
        launch(args);
    }

    public void start(final Stage stage) {

        if (args.length == 0) {

            while (true) {
                ArrayList<String> imageFilesInCurrentDirectory = FileUtils.imageFilesInCurrentDirectory();
                if (imageFilesInCurrentDirectory.isEmpty()) {
                    System.out.println("No image files specified or in current directory");

                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);

                    alert.setTitle("No image files in directory " + FileUtils.workingDirectory);
                    alert.setContentText("Specify another directory?");

                    alert.initOwner(stage.getOwner());
                    Optional<ButtonType> res = alert.showAndWait();

                    if (res.isPresent()) {
                        if (res.get().equals(ButtonType.CANCEL)) {
                            System.exit(0);
                        } else if (res.get().equals(ButtonType.OK)) {
                            DirectoryChooser directoryChooser = new DirectoryChooser();
                            directoryChooser.setInitialDirectory(new File("."));
                            File selectedDirectory = directoryChooser.showDialog(stage);
                            System.out.println(selectedDirectory.getAbsolutePath());
                            FileUtils.workingDirectory = selectedDirectory.getAbsolutePath();
                        } else {
                            throw new RuntimeException("Unrecognized response: " + res);
                        }
                    }

                } else {
                    break;
                }
            }

            ArrayList<String> imageFilesInCurrentDirectory = FileUtils.imageFilesInCurrentDirectory();
            String keyFileInCurrentDirectory = FileUtils.keyFileInCurrentDirectory();
            args = new String[imageFilesInCurrentDirectory.size() + (keyFileInCurrentDirectory == null ? 0 : 2)];
            for (int i = 0; i < imageFilesInCurrentDirectory.size(); i++) {
                args[i] = imageFilesInCurrentDirectory.get(i);
            }
            if (keyFileInCurrentDirectory != null) {
                args[imageFilesInCurrentDirectory.size()] = "-k";
                args[imageFilesInCurrentDirectory.size() + 1] = keyFileInCurrentDirectory;
            }

        }

        boolean minusK = false;
        for (String arg : args) {

            if (arg.equalsIgnoreCase("-k")) {
                minusK = true;
                continue;
            }

            if (minusK && arg.endsWith("txt")) {
                if (key != null && key.isKeyAvailable()) {
                    System.out.println("Too many key files");
                    System.exit(1);
                }
                key = Key.readFromFile(arg);
            } else if (!ImageUtils.isSupportedFormat(arg)) {
                System.out.println("Unsupported image type: " + arg);
                System.exit(1);
            }
            minusK = false;

        }

        Utils.screenWidth = (int) Screen.getPrimary().getBounds().getWidth();
        Utils.screenHeight = (int) Screen.getPrimary().getBounds().getHeight();
        System.out.printf("Screen size: %d x %d\n", Utils.screenWidth, Utils.screenHeight);

        colors = Colors.restore(COLORS_FILE);
        if (colors == null) {
            colors = new Colors();
        }
        TranscribedImage.transcribedImages = TranscribedImage.extractFromArgs(args);

        EditedRecord.restore();

        // Top Header
        HBox titleHbox = Headers.initTitle();

        // Top area
        MainImagePane.initMainImagePane();
        listView = new ClusterListView();
        listView.reset();
        listView.setOnKeyPressed(OTAApplication::keyPressed);
        fullKeyWindow = new FullKeyWindow();
        fullKeyWindow.hide();

        HBox hBox = new HBox(fullKeyWindow, listView, MainImagePane.scrollPane);
        HBox.setHgrow(fullKeyWindow, Priority.ALWAYS);

        // Bottom Area
        selectionArea = new SelectionArea();

        // Bottom Header
        HBox legendHBox = Headers.initLegend();

        VBox root = new VBox(titleHbox, hBox, selectionArea, legendHBox);
        VBox.setVgrow(hBox, Priority.ALWAYS);
        VBox.setVgrow(selectionArea, Priority.ALWAYS);
        VBox.setVgrow(legendHBox, Priority.NEVER);
        root.setOnKeyPressed(OTAApplication::keyPressed);

        Rectangle2D screenBounds = Screen.getPrimary().getBounds();
        Scene scene = new Scene(root, 0.95 * screenBounds.getWidth(), 0.92 * screenBounds.getHeight());
        scene.setOnKeyPressed(OTAApplication::keyPressed);

        myStage = stage;
        myStage.setResizable(false);
        Headers.updateTopTitle();
        stage.setScene(scene);
        stage.setResizable(true);
        stage.getScene().getWindow().addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, event -> {

            if (colors.changed() || key.changed() || TranscribedImage.changed() || EditedRecord.changed) {

                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);

                alert.setTitle("Quit");
                alert.setContentText("Close without saving?");

                alert.initOwner(myStage.getOwner());
                Optional<ButtonType> res = alert.showAndWait();

                if (res.isPresent()) {
                    if (res.get().equals(ButtonType.CANCEL)) {
                        event.consume();
                    } else if (res.get().equals(ButtonType.OK)) {
                        System.exit(0);
                    } else {
                        throw new RuntimeException("Unrecognized response: " + res);
                    }
                }
            } else {
                System.exit(0);
            }
        });
        stage.show();

        showImage(0, TranscribedImage.first(0), true);

    }

    public static void unselectRectangle() {

        String previousId = Selection.clear();
        if (previousId != null && mode == Mode.IMAGE && detailed) {
            DetailedTranscriptionPane.updateSymbol(TranscribedImage.idToIndex(previousId), previousId);
        }
        SelectionBox.hide();

        selectionArea.unselectRectangle();
    }

    public static void unselect() {
        if (selectionArea != null) {
            selectionArea.unselectColor();
            unselectRectangle();
        }
        if (mode == Mode.IMAGE && detailed) {
            DetailedTranscriptionPane.updateBorders(TranscribedImage.currentImageIndex);
        }
    }

    public static void fullKeyChanged() {
        listView.refresh();
        if (mode == Mode.IMAGE && detailed) {
            DetailedTranscriptionPane.show();
        } else if (mode == Mode.CLUSTER && detailed) {
            FullKeyWindow.scrollPane.refresh();
        }
        selectionArea.refresh();
    }

    public static void symbolSelectedFromImagePane(Rectangle node) {
        selectRectangle(TranscribedImage.currentImageIndex, node, true);
        listView.updateListView(false);
        Headers.updateHeadersAndBottom();
    }

    public static void symbolChangedColor(Rectangle draggedR, Color draggedColor) {
        if (mode == Mode.CLUSTER) {
            unselectRectangle();
            selectColor(draggedColor);
        } else {
            if (mode == Mode.IMAGE && detailed) {
                DetailedTranscriptionPane.updateSymbol(TranscribedImage.currentImageIndex,
                        TranscribedImage.rectangleToId(TranscribedImage.currentImageIndex, draggedR));
            }
            selectRectangle(TranscribedImage.currentImageIndex, draggedR, true);
        }
        listView.updateListView(true);
        FullKeyWindow.scrollPane.refresh();
    }

    public static void colorSelected(String colorString) {
        Color color = Color.valueOf(colorString);
        if (color != null) {
            unselectRectangle();
            selectColor(color);
        } else {
            unselect();
        }
    }

    public static void colorParametersChanged(Color selectedColor, boolean transcriptionValueChanged, boolean iconChanged,
            boolean decryptionValueChanged) {
        fullKeyWindow.refresh();
        if (transcriptionValueChanged) {
            listView.reset();
        } else {
            listView.updateListView(true);
        }
        if (mode == Mode.IMAGE && detailed) {
            DetailedTranscriptionPane.refreshColor(selectedColor, decryptionValueChanged);
        }
        if (iconChanged) {
            selectionArea.colorIconChanged();
        }
    }

    public static void symbolClickedFromSelectionArea(String clickedId, int idx, Rectangle nr) {

        saveZoomAndScrollState();

        if (mode == Mode.IMAGE) {
            if (TranscribedImage.currentImageIndex != idx) {
                showImage(idx, nr, false);
            } else {
                selectRectangle(idx, nr, false);
            }
            selectionArea.refreshGrid();
        } else {
            Selection.toggleSelection(clickedId);
        }
    }

    public static void symbolResizedFromImagePane(Rectangle r) {
        selectRectangle(TranscribedImage.currentImageIndex, r, true);
    }

    public static void keyPressed(javafx.scene.input.KeyEvent event) {

        saveZoomAndScrollState();

        Rectangle selected = Selection.singleSelectedIdToRectangle();

        switch (event.getCode()) {
            case ESCAPE:
                if (colors.changed() || key.changed() || TranscribedImage.changed() || EditedRecord.changed) {

                    saveAll(false);

                    Alert alert = new Alert(Alert.AlertType.INFORMATION);

                    alert.setTitle("Save and quit");
                    alert.setContentText("All changes were saved");

                    alert.initOwner(myStage.getOwner());
                    alert.showAndWait();
                }
                System.exit(0);
                break;
            case F1:
                if (mode != Mode.CLUSTER) {
                    showImage((TranscribedImage.currentImageIndex + 1) % TranscribedImage.size(), null, false);
                } else {
                    colors.defaultSorting();
                    listView.reset();
                    fullKeyWindow.refresh();
                }

                break;
            case F2:
                if (mode != Mode.CLUSTER) {
                    showImage((TranscribedImage.currentImageIndex - 1 + TranscribedImage.size())
                            % TranscribedImage.size(), null, false);
                } else {
                    colors.sortByDecryption();
                    listView.reset();
                    fullKeyWindow.refresh();
                }
                break;
            case F3:
                if (mode != Mode.CLUSTER && selected != null && !detailed
                        && MainImagePane.subMode == MainImagePane.SubMode.SYMBOLS) {
                    Rectangle previous = TranscribedImage.previousPosition(TranscribedImage.currentImageIndex,
                            selected);
                    if (previous != null) {
                        selectRectangle(TranscribedImage.currentImageIndex, previous, true);
                    }
                }
                if (mode == Mode.CLUSTER) {
                    colors.sortByFrequency();
                    listView.reset();
                    fullKeyWindow.refresh();
                }
                if (mode == Mode.IMAGE && !detailed && MainImagePane.subMode == MainImagePane.SubMode.DECRYPTION) {
                    MainImagePane.decryptionFontSizeFactor /= 1.05;
                    MainImagePane.showDecryption();
                }
                break;

            case F4:
                if (mode != Mode.CLUSTER && selected != null && !detailed
                        && MainImagePane.subMode == MainImagePane.SubMode.SYMBOLS) {
                    Rectangle next = TranscribedImage.nextPosition(TranscribedImage.currentImageIndex, selected);
                    if (next != null) {
                        selectRectangle(TranscribedImage.currentImageIndex, next, true);
                    }
                }
                if (mode == Mode.IMAGE && !detailed && MainImagePane.subMode == MainImagePane.SubMode.DECRYPTION) {
                    MainImagePane.decryptionFontSizeFactor *= 1.05;
                    MainImagePane.showDecryption();
                }

                break;

            case F6:
                MainImagePane.zoomOut();
                break;
            case F5:
                MainImagePane.zoomIn();
                break;
            case F7:

                if (mode == Mode.IMAGE) {
                    if (detailed) {
                        DetailedTranscriptionSnapshot.detailedTranscriptionSnapshot(TranscribedImage.currentImageIndex);
                    } else {
                        MainImagePane.symbolsInImageSnapshot();
                    }
                    if (event.isControlDown()) {
                        if (key.isKeyAvailable()) {
                            SimulatedImage.simulatedImageSnapshot(TranscribedImage.currentImageIndex, true, true,
                                    false);
                            SimulatedImage.simulatedImageSnapshot(TranscribedImage.currentImageIndex, false, true,
                                    false);
                            SimulatedImage.simulatedImageSnapshot(TranscribedImage.currentImageIndex, true, true, true);
                            SimulatedImage.simulatedImageSnapshot(TranscribedImage.currentImageIndex, false, true,
                                    true);
                            SimulatedImagePartialDecryption.simulatedImageSnapshot(TranscribedImage.currentImageIndex);
                        }
                        SimulatedImage.simulatedImageSnapshot(TranscribedImage.currentImageIndex, true, false, false);
                        SimulatedImage.simulatedImageSnapshot(TranscribedImage.currentImageIndex, false, false, false);
                    }
                } else {
                    SymbolsSnapshot.keySnapshot();
                    KeySnapshot.keySnapshot();
                }

                break;

            case F8:
                CryptanalysisWindow.show(event.isControlDown());
                break;
            case F9:
                if (mode == Mode.IMAGE && !detailed) {
                    MainImagePane.subMode = MainImagePane.nextSubMode();
                    if (MainImagePane.subMode == MainImagePane.SubMode.SYMBOLS) {
                        showImage(TranscribedImage.currentImageIndex, null, false);
                    } else if (MainImagePane.subMode == MainImagePane.SubMode.LINES) {
                        MainImagePane.showLines();
                    } else {
                        MainImagePane.showDecryption();
                    }
                }
                break;
            case F10:
            case S:
                if (event.getCode() == KeyCode.S && !event.isControlDown()) {
                    break;
                }

                saveAll(false);

                Alert alert = new Alert(Alert.AlertType.INFORMATION);

                alert.setTitle("Save");
                alert.setContentText("All changes were saved");

                alert.initOwner(myStage.getOwner());
                alert.showAndWait();

                break;
            case F11:
            case TAB:
                switchMode(mode == Mode.CLUSTER ? Mode.IMAGE : Mode.CLUSTER, false);

                break;
            case F12:

                if (mode == Mode.IMAGE && !detailed) {
                    DetailedTranscriptionPane.setDetailed(event.isControlDown());
                }

                switchMode(mode, !detailed);

                break;
            case DELETE:
                if (selected != null && mode != Mode.CLUSTER) {
                    Rectangle next = TranscribedImage.nextPosition(TranscribedImage.currentImageIndex, selected);
                    if (next != null) {
                        selectRectangle(TranscribedImage.currentImageIndex, next, true);
                    } else {
                        unselect();
                    }
                    MainImagePane.mainPane.getChildren().remove(selected);
                    TranscribedImage.current().remove(selected);
                }
                break;

            default:

                if (mode != Mode.CLUSTER && event.getCode() == KeyCode.ADD && event.isControlDown()) {
                    MainImagePane.zoomIn();
                } else if (mode != Mode.CLUSTER && event.getCode() == KeyCode.SUBTRACT && event.isControlDown()) {
                    MainImagePane.zoomOut();
                }

                break;
        }
        Headers.updateHeadersAndBottom();
        listView.updateListView(false);

        event.consume();

    }

    private static void switchMode(Mode newMode, boolean newDetailed) {
        if (newMode == mode && detailed == newDetailed) {
            return;
        }

        saveZoomAndScrollState();

        if (newMode != mode) {
            newDetailed = false;
        }

        detailed = newDetailed;
        mode = newMode;

        if (mode == Mode.CLUSTER) {
            MainImagePane.scrollPane.setVisible(false);
            MainImagePane.scrollPane.setMaxWidth(0);
            if (detailed) {
                listView.hide();
                fullKeyWindow.show();
            } else {
                listView.show(true);
                fullKeyWindow.hide();
            }
        } else if (mode == Mode.IMAGE) {
            MainImagePane.scrollPane.setMaxWidth(10000);
            MainImagePane.scrollPane.setVisible(true);
            fullKeyWindow.hide();
            listView.show(true); // Needed before with true and after with false
            final String id = Selection.clear();
            if (id != null) {
                final int idx = TranscribedImage.idToIndex(id);
                final Rectangle r = TranscribedImage.idToRectangle(id);
                if (idx != -1 && r != null) {
                    showImage(idx, r, true);
                } else {
                    System.out.printf("CLUSTER to IMAGE - undefined ID: %s\n", id);
                }
            } else {
                showImage(TranscribedImage.currentImageIndex, null, false);
            }
            listView.show(false);
        }

        Headers.updateHeadersAndBottom();
    }

    private static void saveZoomAndScrollState() {
        if (mode == Mode.IMAGE) {
            if (detailed) {
                DetailedTranscriptionPane.saveZoomAndScrollState();
            } else {
                MainImagePane.saveZoomAndScrollState();
            }
        }
    }

    private static void showImage(int newIndex, Rectangle newSelection, boolean updateColor) {

        TranscribedImage.currentImageIndex = newIndex;

        if (OTAApplication.detailed) {
            DetailedTranscriptionPane.show();
        } else {
            MainImagePane.showImage();
        }
        Headers.updateHeadersAndBottom();

        if (newSelection != null) {
            selectRectangle(TranscribedImage.currentImageIndex, newSelection, updateColor);
        } else {
            unselect();
        }

    }

    private static void selectRectangle(int idx, Rectangle newSelected, boolean selectColor) {

        String previousId = Selection.clear();
        if (previousId != null && mode == Mode.IMAGE && detailed) {
            DetailedTranscriptionPane.updateSymbol(TranscribedImage.idToIndex(previousId), previousId);
        }
        if (newSelected == null || idx == -1) {
            return;
        }
        final String newId = TranscribedImage.rectangleToId(idx, newSelected);
        Selection.add(newId);

        if (selectColor) {
            selectColor((Color) newSelected.getFill());
        }
        selectionArea.selectRectangle(idx, newSelected);

        if (mode == Mode.IMAGE) {
            if (detailed) {
                DetailedTranscriptionPane.updateSymbol(TranscribedImage.idToIndex(newId), newId);
            } else {
                SelectionBox.show(newSelected);
            }
            MainImagePane.scrollTo(MainImagePane.scrollPane, newSelected);
        }

    }

    private static void selectColor(Color color) {

        selectionArea.selectColor(color, false);
        MainImagePane.baseRectangle.setFill(color);

        MainImagePane.mainPane.requestFocus();
        if (mode == Mode.IMAGE && detailed) {
            DetailedTranscriptionPane.updateBorders(TranscribedImage.currentImageIndex);
        }
    }

    private static void saveAll(boolean detailedSnapshots) {

        EditedRecord.save();

        TranscribedImage.saveTranscriptionsDecryptionsPositions();

        colors.save(COLORS_FILE);
        key.saveKey();
        SymbolsSnapshot.keySnapshot();
        KeySnapshot.keySnapshot();

        if (!detailedSnapshots) {
            return;
        }
        boolean keepDetailed = detailed;
        Mode keepMode = mode;

        if (mode == Mode.IMAGE && detailed) {
            switchMode(Mode.IMAGE, false);
        }

        switchMode(Mode.CLUSTER, false);
        for (int i = 0; i < TranscribedImage.size(); i++) {
            MainImagePane.saveZoomAndScrollState();
            showImage(i, null, false);
            MainImagePane.symbolsInImageSnapshot();
            DetailedTranscriptionSnapshot.detailedTranscriptionSnapshot(i);
        }

        if (mode != keepMode) {
            switchMode(keepMode, keepDetailed);
        }

    }

}
