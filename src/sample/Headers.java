package sample;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class Headers {
    static Text leftTitle;
    static Text rightTitle;
    static Text legend;
    static Text status;

    static void updateTopTitle(){
        String fs = FileUtils.currentDirectoryFullpathString();
        Main.myStage.setTitle("Offline Transcription Application (OTA - V3.5 - 02/10/2022) - " + fs);
    }

    static void updateHeadersAndBottom() {

        String f9String = MainImagePane.nextSubMode().toString();
        f9String = f9String.charAt(0) + f9String.toLowerCase().substring(1);
        String f3f4String = "Previous/Next Symbol";
        if (Main.mode == Mode.IMAGE && !Main.detailed && MainImagePane.subMode == MainImagePane.SubMode.DECRYPTION) {
            f3f4String = "Smaller/Bigger Font";
        }

        String LEGEND_IMAGE_VIEW = ""
                + " F1/F2 Next/Previous Document"
                + " F3/F4 " + f3f4String
                + " F5/F6 Zoom In/Out"
                + " F7 Snapshot"
                + " F8 Cryptanalysis"
                + " F9 " +  f9String
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


        switch (Main.mode) {
            case IMAGE:
                leftTitle.setText(Main.detailed ? " Transcription Review" : " Transcription ");
                if (TranscribedImage.transcribedImages.length > 1) {
                    rightTitle.setText(TranscribedImage.current().filename + " [" + (TranscribedImage.currentImageIndex + 1) + "/" + TranscribedImage.size() + " - " + FileUtils.currentDirectoryString()+ "] ");
                } else {
                    rightTitle.setText(TranscribedImage.current().filename + " [" + FileUtils.currentDirectoryString()+ "] ");
                }
                if (Main.key.isKeyAvailable()) {
                    status.setText("[" + TranscribedImage.current().positions().size() + " symbols in " + TranscribedImage.current().filename + "]"  + " " + "[Key: " + Main.key.getKeyFilename() + "]");
                } else {
                    status.setText("[" + TranscribedImage.current().positions().size() + " symbols in " + TranscribedImage.current().filename + "]");
                }
                legend.setText(Main.detailed ? LEGEND_IMAGE_DETAILED_VIEW : LEGEND_IMAGE_VIEW);

                break;
            case CLUSTER:
                leftTitle.setText(Main.detailed ? " Symbols Types - Grid View": " Symbols Types - List View");
                legend.setText(Main.detailed ? LEGEND_CLUSTER_DETAILED_VIEW : LEGEND_CLUSTER_VIEW);
                if (Main.key.isKeyAvailable()) {
                    status.setText("[" + TranscribedImage.totalSymbols() + " symbols in " + TranscribedImage.size() + " documents]" + " " + "[Key: " + Main.key.getKeyFilename() + "]");
                } else {
                    status.setText("[" + TranscribedImage.totalSymbols() + " symbols in " + TranscribedImage.size() + " documents]");
                }
                rightTitle.setText("");
                break;

        }

        if (Main.colors.changed() || Main.key.changed() || TranscribedImage.change()) {
            status.setText(status.getText() + " *");
        }



    }

    static HBox initLegend() {
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
                            boolean changed = Main.colors.changed() || Main.key.changed() || TranscribedImage.change() || EditedRecord.changed;
                            boolean changedShown = status.getText().endsWith("*");
                            if (changedShown != changed) {
                                updateHeadersAndBottom();
                            }
                        }
                ));
        tl.setCycleCount(Animation.INDEFINITE);
        tl.play();

        return legendHBox;
    }

    static HBox initTitle() {
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
