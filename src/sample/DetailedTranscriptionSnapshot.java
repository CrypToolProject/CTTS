package sample;

import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.transform.Scale;
import javafx.stage.Popup;


public class DetailedTranscriptionSnapshot extends Popup {

    Pane mainPane;
    boolean snapshot;
    int index;
    DetailedTranscriptionSnapshot(boolean snapshot, int index) {
        this.index = index;
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
        scrollPane.setMaxSize(2000, 1000);

        final Background globalBackground = new Background(new BackgroundFill(Color.rgb(240, 240, 255), CornerRadii.EMPTY, Insets.EMPTY));
        mainPane.setBackground(globalBackground);
        canvas.setWidth(1000);
        canvas.setHeight(500);

        VBox lines = DetailedTranscriptionPane.drawLines(index);

        mainPane.getChildren().add(lines);
        getContent().addAll(scrollPane);



    }

    static void detailedTranscriptionSnapshot(int i) {
        DetailedTranscriptionSnapshot p = new DetailedTranscriptionSnapshot(true, i);
        p.show(Main.myStage);
        p.snapshot();
        p.hide();
    }


    public void snapshot() {

        FileUtils.snapshot("snapshots", TranscribedImage.transcribedImages[index].filename.replaceAll("\\..*", "_detailed"), mainPane);

    }

}