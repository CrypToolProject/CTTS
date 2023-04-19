package sample;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class SelectionBox {

    static final double MARGIN_WIDTH = 3;
    static final Color COLOR = Color.valueOf("0xfe0000ff");
    static final Rectangle rect1 = new Rectangle(200,200);
    static final Rectangle rect2 = new Rectangle(200,200);
    static final Rectangle rect3 = new Rectangle(200,200);
    static final Rectangle rect4 = new Rectangle(200,200);
    static void add(Pane mainPane) {
        mainPane.getChildren().add(rect1);
        mainPane.getChildren().add(rect2);
        mainPane.getChildren().add(rect3);
        mainPane.getChildren().add(rect4);
        hide();
    }
    static void hide(){
        rect1.setVisible(false);
        rect2.setVisible(false);
        rect3.setVisible(false);
        rect4.setVisible(false);
    }

    static void show(Rectangle r){
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
