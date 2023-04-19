package sample;

import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollBar;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;


public class IconEditWindow {

    final static double MAX_GRAY_SCALE = 0.60;
    final static int BLUR_RADIUS = 3;
    final static double CONTRAST = 0.0;

    static double maxGrayScale = MAX_GRAY_SCALE;
    static double blurRadius = BLUR_RADIUS;
    static double contrast = CONTRAST;

    static boolean allowUpdates = false;

    static Stage myDialog;
    private static final int ICON_SIZE = 40;


    static Button save = new Button("Save");
    static Button close = new Button("Cancel");
    static ScrollBar maxGrayScaleSb = new ScrollBar();
    static ScrollBar blurRadiusSb = new ScrollBar();
    static ScrollBar contrastSb = new ScrollBar();
    static Text maxGrayScaleT = new Text();
    static Text blurRadiusT = new Text();
    static Text contrastT = new Text();
    static ImageView iconImageView = new ImageView();
    static ImageView origImageView = new ImageView();

    static WritableImage snapshot;

    public static void show(int idx, Rectangle r) {

        if (r == null) {
            return;
        }
        origImageView.setImage(TranscribedImage.transcribedImages[idx].image);
        origImageView.setFitWidth(Utils.adjust(ICON_SIZE * 2));
        origImageView.setPreserveRatio(true);
        origImageView.setFitHeight(Utils.adjust(ICON_SIZE * 2));
        double x = r.getLayoutX();
        double y = r.getLayoutY();
        double w = r.getWidth();
        double h = r.getHeight();

        Rectangle2D viewport = new Rectangle2D(x, y, w, h);
        origImageView.setViewport(viewport);

        snapshot = origImageView.snapshot(new SnapshotParameters(), null);

        WritableImage image2 = transform(snapshot);

        iconImageView.setImage(image2);
        iconImageView.setFitWidth(Utils.adjust(ICON_SIZE * 2));
        iconImageView.setPreserveRatio(true);
        iconImageView.setFitHeight(Utils.adjust(ICON_SIZE * 2));

        myDialog = new Stage();
        myDialog.initModality(Modality.APPLICATION_MODAL);


        VBox vBox = new VBox();
        vBox.setSpacing(30);
        //vBox.setAlignment(Pos.CENTER);
        vBox.setPadding(new Insets(10));

        Color color = (Color) r.getFill();
        Color newColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), 0.4);
        final Background background = new Background(new BackgroundFill(newColor, CornerRadii.EMPTY, Insets.EMPTY));
        vBox.setBackground(background);

        StackPane sp = new StackPane(iconImageView);
        sp.setMinHeight(Utils.adjust(ICON_SIZE * 2));
        sp.setMaxHeight(Utils.adjust(ICON_SIZE * 2));

        vBox.getChildren().addAll(new HBox(sp, hRegion(0), origImageView));
        vBox.getChildren().addAll(new HBox(maxGrayScaleSb, hRegion(10), maxGrayScaleT));
        vBox.getChildren().addAll(new HBox(blurRadiusSb, hRegion(10), blurRadiusT));
        //vBox.getChildren().addAll(new HBox(contrastSb, hRegion(10), contrastT));
        vBox.getChildren().add(new HBox(hRegion(40), save, hRegion(45), close));


        allowUpdates = false;

        maxGrayScaleSb.setMinWidth(Utils.adjust(150));
        maxGrayScaleSb.setMin(0.0);
        maxGrayScaleSb.setMax(1.0);
        maxGrayScaleSb.valueProperty().addListener((ObservableValue<? extends Number> ov, Number old_val, Number new_val) -> {
            if (allowUpdates) {
                update();
            }
        });
        maxGrayScaleSb.setValue(maxGrayScale);

        blurRadiusSb.setMinWidth(Utils.adjust(150));
        blurRadiusSb.setMin(0.0);
        blurRadiusSb.setMax(10.0);
        blurRadiusSb.valueProperty().addListener((ObservableValue<? extends Number> ov, Number old_val, Number new_val) -> {
            if (allowUpdates) {
                update();
            }
        });

        contrastSb.setValue(contrast);
        contrastSb.setMinWidth(Utils.adjust(150));
        contrastSb.setMin(0.0);
        contrastSb.setMax(1.0);
        contrastSb.valueProperty().addListener((ObservableValue<? extends Number> ov, Number old_val, Number new_val) -> {
            if (allowUpdates) {
                update();
            }
        });
        contrastSb.setValue(contrast);

        save.setOnAction(arg0 -> {
            WritableImage image = transform(snapshot);

            Icons.saveIcon(color, image);
            if (Icons.readIcon(color, false)) {
                Main.colorParametersChanged(color, false, true, false);
            }

            myDialog.close();
        });
        close.setOnAction(arg0 -> myDialog.close());


        Scene myDialogScene = new Scene(vBox);
        myDialog.setScene(myDialogScene);
        myDialog.setMinWidth(Utils.adjust(300));
        myDialog.setMinHeight(Utils.adjust(300));
        myDialog.setTitle("Edit Icon - " + Main.colors.get(color.toString()));

        close.setMinWidth(70);
        save.setMinWidth(70);

        allowUpdates = true;
        update();


        myDialog.show();
    }

    static void update() {
        maxGrayScale = maxGrayScaleSb.getValue();
        blurRadius = blurRadiusSb.getValue();
        contrast = contrastSb.getValue();

        iconImageView.setImage(transform(snapshot));

        maxGrayScaleT.setText( String.format("Threshold: %4.2f", maxGrayScale));
        blurRadiusT.setText( String.format(  "Soften: %4.2f", blurRadius/10.0));
        contrastT.setText( String.format(  "Contrast: %4.2f", contrast));

    }

    private static WritableImage transform(Image snapshot) {
        return ImageUtils.binarize(ImageUtils.gaussianBlur(ImageUtils.binarize(ImageUtils.contrast(snapshot, contrast), maxGrayScale), blurRadius), 0.1);
    }

    static Region hRegion(double gap) {
        Region region = new Region();
        region.setMinHeight(1);
        if (gap == 0) {
            HBox.setHgrow(region, Priority.ALWAYS);
        }
        region.setMinWidth(Utils.adjust(gap));
        return region;
    }


}