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

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class SelectionBox {

    static final double MARGIN_WIDTH = 3;
    static final Color COLOR = Color.valueOf("0xfe0000ff");
    static final Rectangle rect1 = new Rectangle(200, 200);
    static final Rectangle rect2 = new Rectangle(200, 200);
    static final Rectangle rect3 = new Rectangle(200, 200);
    static final Rectangle rect4 = new Rectangle(200, 200);

    static void add(Pane mainPane) {
        mainPane.getChildren().add(rect1);
        mainPane.getChildren().add(rect2);
        mainPane.getChildren().add(rect3);
        mainPane.getChildren().add(rect4);
        hide();
    }

    static void hide() {
        rect1.setVisible(false);
        rect2.setVisible(false);
        rect3.setVisible(false);
        rect4.setVisible(false);
    }

    static void show(Rectangle r) {
        rect1.setFill(COLOR);
        rect1.setLayoutX(r.getLayoutX() - MARGIN_WIDTH);
        rect1.setLayoutY(r.getLayoutY() - MARGIN_WIDTH);
        rect1.setWidth(r.getWidth() + 2 * MARGIN_WIDTH);
        rect1.setHeight(MARGIN_WIDTH);

        rect2.setFill(COLOR);
        rect2.setLayoutX(r.getLayoutX() - MARGIN_WIDTH);
        rect2.setLayoutY(r.getLayoutY() - MARGIN_WIDTH);
        rect2.setHeight(r.getHeight() + 2 * MARGIN_WIDTH);
        rect2.setWidth(MARGIN_WIDTH);

        rect3.setFill(COLOR);
        rect3.setLayoutX(r.getLayoutX() - MARGIN_WIDTH);
        rect3.setLayoutY(r.getLayoutY() + r.getHeight());
        rect3.setWidth(r.getWidth() + 2 * MARGIN_WIDTH);
        rect3.setHeight(MARGIN_WIDTH);

        rect4.setFill(COLOR);
        rect4.setLayoutX(r.getLayoutX() + r.getWidth());
        rect4.setLayoutY(r.getLayoutY() - MARGIN_WIDTH);
        rect4.setHeight(r.getHeight() + 2 * MARGIN_WIDTH);
        rect4.setWidth(MARGIN_WIDTH);

        rect1.setVisible(true);
        rect2.setVisible(true);
        rect3.setVisible(true);
        rect4.setVisible(true);
    }
}
