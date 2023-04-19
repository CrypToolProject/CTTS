package sample;

import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;

public class DragResizeMod {

    public static boolean acceptMainPaneMouseEvents = true;
    public static double[] pressed = new double[2];

    public interface OnDragResizeEventListener {
        void onDrag(Node node, double x, double y, double h, double w);
        void onResize(Node node, double x, double y, double h, double w);
    }


    private static final OnDragResizeEventListener defaultListener = new OnDragResizeEventListener() {
        @Override
        public void onDrag(Node node, double x, double y, double h, double w) {
            if (Main.mode == Mode.IMAGE && !Main.detailed && MainImagePane.subMode == MainImagePane.SubMode.SYMBOLS) {
                setNodeSize(node, x, y, h, w);
            }
        }

        @Override
        public void onResize(Node node, double x, double y, double h, double w) {
            if (Main.mode == Mode.IMAGE && !Main.detailed && MainImagePane.subMode == MainImagePane.SubMode.SYMBOLS) {
                setNodeSize(node, x, y, h, w);
            }
        }

        private void setNodeSize(Node node, double x, double y, double h, double w) {
            if (node instanceof Rectangle) {
                final Rectangle r = (Rectangle) node;

                TranscribedImage.resizeOrMove(r, x, y, w, h);
                Main.symbolResizedFromImagePane(r);
            }
        }
    };

    public enum S {
        DEFAULT,
        DRAG,
        NW_RESIZE,
        SW_RESIZE,
        NE_RESIZE,
        SE_RESIZE,
        E_RESIZE,
        W_RESIZE,
        N_RESIZE,
        S_RESIZE
    }

    private double clickX, clickY, nodeX, nodeY, nodeH, nodeW;

    private S state = S.DEFAULT;

    private final Node node;
    private OnDragResizeEventListener listener = defaultListener;

    private static final int MARGIN = 5;
    private static final double MIN_W = 15;
    private static final double MIN_H = 15;

    private DragResizeMod(Node node, OnDragResizeEventListener listener) {
        this.node = node;
        if (listener != null)
            this.listener = listener;
    }

    public static void makeResizable(Node node, OnDragResizeEventListener listener) {
        final DragResizeMod resizer = new DragResizeMod(node, listener);

        node.setOnMousePressed(resizer::mousePressed);
        node.setOnMouseDragged(resizer::mouseDragged);
        node.setOnMouseMoved(resizer::mouseOver);
        node.setOnMouseReleased(resizer::mouseReleased);
    }

    protected void mouseReleased(MouseEvent event) {
        node.setCursor(Cursor.DEFAULT);
        state = S.DEFAULT;
        acceptMainPaneMouseEvents = true;

    }

    protected void mouseOver(MouseEvent event) {
        S state = currentMouseState(event);
        Cursor cursor = getCursorForState(state);
        node.setCursor(cursor);
    }

    private S currentMouseState(MouseEvent event) {
        S state = S.DEFAULT;
        boolean left = isLeftResizeZone(event);
        boolean right = isRightResizeZone(event);
        boolean top = isTopResizeZone(event);
        boolean bottom = isBottomResizeZone(event);

        if (left && top) state = S.NW_RESIZE;
        else if (left && bottom) state = S.SW_RESIZE;
        else if (right && top) state = S.NE_RESIZE;
        else if (right && bottom) state = S.SE_RESIZE;
        else if (right) state = S.E_RESIZE;
        else if (left) state = S.W_RESIZE;
        else if (top) state = S.N_RESIZE;
        else if (bottom) state = S.S_RESIZE;
        else if (isInDragZone(event)) state = S.DRAG;

        return state;
    }

    private static Cursor getCursorForState(S state) {
        switch (state) {
            case NW_RESIZE : return Cursor.NW_RESIZE;
            case SW_RESIZE : return Cursor.SW_RESIZE;
            case NE_RESIZE : return Cursor.NE_RESIZE;
            case SE_RESIZE : return Cursor.SE_RESIZE;
            case E_RESIZE : return Cursor.E_RESIZE;
            case W_RESIZE : return Cursor.W_RESIZE;
            case N_RESIZE : return Cursor.N_RESIZE;
            case S_RESIZE : return Cursor.S_RESIZE;
            default : return Cursor.DEFAULT;
        }
    }


    protected void mouseDragged(MouseEvent event) {

        if (listener != null) {
            double mouseX = parentX(event.getX());
            double mouseY = parentY(event.getY());
            if (state == S.DRAG) {
                listener.onDrag(node, mouseX - clickX, mouseY - clickY, nodeH, nodeW);
            } else if (state != S.DEFAULT) {
                //resizing
                double newX = nodeX;
                double newY = nodeY;
                double newH = nodeH;
                double newW = nodeW;

                // Right Resize
                if (state == S.E_RESIZE || state == S.NE_RESIZE || state == S.SE_RESIZE) {
                    newW = mouseX - nodeX;
                }
                // Left Resize
                if (state == S.W_RESIZE || state == S.NW_RESIZE || state == S.SW_RESIZE) {
                    newX = mouseX;
                    newW = nodeW + nodeX - newX;
                }

                // Bottom Resize
                if (state == S.S_RESIZE || state == S.SE_RESIZE || state == S.SW_RESIZE) {
                    newH = mouseY - nodeY;
                }
                // Top Resize
                if (state == S.N_RESIZE || state == S.NW_RESIZE || state == S.NE_RESIZE) {
                    newY = mouseY;
                    newH = nodeH + nodeY - newY;
                }

                //min valid rect Size Check
                if (newW < MIN_W) {
                    if (state == S.W_RESIZE || state == S.NW_RESIZE || state == S.SW_RESIZE)
                        newX = newX - MIN_W + newW;
                    newW = MIN_W;
                }

                if (newH < MIN_H) {
                    if (state == S.N_RESIZE || state == S.NW_RESIZE || state == S.NE_RESIZE)
                        newY = newY + newH - MIN_H;
                    newH = MIN_H;
                }

                listener.onResize(node, newX, newY, newH, newW);
            }
        }
    }

    protected void mousePressed(MouseEvent event) {

        acceptMainPaneMouseEvents = false;

        Main.symbolSelectedFromImagePane((Rectangle) node);

        if (isInResizeZone(event)) {
            setNewInitialEventCoordinates(event);
            state = currentMouseState(event);
        } else if (isInDragZone(event)) {
            setNewInitialEventCoordinates(event);
            state = S.DRAG;
        } else {
            state = S.DEFAULT;
        }
    }

    private void setNewInitialEventCoordinates(MouseEvent event) {
        nodeX = nodeX();
        nodeY = nodeY();
        nodeH = nodeH();
        nodeW = nodeW();
        clickX = event.getX();
        clickY = event.getY();
    }

    private boolean isInResizeZone(MouseEvent event) {
        return isLeftResizeZone(event) || isRightResizeZone(event)
                || isBottomResizeZone(event) || isTopResizeZone(event);
    }

    private boolean isInDragZone(MouseEvent event) {
        double xPos = parentX(event.getX());
        double yPos = parentY(event.getY());
        double nodeX = nodeX() + MARGIN;
        double nodeY = nodeY() + MARGIN;
        double nodeX0 = nodeX() + nodeW() - MARGIN;
        double nodeY0 = nodeY() + nodeH() - MARGIN;

        return (xPos > nodeX && xPos < nodeX0) && (yPos > nodeY && yPos < nodeY0);
    }

    private boolean isLeftResizeZone(MouseEvent event) {
        return intersect(0, event.getX());
    }

    private boolean isRightResizeZone(MouseEvent event) {
        return intersect(nodeW(), event.getX());
    }

    private boolean isTopResizeZone(MouseEvent event) {
        return intersect(0, event.getY());
    }

    private boolean isBottomResizeZone(MouseEvent event) {
        return intersect(nodeH(), event.getY());
    }

    private boolean intersect(double side, double point) {
        return side + MARGIN > point && side - MARGIN < point;
    }

    private double parentX(double localX) {
        return nodeX() + localX;
    }

    private double parentY(double localY) {
        return nodeY() + localY;
    }

    private double nodeX() {
        return node.getBoundsInParent().getMinX();
    }

    private double nodeY() {
        return node.getBoundsInParent().getMinY();
    }

    private double nodeW() {
        return node.getBoundsInParent().getWidth();
    }

    private double nodeH() {
        return node.getBoundsInParent().getHeight();
    }
}