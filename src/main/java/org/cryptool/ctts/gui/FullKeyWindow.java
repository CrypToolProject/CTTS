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

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.cryptool.ctts.CTTSApplication;
import org.cryptool.ctts.util.Utils;

import java.util.ArrayList;


public class FullKeyWindow extends ScrollPane {
    final static Background whiteBg = new Background(new BackgroundFill(Color.WHITE, null, null));
    public static FullKeyWindow scrollPane;
    static double Vvalue = 0;
    static double Hvalue = 0;
    TilePane tilePane = new TilePane();

    public FullKeyWindow() {
        scrollPane = this;
        Pane pane = new Pane(tilePane);
        final Group group = new Group(pane);
        setContent(group);

        setHbarPolicy(ScrollBarPolicy.ALWAYS);
        setVbarPolicy(ScrollBarPolicy.ALWAYS);
        setMaxSize(Utils.adjust(2100), Utils.adjust(750));
        setMinSize(Utils.adjust(2100), Utils.adjust(750));

        final Background globalBackground = new Background(
                new BackgroundFill(Color.rgb(240, 240, 255), CornerRadii.EMPTY, Insets.EMPTY));
        tilePane.setBackground(globalBackground);

        tilePane.setVgap(Utils.adjust(6.0));
        tilePane.setHgap(Utils.adjust(6.0));
        tilePane.setPrefRows(100);
        tilePane.setOrientation(Orientation.VERTICAL);

        refresh();

    }

    public void refresh() {
        Vvalue = scrollPane.getVvalue();
        Hvalue = scrollPane.getHvalue();
        ArrayList<String> usedColors = CTTSApplication.colors.sortedColors();
        tilePane.getChildren().clear();
        for (String color : usedColors) {
            final HBox line = ClusterListView.line(color, true, false);
            if (line != null) {
                tilePane.getChildren().add(line);
            }
        }

        Timeline tl = new Timeline(
                new KeyFrame(Duration.millis(100),
                        event -> {
                            scrollPane.setVvalue(Vvalue);
                            scrollPane.setHvalue(Hvalue);
                        }
                ));
        tl.setCycleCount(1);
        tl.play();

    }

    public void show() {
        final double width = getParent().getBoundsInParent().getWidth();
        final double height = getParent().getBoundsInParent().getHeight();
        setMinWidth(width);
        setMinHeight(height);
        setMaxWidth(width);
        setMaxHeight(height);

        tilePane.setMinWidth(width);
        tilePane.setMinHeight(height - Utils.adjust(20));
        tilePane.setMaxWidth(width);
        tilePane.setMaxHeight(height - Utils.adjust(20));
        refresh();
        setVisible(true);
    }

    public void hide() {
        setVisible(false);
        setMaxWidth(0);
        setMinWidth(0);
    }

}