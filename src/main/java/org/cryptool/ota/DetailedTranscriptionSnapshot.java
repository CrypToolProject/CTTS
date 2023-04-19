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
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
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

        final Background globalBackground = new Background(
                new BackgroundFill(Color.rgb(240, 240, 255), CornerRadii.EMPTY, Insets.EMPTY));
        mainPane.setBackground(globalBackground);
        canvas.setWidth(1000);
        canvas.setHeight(500);

        VBox lines = DetailedTranscriptionPane.drawLines(index);

        mainPane.getChildren().add(lines);
        getContent().addAll(scrollPane);

    }

    static void detailedTranscriptionSnapshot(int i) {
        DetailedTranscriptionSnapshot p = new DetailedTranscriptionSnapshot(true, i);
        p.show(OTAApplication.myStage);
        p.snapshot();
        p.hide();
    }

    public void snapshot() {

        FileUtils.snapshot("snapshots",
                TranscribedImage.transcribedImages[index].filename.replaceAll("\\..*", "_detailed"), mainPane);

    }

}