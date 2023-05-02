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

import org.cryptool.ctts.CTTSApplication;
import org.cryptool.ctts.CTTSApplication.Mode;
import org.cryptool.ctts.util.EditedRecord;
import org.cryptool.ctts.util.FileUtils;
import org.cryptool.ctts.util.TranscribedImage;
import org.cryptool.ctts.util.Utils;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class Headers {

    public static Text leftTitle;
    public static Text rightTitle;
    public static Text legend;
    public static Text status;

    public static void updateTopTitle() {
        String fs = FileUtils.currentDirectoryFullpathString();
        CTTSApplication.myStage.setTitle("CrypTool Transcription & Solver (CTTS - V3.5 - 2023-05-02) - " + fs);
    }

    public static void updateHeadersAndBottom() {

        String f9String = MainImagePane.nextSubMode().toString();
        f9String = f9String.charAt(0) + f9String.toLowerCase().substring(1);
        String f3f4String = "Previous/Next Symbol";
        if (CTTSApplication.mode == Mode.IMAGE && !CTTSApplication.detailed && MainImagePane.subMode == MainImagePane.SubMode.DECRYPTION) {
            f3f4String = "Smaller/Bigger Font";
        }

        String LEGEND_IMAGE_VIEW = ""
                + " F1/F2 Next/Previous Document"
                + " F3/F4 " + f3f4String
                + " F5/F6 Zoom In/Out"
                + " F7 Snapshot"
                + " F8 Cryptanalysis"
                + " F9 " + f9String
                + " F10 Save"
                + " F11/tab Symbols Types"
                + " F12 Transcription Review"
                + " DEL Delete Symbol"
                + " ESC Exit";

        String LEGEND_IMAGE_DETAILED_VIEW = ""
                + " F1/F2 Next/Previous Document"
                + " F3/F4 Previous/Next Symbol"
                + " F5/F6 Zoom In/Out"
                + " F7 Snapshot"
                + " F8 Cryptanalysis"
                + " F10 Save"
                + " F11/tab Symbols Types"
                + " F12 Transcription"
                + " ESC Exit";

        String LEGEND_CLUSTER_VIEW = ""
                + " F1 Sort by Name"
                + " F2 Sort by Decryption"
                + " F3 Sort by Count"
                + " F7 Snapshot"
                + " F8 Cryptanalysis"
                + " F10 Save"
                + " F11/tab Transcription"
                + " F12 Symbols Types - Grid View"
                + " ESC Exit";
        String LEGEND_CLUSTER_DETAILED_VIEW = ""
                + " F1 Sort by Name"
                + " F2 Sort by Decryption"
                + " F3 Sort by Count"
                + " F7 Snapshot"
                + " F8 Cryptanalysis"
                + " F10 Save"
                + " F11/tab Transcription"
                + " F12 Symbols Types - List View"
                + " ESC Exit";

        switch (CTTSApplication.mode) {
            case IMAGE:
                leftTitle.setText(CTTSApplication.detailed ? " Transcription Review" : " Transcription ");
                if (TranscribedImage.transcribedImages.length > 1) {
                    rightTitle.setText(
                            TranscribedImage.current().filename + " [" + (TranscribedImage.currentImageIndex + 1) + "/"
                                    + TranscribedImage.size() + " - " + FileUtils.currentDirectoryString() + "] ");
                } else {
                    rightTitle.setText(
                            TranscribedImage.current().filename + " [" + FileUtils.currentDirectoryString() + "] ");
                }
                if (CTTSApplication.key.isKeyAvailable()) {
                    status.setText("[" + TranscribedImage.current().positions().size() + " symbols in "
                            + TranscribedImage.current().filename + "]" + " " + "[Key: " + CTTSApplication.key.getKeyFilename()
                            + "]");
                } else {
                    status.setText("[" + TranscribedImage.current().positions().size() + " symbols in "
                            + TranscribedImage.current().filename + "]");
                }
                legend.setText(CTTSApplication.detailed ? LEGEND_IMAGE_DETAILED_VIEW : LEGEND_IMAGE_VIEW);

                break;
            case CLUSTER:
                leftTitle.setText(CTTSApplication.detailed ? " Symbols Types - Grid View" : " Symbols Types - List View");
                legend.setText(CTTSApplication.detailed ? LEGEND_CLUSTER_DETAILED_VIEW : LEGEND_CLUSTER_VIEW);
                if (CTTSApplication.key.isKeyAvailable()) {
                    status.setText("[" + TranscribedImage.totalSymbols() + " symbols in " + TranscribedImage.size()
                            + " documents]" + " " + "[Key: " + CTTSApplication.key.getKeyFilename() + "]");
                } else {
                    status.setText("[" + TranscribedImage.totalSymbols() + " symbols in " + TranscribedImage.size()
                            + " documents]");
                }
                rightTitle.setText("");
                break;

        }

        if (CTTSApplication.colors.changed() || CTTSApplication.key.changed() || TranscribedImage.changed()) {
            status.setText(status.getText() + " *");
        }

    }

    public static HBox initLegend() {
        legend = new Text("");
        legend.setFont(new Font(Utils.adjust(16)));
        status = new Text("");
        status.setFont(new Font(Utils.adjust(16)));
        Region legendMidRegion = new Region();
        Region legendRightRegion = new Region();
        legendRightRegion.setMinWidth(Utils.adjust(10));

        HBox legendHBox = new HBox(legend, legendMidRegion, status, legendRightRegion);
        legendHBox.setMinHeight(Utils.adjust(30));
        legendHBox.setMaxHeight(Utils.adjust(30));
        HBox.setHgrow(legendMidRegion, Priority.ALWAYS);

        Timeline tl = new Timeline(
                new KeyFrame(Duration.millis(1000),
                        event -> {
                            boolean changed = CTTSApplication.colors.changed() || CTTSApplication.key.changed() || TranscribedImage.changed()
                                    || EditedRecord.changed;
                            boolean changedShown = status.getText().endsWith("*");
                            if (changedShown != changed) {
                                updateHeadersAndBottom();
                            }
                        }));
        tl.setCycleCount(Animation.INDEFINITE);
        tl.play();

        return legendHBox;
    }

    public static HBox initTitle() {
        Font hugeBold = Font.font("Verdana", FontWeight.BOLD, Utils.adjust(36));
        leftTitle = new Text(" ");
        leftTitle.setFont(hugeBold);
        rightTitle = new Text(" ");
        rightTitle.setFont(hugeBold);
        Region titleMidRegion = new Region();

        HBox titleHbox = new HBox(leftTitle, titleMidRegion, rightTitle);
        titleHbox.setBackground(new Background(new BackgroundFill(Color.LIGHTCYAN, CornerRadii.EMPTY, Insets.EMPTY)));
        HBox.setHgrow(titleMidRegion, Priority.ALWAYS);
        return titleHbox;
    }
}
