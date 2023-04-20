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

package org.cryptool.ota.util;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class SymbolRectangle extends Rectangle {
    public SymbolRectangle(double layoutX, double layoutY, double width, double height, Color color, double opacity,
            boolean visible) {
        super(width, height);
        setLayoutX(layoutX);
        setLayoutY(layoutY);
        setFill(color);
        opacityProperty().set(opacity);
        DragResizeMod.makeResizable(this, null);
        setVisible(visible);

    }
}
