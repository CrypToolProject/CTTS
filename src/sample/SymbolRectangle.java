package sample;


import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class SymbolRectangle extends Rectangle {
    SymbolRectangle(double layoutX, double layoutY, double width, double height, Color color, double opacity, boolean visible){
        super(width, height);
        setLayoutX(layoutX);
        setLayoutY(layoutY);
        setFill(color);
        opacityProperty().set(opacity);
        DragResizeMod.makeResizable(this, null);
        setVisible(visible);

    }
}
